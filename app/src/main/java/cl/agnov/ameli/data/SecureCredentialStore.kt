package cl.agnov.ameli.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Permite sustituir [SecureCredentialStore] por un fake en pruebas unitarias. */
interface CredentialStore {
    fun savePassword(password: String)
    fun readPassword(): String?
    fun clear()
}

/**
 * Almacena la contraseña SIP cifrada con una clave AES-GCM guardada en
 * Android Keystore. Nunca se guarda en texto plano ni se registra en logs.
 *
 * `androidx.security.crypto` (EncryptedSharedPreferences/MasterKey) está
 * marcado `@Deprecated` desde la versión 1.1.0 en favor de usar
 * [KeyGenerator] con AndroidKeyStore directamente, que es lo que se hace
 * aquí: solo la clave vive en el Keystore, el valor cifrado (IV + texto
 * cifrado, en Base64) se guarda en unas SharedPreferences normales.
 */
class SecureCredentialStore(context: Context) : CredentialStore {

    private val preferences =
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    private val secretKey: SecretKey by lazy { getOrCreateKey() }

    override fun savePassword(password: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }
        val ciphertext = cipher.doFinal(password.toByteArray(Charsets.UTF_8))

        preferences.edit()
            .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putString(KEY_CIPHERTEXT, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
            .apply()
    }

    override fun readPassword(): String? {
        val ivEncoded = preferences.getString(KEY_IV, null) ?: return null
        val ciphertextEncoded = preferences.getString(KEY_CIPHERTEXT, null) ?: return null

        return try {
            val iv = Base64.decode(ivEncoded, Base64.NO_WRAP)
            val ciphertext = Base64.decode(ciphertextEncoded, Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            }
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    override fun clear() {
        preferences.edit().remove(KEY_IV).remove(KEY_CIPHERTEXT).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "ameli_sip_password_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val FILE_NAME = "ameli_secure_credentials"
        const val KEY_IV = "sip_password_iv"
        const val KEY_CIPHERTEXT = "sip_password_ciphertext"
    }
}
