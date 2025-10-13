package com.example.owoo.network

import com.example.owoo.data.hisense.HisenseData
import com.example.owoo.data.hisense.HisenseProcessHistory
import com.example.owoo.data.hisense.HisenseSchoolInfo
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

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
            val onClickAttribute = firstRow?.attr("onclick")
            val urlMatch = onClickAttribute?.let { Regex("window.open('([^']*)')").find(it) }
            val nextPath = urlMatch?.groupValues?.getOrNull(1)

            val firstTdStyle = firstRow?.select("td")?.first()?.attr("style") ?: ""
            val isGreen = firstTdStyle.contains("color:green")

            if (nextPath == null) {
                // If there's no link, return only the color status
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
            val schoolInfoMap = dkmDoc.select(".filter-section input[type=\"text\"]").associate { el -> // Corrected escaped quote
                val label = el.previousElementSibling()?.text()?.trim()?.replace("Telp", "Telp PIC") ?: ""
                label to el.`val`()
            }
            val schoolInfo = HisenseSchoolInfo(
                npsn = schoolInfoMap["NPSN"],
                nama = schoolInfoMap["Nama"],
                alamat = schoolInfoMap["Alamat"],
                provinsi = schoolInfoMap["Provinsi"],
                kabupaten = schoolInfoMap["Kabupaten"],
                kecamatan = schoolInfoMap["Kecamatan"],
                kelurahanDesa = schoolInfoMap["Kelurahan/Desa"],
                jenjang = schoolInfoMap["Jenjang"],
                bentuk = schoolInfoMap["Bentuk"],
                sekolah = schoolInfoMap["Sekolah"],
                formal = schoolInfoMap["Formal"],
                pic = schoolInfoMap["PIC"],
                telpPic = schoolInfoMap["Telp PIC"],
                resiPengiriman = schoolInfoMap["Resi Pengiriman"],
                serialNumber = schoolInfoMap["Serial Number"],
                status = schoolInfoMap["Status"]
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
}