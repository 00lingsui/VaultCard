package com.example.cardmanager.core.webdav

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object WebDavClient {

    private const val DEFAULT_BACKUP_FILENAME = "vaultcard_backup.json"

    fun getBackupFileUrl(config: WebDavConfig): String {
        val base = config.serverUrl.trim()
        return when {
            base.endsWith(".json", ignoreCase = true) -> base
            base.endsWith("/") -> "$base$DEFAULT_BACKUP_FILENAME"
            else -> "$base/$DEFAULT_BACKUP_FILENAME"
        }
    }

    private fun getAuthHeader(config: WebDavConfig): String {
        val credentials = "${config.username.trim()}:${config.password}"
        val encoded = Base64.encodeToString(credentials.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        return "Basic $encoded"
    }

    private fun openConnection(urlString: String, config: WebDavConfig, method: String): HttpURLConnection {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 12000
        connection.readTimeout = 12000
        connection.setRequestProperty("Authorization", getAuthHeader(config))
        connection.setRequestProperty("User-Agent", "VaultCard-Android")
        return connection
    }

    suspend fun testConnection(config: WebDavConfig): Result<Unit> = withContext(Dispatchers.IO) {
        if (config.serverUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("服务器地址不能为空"))
        }
        if (config.username.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("用户名/账号不能为空"))
        }

        try {
            val targetUrl = config.serverUrl.trim()
            var conn = openConnection(targetUrl, config, "PROPFIND").apply {
                setRequestProperty("Depth", "0")
            }

            var responseCode = conn.responseCode
            // Some servers return 405 Method Not Allowed for PROPFIND, fallback to GET/HEAD
            if (responseCode == 405) {
                conn.disconnect()
                conn = openConnection(targetUrl, config, "GET")
                responseCode = conn.responseCode
            }

            when (responseCode) {
                in 200..299 -> Result.success(Unit)
                401, 403 -> Result.failure(Exception("认证失败：用户名或应用密码错误 (HTTP $responseCode)"))
                404 -> Result.success(Unit) // Path doesn't exist yet, but server is reachable and auth passed
                else -> Result.failure(Exception("服务器响应异常 (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ensureRemoteDirectory(fileUrl: String, config: WebDavConfig) {
        try {
            val lastSlash = fileUrl.lastIndexOf('/')
            if (lastSlash > 8) {
                val dirUrl = fileUrl.substring(0, lastSlash)
                val conn = openConnection(dirUrl, config, "MKCOL")
                conn.responseCode // execute
                conn.disconnect()
            }
        } catch (_: Exception) {
            // Ignore if directory already exists or parent directory cannot be created
        }
    }

    suspend fun uploadBackup(config: WebDavConfig, jsonContent: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileUrl = getBackupFileUrl(config)
            val bytes = jsonContent.toByteArray(Charsets.UTF_8)

            var conn = openConnection(fileUrl, config, "PUT").apply {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                setFixedLengthStreamingMode(bytes.size)
            }

            conn.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }

            var responseCode = conn.responseCode
            if (responseCode == 404) {
                conn.disconnect()
                ensureRemoteDirectory(fileUrl, config)
                conn = openConnection(fileUrl, config, "PUT").apply {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setFixedLengthStreamingMode(bytes.size)
                }
                conn.outputStream.use { os ->
                    os.write(bytes)
                    os.flush()
                }
                responseCode = conn.responseCode
            }

            conn.disconnect()

            if (responseCode in 200..299) {
                Result.success(Unit)
            } else if (responseCode == 401 || responseCode == 403) {
                Result.failure(Exception("WebDAV 认证失败，请检查账号密码 (HTTP $responseCode)"))
            } else {
                Result.failure(Exception("上传备份失败 (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadBackup(config: WebDavConfig): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fileUrl = getBackupFileUrl(config)
            val conn = openConnection(fileUrl, config, "GET")
            val responseCode = conn.responseCode

            if (responseCode in 200..299) {
                val jsonString = conn.inputStream.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        reader.readText()
                    }
                }
                conn.disconnect()
                Result.success(jsonString)
            } else if (responseCode == 404) {
                conn.disconnect()
                Result.failure(Exception("云端未找到备份文件 $DEFAULT_BACKUP_FILENAME"))
            } else if (responseCode == 401 || responseCode == 403) {
                conn.disconnect()
                Result.failure(Exception("WebDAV 认证失败，请检查账号密码 (HTTP $responseCode)"))
            } else {
                conn.disconnect()
                Result.failure(Exception("下载备份失败 (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
