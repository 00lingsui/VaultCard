package com.example.cardmanager.core.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.cardmanager.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdateInfo(
    val latestVersion: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val isUpdateAvailable: Boolean
)

@Singleton
class UpdateManager @Inject constructor() {

    suspend fun checkForUpdates(
        currentVersionName: String = BuildConfig.VERSION_NAME
    ): AppUpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/00lingsui/VaultCard/releases/latest")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                connectTimeout = 8000
                readTimeout = 8000
            }

            if (connection.responseCode == 200) {
                val jsonStr = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonStr)

                val rawTag = json.getString("tag_name")
                val cleanTag = rawTag.removePrefix("v").removePrefix("V").trim()
                val body = json.optString("body", "修复已知问题，提升性能与稳定性。")

                var apkDownloadUrl = ""
                val assets = json.optJSONArray("assets")
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.getString("name")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkDownloadUrl = asset.getString("browser_download_url")
                            break
                        }
                    }
                }

                val isStrictlyNewer = isNewerVersion(currentVersionName, cleanTag)
                val isAvailable = isStrictlyNewer && apkDownloadUrl.isNotEmpty()

                AppUpdateInfo(
                    latestVersion = cleanTag,
                    releaseNotes = body,
                    downloadUrl = apkDownloadUrl,
                    isUpdateAvailable = isAvailable
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun startDownload(context: Context, downloadUrl: String, fileName: String = "VaultCard-update.apk") {
        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
            setTitle("正在下载 VaultCard 更新")
            setDescription("正在从 GitHub 下载最新版本安装包")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
        }

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }

    fun isNewerVersion(current: String, latest: String): Boolean {
        val cleanCurrent = current.removePrefix("v").removePrefix("V").substringBefore("-").trim()
        val cleanLatest = latest.removePrefix("v").removePrefix("V").substringBefore("-").trim()

        val currentParts = cleanCurrent.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = cleanLatest.split(".").mapNotNull { it.toIntOrNull() }

        if (currentParts.isEmpty() || latestParts.isEmpty()) {
            return false
        }

        val length = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until length) {
            val curr = currentParts.getOrElse(i) { 0 }
            val lat = latestParts.getOrElse(i) { 0 }
            if (lat > curr) return true
            if (lat < curr) return false
        }
        return false
    }
}
