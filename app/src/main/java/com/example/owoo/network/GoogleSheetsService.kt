package com.example.owoo.network

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

object GoogleSheetsService {

    private const val SPREADSHEET_ID = "1rtLbHvl6qpQiRat4h79vvLlUAqq15dc1b7p81zaQoqM"
    private const val SHEET_NAME = "'Lembar Kerja'"

    private fun getSheetsService(jsonContent: String): Sheets {
        val credentials = GoogleCredentials.fromStream(jsonContent.byteInputStream())
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("Owoo")
            .build()
    }

    fun getPendingRows(jsonContent: String, verifierName: String): Pair<List<String>, List<List<String>>> {
        val service = getSheetsService(jsonContent)
        val range = "${SHEET_NAME}!A:Y"

        val response = service.spreadsheets().values().get(SPREADSHEET_ID, range).execute()
        val values = response.getValues() ?: return Pair(emptyList(), emptyList())

        if (values.size < 3) {
            return Pair(emptyList(), emptyList())
        }

        val headerRow = values[2].map { it.toString() }
        val verifikatorCol = headerRow.indexOf("VERIFIKATOR")
        val statusCol = headerRow.indexOf("STATUS (DITERIMA/DITOLAK)")

        if (verifikatorCol == -1 || statusCol == -1) {
            throw IllegalStateException("Kolom VERIFIKATOR atau STATUS tidak ditemukan di Sheet.")
        }

        val pendingRows = values
            .asSequence()
            .drop(3)
            .filter { row ->
                val verifier = row.getOrNull(verifikatorCol)?.toString() ?: ""
                val status = row.getOrNull(statusCol)?.toString()?.trim() ?: ""
                verifier == verifierName && status.isEmpty()
            }
            .map { row -> row.map { it.toString() } }
            .toList()

        return Pair(headerRow, pendingRows)
    }
}
