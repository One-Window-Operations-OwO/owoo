package com.example.owoo.network

import com.example.owoo.data.datadik.DatadikData
import com.example.owoo.data.datadik.PtkItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

object DatadikService {

    private const val datadikUrl = "https://datadik.kemendikdasmen.go.id/"
    private const val djanCook = "eyJzZXNzaWQiOiIxNzNGMzk5Ni1FRDM3LTRENDktODQ4Ny01MzREMENFNTM0MjEiLCJwaWQiOiJBQkQ0MDJENS0xQTI4LTQ2OTUtOEI1OS01RDM5MzFCQUNDRjciLCJ1c2VyIjoiYm9zZGFrZmlzaWtzbWFAZ21haWwuY29tIiwidHlwZSI6IjUiLCJuYW1hIjoiTnVydWwgTWFoZnVkaSAoRGl0LiBTTUEpIiwibGVtYmFnYSI6bnVsbCwid2lsYXlhaCI6IjAwMDAwMCIsInNla29sYWgiOm51bGwsImV4cCI6MH0%3D"

    suspend fun getDatadik(q: String): DatadikData {
        val refSpUrl = "${datadikUrl}refsp/q/173F3996-ED37-4D49-8487-534D0CE53421"
        val initialData = fetchInitialDatadikData(refSpUrl, q)

        val ptkUrl = "${datadikUrl}ma74/sekolahptk/${initialData[0][0]}/1"
        val ptkData = fetchPtkData(ptkUrl)

        val kepalaSekolah = ptkData.find { it.jabatan_ptk == "Kepala Sekolah" }?.nama ?: ""

        return DatadikData(
            id = initialData[0][0],
            name = initialData[0][1],
            address = initialData[0][3],
            kecamatan = initialData[0][4],
            kabupaten = initialData[0][5],
            provinsi = initialData[0][6],
            kepalaSekolah = kepalaSekolah,
            ptk = ptkData
        )
    }

    private fun fetchInitialDatadikData(url: String, q: String): List<List<String>> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        conn.setRequestProperty("Cookie", "BNI_persistence=loBsixwO2PznD3anBe3G7iGGItiIlJeSoqOEU0WHOQeppAcllasC4GWDdG2PDrlBr62YmZNZyRkI6eBCgGkbyg==; djanCook=$djanCook")

        val postData = "q=${java.net.URLEncoder.encode(q, "UTF-8")}"
        conn.outputStream.use { it.write(postData.toByteArray()) }

        val json = conn.inputStream.bufferedReader().readText()
        val type = object : TypeToken<List<List<String>>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun fetchPtkData(url: String): List<PtkItem> {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val json = conn.inputStream.bufferedReader().readText()
        val type = object : TypeToken<List<PtkItem>>() {}.type
        return Gson().fromJson(json, type)
    }
}
