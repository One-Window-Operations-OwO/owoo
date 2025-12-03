package com.example.owoo.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

object HisenseAuthService {

    private const val hisenseUrl = "https://kemendikdasmen.hisense.id/"

    suspend fun loginHisense(username: String, password: String): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${hisenseUrl}login_p.php")
                val postData = "username=$username&password=$password"

                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                conn.instanceFollowRedirects = false

                conn.outputStream.use { os ->
                    os.write(postData.toByteArray())
                }

                val cookieHeader = conn.headerFields["Set-Cookie"]
                if (cookieHeader != null) {
                    for (cookie in cookieHeader) {
                        if (cookie.startsWith("PHPSESSID=")) {
                            val phpsessid = cookie.split(";")[0].split("=")[1]
                            return@withContext Result.success(phpsessid)
                        }
                    }
                }
                Result.failure(Exception("Login failed: No cookie received."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun validateHisenseCookie(cookie: String): Result<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${hisenseUrl}r_dkm.php")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Cookie", "PHPSESSID=$cookie")

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(Exception("Failed to access Hisense. Response code: ${conn.responseCode}"))
                }

                val html = conn.inputStream.bufferedReader().use { it.readText() }
                val doc = Jsoup.parse(html)
                val buttonText = doc.select(".dropdown-toggle").text().trim()

                if (buttonText.isNotEmpty()) {
                    Result.success(buttonText)
                } else {
                    Result.failure(Exception("Cookie validation failed: User not logged in."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}