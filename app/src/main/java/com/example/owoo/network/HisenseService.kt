package com.example.owoo.network

import com.example.owoo.data.hisense.HisenseData
import com.example.owoo.data.hisense.HisenseProcessHistory
import com.example.owoo.data.hisense.HisenseSchoolInfo
import org.jsoup.Jsoup
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

data class HisenseResponse(val body: String, val status: Int, val contentType: String) {
    val isSuccessful: Boolean
        get() = status in 200..299
}

object HisenseService {

    private const val hisenseUrl = "https://kemendikdasmen.hisense.id/"

    fun getHisense(npsn: String, cookie: String): HisenseData {
        try {
            if (cookie.isEmpty()) {
                return HisenseData(isGreen = false, error = "Cookie PHPSESSID diperlukan")
            }

            // 1. Fetch the initial monitoring page
            val initialUrl = "${hisenseUrl}r_monitoring.php?inpsn=$npsn"
            val conn1 = URL(initialUrl).openConnection() as HttpURLConnection
            conn1.requestMethod = "GET"
            conn1.setRequestProperty("Cookie", "PHPSESSID=$cookie")

            val html1 = conn1.inputStream.bufferedReader().readText()
            val doc1 = Jsoup.parse(html1)

            // 2. Parse the first row to find the link to the details page and the color status
            val firstRow = doc1.select("#main-content > div > div > div > div.table-container > div > table > tbody tr").first()
            val onClickAttribute = firstRow?.attr("onclick")?.toString() ?: ""
            val urlMatch = Regex("window\\.open\\('([^']*)'").find(onClickAttribute)
            val nextPath = urlMatch?.groupValues?.getOrNull(1)

            val firstTdStyle = firstRow?.select("td")?.first()?.attr("style") ?: ""
            val isGreen = firstTdStyle.contains("color:green")

            if (nextPath == null) {
                return HisenseData(isGreen = isGreen)
            }

            // 3. Fetch the details page (dkm.php)
            val dkmUrl = "$hisenseUrl$nextPath"
            val conn2 = URL(dkmUrl).openConnection() as HttpURLConnection
            conn2.requestMethod = "GET"
            conn2.setRequestProperty("Cookie", "PHPSESSID=$cookie")

            val dkmHtml = conn2.inputStream.bufferedReader().readText()
            val dkmDoc = Jsoup.parse(dkmHtml)

            // 4. Parse all the data from the details page
            val inputs = dkmDoc.select(".filter-section input")
            val schoolInfo = HisenseSchoolInfo(
                npsn = inputs.getOrNull(0)?.`val`(),
                nama = inputs.getOrNull(1)?.`val`(),
                alamat = inputs.getOrNull(2)?.`val`(),
                provinsi = inputs.getOrNull(3)?.`val`(),
                kabupaten = inputs.getOrNull(4)?.`val`(),
                kecamatan = inputs.getOrNull(5)?.`val`(),
                kelurahanDesa = inputs.getOrNull(6)?.`val`(),
                jenjang = inputs.getOrNull(7)?.`val`(),
                bentuk = inputs.getOrNull(8)?.`val`(),
                sekolah = inputs.getOrNull(9)?.`val`(),
                formal = inputs.getOrNull(10)?.`val`(),
                pic = inputs.getOrNull(11)?.`val`(),
                telpPic = inputs.getOrNull(12)?.`val`(),
                resiPengiriman = inputs.getOrNull(13)?.`val`(),
                serialNumber = inputs.getOrNull(14)?.`val`(),
                status = inputs.getOrNull(15)?.`val`()
            )

            val images = dkmDoc.select("#flush-collapseTwo img").associate {
                val label = it.closest(".card")?.select("label > b")?.text()?.trim() ?: ""
                label to it.attr("src")
            }

            val processHistory = dkmDoc.select("#flush-collapseOne tbody tr").map {
                val columns = it.select("td")
                HisenseProcessHistory(
                    tanggal = columns.getOrNull(0)?.text()?.trim(),
                    status = columns.getOrNull(1)?.text()?.trim(),
                    keterangan = columns.getOrNull(2)?.text()?.trim()
                )
            }

            val queryString = nextPath.substringAfter('?')
            val queryParams = queryString.split("&").map { val parts = it.split("="); parts[0] to if (parts.size > 1) URLDecoder.decode(parts[1], "UTF-8") else "" }.toMap()

            // 5. Assemble and return the final HisenseData object
            return HisenseData(
                isGreen = isGreen,
                schoolInfo = schoolInfo,
                images = images,
                processHistory = processHistory,
                q = queryParams["q"],
                npsn = schoolInfo.npsn,
                iprop = queryParams["iprop"],
                ikab = queryParams["ikab"],
                ikec = queryParams["ikec"],
                iins = queryParams["iins"],
                ijenjang = queryParams["ijenjang"],
                ibp = queryParams["ibp"],
                iss = queryParams["iss"],
                isf = queryParams["isf"],
                istt = queryParams["istt"],
                itgl = queryParams["itgl"],
                itgla = queryParams["itgla"],
                itgle = queryParams["itgle"],
                ipet = queryParams["ipet"],
                ihnd = queryParams["ihnd"],
                nextPath = "?$queryString"
            )

        } catch (e: Exception) {
            return HisenseData(isGreen = false, error = e.message)
        }
    }

    fun getIsGreen(npsn: String, cookie: String): HisenseData {
        try {
            if (cookie.isEmpty()) {
                return HisenseData(isGreen = false, error = "Cookie PHPSESSID diperlukan")
            }

            // 1. Fetch the initial monitoring page
            val initialUrl = "${hisenseUrl}r_monitoring.php?inpsn=$npsn"
            val conn1 = URL(initialUrl).openConnection() as HttpURLConnection
            conn1.requestMethod = "GET"
            conn1.setRequestProperty("Cookie", "PHPSESSID=$cookie")

            val html1 = conn1.inputStream.bufferedReader().readText()
            val doc1 = Jsoup.parse(html1)

            val firstRow = doc1.select("#main-content > div > div > div > div.table-container > div > table > tbody tr").first()
            val firstTdStyle = firstRow?.select("td")?.first()?.attr("style") ?: ""
            val isGreen = firstTdStyle.contains("color:green")

            return HisenseData(
                isGreen = isGreen,
            )

        } catch (e: Exception) {
            return HisenseData(isGreen = false, error = e.message)
        }
    }

    @Throws(IOException::class)
    fun makeHisenseRequest(path: String, cookie: String): HisenseResponse {
        val url = "$hisenseUrl$path"
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Cookie", "PHPSESSID=$cookie")

        val status = conn.responseCode
        val body = (if (status in 200..299) conn.inputStream else conn.errorStream)
            .bufferedReader().readText()
        val contentType = conn.contentType ?: "text/html"

        return HisenseResponse(body, status, contentType)
    }
}