package cl.agnov.ameli.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Permite sustituir [SecureCredentialStore] por un fake en pruebas unitarias. */
interface CredentialStore {
    fun savePassword(password: String)
    fun readPassword(): String?
    fun saveTurnPassword(password: String)
    fun readTurnPassword(): String?
    fun clear()
}

/**
 * Almacena credenciales (contraseña SIP y, opcionalmente, contraseña TURN)
 * cifradas con una clave AES-GCM guardada en Android Keystore. Nunca se
 * guardan en texto plano ni se registran en logs.
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

    override fun savePassword(password: String) = save(KEY_SIP_IV, KEY_SIP_CIPHERTEXT, password)

    override fun readPassword(): String? = read(KEY_SIP_IV, KEY_SIP_CIPHERTEXT)

    override fun saveTurnPassword(password: String) = save(KEY_TURN_IV, KEY_TURN_CIPHERTEXT, password)

    override fun readTurnPassword(): String? = read(KEY_TURN_IV, KEY_TURN_CIPHERTEXT)

    override fun clear() {
        preferences.edit {
            remove(KEY_SIP_IV)
            remove(KEY_SIP_CIPHERTEXT)
            remove(KEY_TURN_IV)
            remove(KEY_TURN_CIPHERTEXT)
        }
    }

    private fun save(ivKey: String, ciphertextKey: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, secretKey)
        }
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))

        preferences.edit {
            putString(ivKey, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            putString(ciphertextKey, Base64.encodeToString(ciphertext, Base64.NO_WRAP))
        }
    }

    private fun read(ivKey: String, ciphertextKey: String): String? {
        val ivEncoded = preferences.getString(ivKey, null) ?: return null
        val ciphertextEncoded = preferences.getString(ciphertextKey, null) ?: return null

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
        const val KEY_SIP_IV = "sip_password_iv"
        const val KEY_SIP_CIPHERTEXT = "sip_password_ciphertext"
        const val KEY_TURN_IV = "turn_password_iv"
        const val KEY_TURN_CIPHERTEXT = "turn_password_ciphertext"
    }
}
