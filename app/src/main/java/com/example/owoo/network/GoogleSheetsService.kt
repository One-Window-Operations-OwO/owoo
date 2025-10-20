package com.example.owoo.network

import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.ExtendedValue
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.RowData
import com.google.api.services.sheets.v4.model.UpdateCellsRequest
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import java.lang.IllegalStateException

data class RowWithIndex(val rowIndex: Int, val rowData: List<String>)

object GoogleSheetsService {

    private const val SPREADSHEET_ID = "1rtLbHvl6qpQiRat4h79vvLlUAqq15dc1b7p81zaQoqM"
    private const val SHEET_NAME = "'Lembar Kerja'"
    private const val SHEET_ID_LEMBAR_KERJA = 340924294

    private lateinit var sheetsService: Sheets

    fun initialize(jsonContent: String) {
        if (::sheetsService.isInitialized) return

        val credentials = GoogleCredentials.fromStream(jsonContent.byteInputStream())
            .createScoped(listOf(SheetsScopes.SPREADSHEETS))

        sheetsService = Sheets.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        )
            .setApplicationName("Owoo")
            .build()
    }

    private fun getService(): Sheets {
        if (!::sheetsService.isInitialized) {
            throw IllegalStateException("GoogleSheetsService must be initialized before use.")
        }
        return sheetsService
    }

    fun getPendingRows(verifierName: String): Pair<List<String>, List<RowWithIndex>> {
        val service = getService()
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
            .mapIndexed { index, row -> Pair(index + 1, row) } // Get row index (1-based)
            .drop(3) // Drop header rows
            .filter { (_, row) ->
                val verifier = row.getOrNull(verifikatorCol)?.toString() ?: ""
                val status = row.getOrNull(statusCol)?.toString()?.trim() ?: ""
                verifier == verifierName && status.isEmpty()
            }
            .map { (index, row) -> RowWithIndex(index, row.map { it.toString() }) }
            .toList()

        return Pair(headerRow, pendingRows)
    }

    fun updateSheet(
        sheetId: String,
        rowIndex: Int,
        action: String,
        updates: Map<String, Any>?,
        customReason: String?
    ) {
        val service = getService()

        if (action == "update") {
            if (updates == null) {
                throw IllegalArgumentException("Parameter 'updates' dibutuhkan untuk action 'update'")
            }

            val data = mutableListOf<ValueRange>()
            updates.forEach { (column, value) ->
                val valueRange = ValueRange()
                    .setRange("'Lembar Kerja'!$column$rowIndex")
                    .setValues(listOf(listOf(value)))
                data.add(valueRange)
            }

            if (updates["N"] == "Sesuai") {
                data.add(
                    ValueRange()
                        .setRange("'Lembar Kerja'!I$rowIndex")
                        .setValues(listOf(listOf("=H$rowIndex")))
                )
            }

            if (customReason != null) {
                data.add(
                    ValueRange()
                        .setRange("'Lembar Kerja'!Y$rowIndex")
                        .setValues(listOf(listOf(customReason)))
                )
                data.add(
                    ValueRange()
                        .setRange("'Lembar Kerja'!X$rowIndex")
                        .setValues(listOf(listOf("DITOLAK")))
                )
            }

            val requestBody = BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(data)

            service.spreadsheets().values().batchUpdate(sheetId, requestBody).execute()

        } else {
            val requests = mutableListOf<Request>()

            val backgroundColor = if (action == "formatSkip") {
                Color().setRed(1f).setGreen(1f).setBlue(1f) // White
            } else {
                Color().setRed(0.85f).setGreen(0.85f).setBlue(0.85f) // Gray
            }

            requests.add(
                Request().setRepeatCell(
                    RepeatCellRequest()
                        .setRange(
                            GridRange()
                                .setSheetId(SHEET_ID_LEMBAR_KERJA)
                                .setStartRowIndex(rowIndex - 1)
                                .setEndRowIndex(rowIndex)
                                .setStartColumnIndex(9)
                                .setEndColumnIndex(24)
                        )
                        .setCell(
                            CellData().setUserEnteredFormat(
                                CellFormat().setBackgroundColor(backgroundColor)
                            )
                        )
                        .setFields("userEnteredFormat.backgroundColor")
                )
            )

            val cellValue = if (action == "formatSkip") {
                ExtendedValue()
            } else {
                ExtendedValue().setStringValue("HITAM")
            }

            requests.add(
                Request().setUpdateCells(
                    UpdateCellsRequest()
                        .setRange(
                            GridRange()
                                .setSheetId(SHEET_ID_LEMBAR_KERJA)
                                .setStartRowIndex(rowIndex - 1)
                                .setEndRowIndex(rowIndex)
                                .setStartColumnIndex(23)
                                .setEndColumnIndex(24)
                        )
                        .setRows(listOf(RowData().setValues(listOf(CellData().setUserEnteredValue(cellValue)))))
                        .setFields("userEnteredValue")
                )
            )

            val requestBody = BatchUpdateSpreadsheetRequest().setRequests(requests)
            service.spreadsheets().batchUpdate(sheetId, requestBody).execute()
        }
    }
}
