package com.example.owoo.network

import com.example.owoo.data.hisense.HisenseData
import com.example.owoo.data.hisense.ProcessHistoryItem
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL

object HisenseService {

    private const val hisenseUrl = "https://kemendikdasmen.hisense.id/"

    private fun fetchDkmData(url: String, cookie: String, isGreen: Boolean, nextPath: String): HisenseData {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Cookie", "PHPSESSID=$cookie")

        val dkmHtml = conn.inputStream.bufferedReader().readText()
        val dkmDoc = Jsoup.parse(dkmHtml)

        val queryString = nextPath.substring(nextPath.indexOf("?") + 1)
        val queryParams = queryString.split("&").map { it.split("=") }.associate { it[0] to it.getOrElse(1) { "" } }

        val schoolInfo = dkmDoc.select(".filter-section input[type=\"text\"]").associate { el ->
            val label = el.previousElementSibling()?.text()?.trim()?.replace("Telp", "Telp PIC") ?: ""
            val value = el.`val`()
            label to value
        }

        val images = dkmDoc.select("#flush-collapseTwo img").associate { el ->
            val label = el.closest(".card")?.select("label > b")?.text()?.trim() ?: ""
            val src = el.attr("src")
            label to src
        }

        val processHistory = dkmDoc.select("#flush-collapseOne tbody tr").map { row ->
            val columns = row.select("td")
            ProcessHistoryItem(
                tanggal = columns[0].text().trim(),
                status = columns[1].text().trim(),
                keterangan = columns[2].text().trim()
            )
        }

        return HisenseData(
            isGreen = isGreen,
            schoolInfo = schoolInfo,
            images = images,
            processHistory = processHistory,
            q = queryParams["q"] ?: "",
            npsn = schoolInfo["NPSN"] ?: "",
            iprop = queryParams["iprop"] ?: "",
            ikab = queryParams["ikab"] ?: "",
            ikec = queryParams["ikec"] ?: "",
            iins = queryParams["iins"] ?: "",
            ijenjang = queryParams["ijenjang"] ?: "",
            ibp = queryParams["ibp"] ?: "",
            iss = queryParams["iss"] ?: "",
            isf = queryParams["isf"] ?: "",
            istt = queryParams["istt"] ?: "",
            itgl = queryParams["itgl"] ?: "",
            itgla = queryParams["itgla"] ?: "",
            itgle = queryParams["itgle"] ?: "",
            ipet = queryParams["ipet"] ?: "",
            ihnd = queryParams["ihnd"] ?: ""
        )
    }

    suspend fun getHisense(npsn: String, cookie: String): HisenseData {
        if (cookie.isEmpty()) throw Exception("Cookie PHPSESSID diperlukan")

        val initialUrl = "${hisenseUrl}r_monitoring.php?inpsn=$npsn"
        val (isGreen, nextPath) = fetchInitialHisenseData(initialUrl, cookie)

        if (nextPath == null) {
            return HisenseData(isGreen, emptyMap(), emptyMap(), emptyList(), "", npsn, "", "", "", "", "", "", "", "", "", "", "", "", "", "")
        }

        val dkmUrl = "$hisenseUrl$nextPath"
        return fetchDkmData(dkmUrl, cookie, isGreen, nextPath)
    }

    private fun fetchInitialHisenseData(url: String, cookie: String): Pair<Boolean, String?> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Cookie", "PHPSESSID=$cookie")

        val html = conn.inputStream.bufferedReader().readText()
        val doc = Jsoup.parse(html)

        val firstRow = doc.select("#main-content > div > div > div > div.table-container > div > table > tbody tr").first()
        val onClickAttribute = firstRow?.attr("onclick")
        val urlMatch = onClickAttribute?.let { Regex("window.open('([^\"]*)')").find(it) }
        val nextPath = urlMatch?.groupValues?.get(1)

        val firstTdStyle = firstRow?.select("td")?.first()?.attr("style") ?: ""
        val isGreen = firstTdStyle.contains("color:green")

        return Pair(isGreen, nextPath)
    }
}
