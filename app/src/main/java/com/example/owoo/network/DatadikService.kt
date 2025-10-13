package com.example.owoo.network

import com.example.owoo.data.datadik.DatadikData
import com.example.owoo.data.datadik.Ptk
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.HttpURLConnection
import java.net.URL

object DatadikService {

    private const val datadikUrl = "https://datadik.kemendikdasmen.go.id/"
    // IMPORTANT: This cookie is hardcoded and might expire.
    // In the TypeScript code, this was loaded from environment variables.
    // Consider a more robust way to manage this authentication token.
    private const val djanCook = "eyJzZXNzaWQiOiIxNzNGMzk5Ni1FRDM3LTRENDktODQ4Ny01MzREMENFNTM0MjEiLCJwaWQiOiJBQkQ0MDJENS0xQTI4LTQ2OTUtOEI1OS01RDM5MzFCQUNDRjciLCJ1c2VyIjoiYm9zZGFrZmlzaWtzbWFAZ21haWwuY29tIiwidHlwZSI6IjUiLCJuYW1hIjoiTnVydWwgTWFoZnVkaSAoRGl0LiBTTUEpIiwibGVtYmFnYSI6bnVsbCwid2lsYXlhaCI6IjAwMDAwMCIsInNla29sYWgiOm51bGwsImV4cCI6MH0%3D"

    fun getDatadik(q: String): DatadikData {
        try {
            // First fetch: Get initial school data
            val refSpUrl = "${datadikUrl}refsp/q/173F3996-ED37-4D49-8487-534D0CE53421"
            val conn1 = URL(refSpUrl).openConnection() as HttpURLConnection
            conn1.requestMethod = "POST"
            conn1.doOutput = true
            conn1.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn1.setRequestProperty("Cookie", "djanCook=$djanCook")

            val postData = "q=${java.net.URLEncoder.encode(q, "UTF-8")}"
            conn1.outputStream.use { it.write(postData.toByteArray()) }

            val json1 = conn1.inputStream.bufferedReader().readText()
            val type1 = object : TypeToken<List<List<String>>>() {}.type
            val initialData: List<List<String>> = Gson().fromJson(json1, type1)

            if (initialData.isEmpty()) {
                return DatadikData(error = "Data not found for q: $q")
            }

            val id = initialData[0][0]
            val name = initialData[0][1]
            val address = initialData[0][3]
            val kecamatan = initialData[0][4]
            val kabupaten = initialData[0][5]
            val provinsi = initialData[0][6]

            // Second fetch: Get PTK data
            val ptkUrl = "${datadikUrl}ma74/sekolahptk/$id/1"
            val conn2 = URL(ptkUrl).openConnection() as HttpURLConnection
            conn2.requestMethod = "GET"
            conn2.setRequestProperty("Cookie", "djanCook=$djanCook")

            val json2 = conn2.inputStream.bufferedReader().readText()
            val type2 = object : TypeToken<List<Ptk>>() {}.type
            val ptkData: List<Ptk> = Gson().fromJson(json2, type2)

            val kepalaSekolah = ptkData.find { it.jabatanPtk == "Kepala Sekolah" }?.nama ?: ""

            return DatadikData(
                id = id,
                name = name,
                address = address,
                kecamatan = kecamatan,
                kabupaten = kabupaten,
                provinsi = provinsi,
                kepalaSekolah = kepalaSekolah,
                ptk = ptkData
            )
        } catch (e: Exception) {
            return DatadikData(error = e.message)
        }
    }
}
