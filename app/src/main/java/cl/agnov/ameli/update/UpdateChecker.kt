package cl.agnov.ameli.update

import cl.agnov.ameli.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class ReleaseInfo(
    val versionName: String,
    val releaseUrl: String,
    val apkDownloadUrl: String?,
)

/**
 * Consulta la última Release publicada en GitHub y la compara con la
 * versión instalada. No requiere autenticación (repositorio público) ni
 * dependencias nuevas: usa `org.json`, incluido en el SDK de Android.
 */
class UpdateChecker(private val currentVersionName: String) {

    /** Devuelve la [ReleaseInfo] de la última release si es más nueva que la instalada, o null si no hay novedad o falló la consulta. */
    fun checkForUpdate(): ReleaseInfo? {
        return try {
            val connection = URL(
                "https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest",
            ).openConnection() as HttpsURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/vnd.github+json")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) return null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val remoteVersion = json.getString("tag_name").removePrefix("v")

            if (!isNewer(remoteVersion, currentVersionName)) return null

            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }
            }

            ReleaseInfo(
                versionName = remoteVersion,
                releaseUrl = json.getString("html_url"),
                apkDownloadUrl = apkUrl,
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun isNewer(remote: String, local: String): Boolean {
        val remoteParts = remote.split(".").map { it.toIntOrNull() ?: 0 }
        val localParts = local.split(".").map { it.toIntOrNull() ?: 0 }
        val maxSize = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until maxSize) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r > l
        }
        return false
    }

    private companion object {
        const val TIMEOUT_MS = 5_000
    }
}
