package com.example.owoo.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.owoo.data.datadik.DatadikData
import com.example.owoo.data.hisense.HisenseData
import com.example.owoo.network.DatadikService
import com.example.owoo.network.GoogleSheetsService
import com.example.owoo.network.HisenseAuthService
import com.example.owoo.network.HisenseService
import com.example.owoo.network.RowWithIndex
import com.example.owoo.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class RowDetails(
    val hisenseData: HisenseData,
    val datadikData: DatadikData
)

data class HomeState(
    val serviceAccountJson: String? = null,
    val headerRow: List<String> = emptyList(),
    val pendingRows: List<RowWithIndex> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rowDetails: RowDetails? = null,
    val evaluationForm: Map<String, String> = EvaluationConstants.defaultEvaluationValues,
    val rejectionMessages: List<String> = emptyList(),
    val rejectionReasonString: String = ""
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheManager = CacheManager(application)
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    init {
        val cachedData = cacheManager.loadPendingRows()
        val savedJson = sessionManager.getServiceAccountJson()
        if (savedJson != null) { 
            GoogleSheetsService.initialize(savedJson)
        }
        if (cachedData != null) {
            _uiState.value = _uiState.value.copy(headerRow = cachedData.header, pendingRows = cachedData.rows, serviceAccountJson = savedJson)
        } else {
            _uiState.value = _uiState.value.copy(serviceAccountJson = savedJson)
        }
    }

    fun updateRejectionReason(newReason: String) {
        _uiState.value = _uiState.value.copy(rejectionReasonString = newReason)
    }

    fun updateEvaluation(col: String, value: String) {
        val newForm = _uiState.value.evaluationForm.toMutableMap()
        newForm[col] = value

        val newRejectionMessages = mutableListOf<String>()
        newForm.forEach { (key, selectedValue) ->
            val defaultValue = EvaluationConstants.defaultEvaluationValues[key]
            if (selectedValue != defaultValue) {
                val message = RejectionHelper.getRejectionMessage(key, selectedValue)
                message?.let { newRejectionMessages.add(it) }
            }
        }
        val newReasonString = newRejectionMessages.joinToString(separator = "; ")

        _uiState.value = _uiState.value.copy(
            evaluationForm = newForm,
            rejectionMessages = newRejectionMessages,
            rejectionReasonString = newReasonString
        )
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonContent = getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                if (jsonContent != null) {
                    sessionManager.saveServiceAccountJson(jsonContent)
                    GoogleSheetsService.initialize(jsonContent)
                    _uiState.value = _uiState.value.copy(serviceAccountJson = jsonContent, errorMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun fetchPendingRows(verifierName: String, isRefetch: Boolean = false) {
        viewModelScope.launch {
            val loadingMessage = if (isRefetch) "Mengecek data yang dilewati..." else null
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = loadingMessage, pendingRows = emptyList())
            try {
                val (header, rows) = withContext(Dispatchers.IO) {
                    GoogleSheetsService.getPendingRows(verifierName)
                }

                if (rows.isEmpty() && isRefetch) {
                    _uiState.value = _uiState.value.copy(isLoading = false, rowDetails = null, errorMessage = "Semua data telah diverifikasi.")
                } else {
                    _uiState.value = _uiState.value.copy(headerRow = header, pendingRows = rows, isLoading = false)
                    cacheManager.savePendingRows(CachedData(header, rows))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
                cacheManager.clearCache()
            }
        }
    }

    fun fetchFirstRowDetails() {
        if (uiState.value.pendingRows.isNotEmpty()) {
            val phpsessid = sessionManager.getCookie()
            if (phpsessid != null) {
                fetchRowDetails(uiState.value.pendingRows.first(), phpsessid)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please login again.")
            }
        }
    }

    fun fetchRowDetails(rowWithIndex: RowWithIndex, phpsessid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                rowDetails = null, 
                errorMessage = null,
                rejectionMessages = emptyList(),
                rejectionReasonString = "" 
            )
            try {
                val npsnIndex = _uiState.value.headerRow.indexOf("NPSN")
                if (npsnIndex == -1) {
                    throw IllegalStateException("Kolom NPSN tidak ditemukan.")
                }
                val npsn = rowWithIndex.rowData[npsnIndex]

                val hisenseData = withContext(Dispatchers.IO) {
                    HisenseService.getHisense(npsn, phpsessid)
                }

                if (hisenseData.error != null) {
                    throw Exception("Hisense error: ${hisenseData.error}")
                }

                if (!hisenseData.isGreen) {
                    withContext(Dispatchers.IO) {
                        GoogleSheetsService.updateSheet(
                            sheetId = "1rtLbHvl6qpQiRat4h79vvLlUAqq15dc1b7p81zaQoqM",
                            rowIndex = rowWithIndex.rowIndex,
                            action = "formatSkipHitam",
                            updates = null,
                            customReason = null
                        )
                    }

                    val currentRows = _uiState.value.pendingRows
                    val newRows = currentRows.drop(1)
                    proceedToNextOrRefetch(newRows)
                    return@launch
                }

                val datadikData = withContext(Dispatchers.IO) {
                    DatadikService.getDatadik(npsn)
                }

                if (datadikData.error != null) {
                    throw Exception("Datadik error: ${datadikData.error}")
                }
                val installationDate = hisenseData.itgle ?: ""

                val newEvaluation = EvaluationConstants.defaultEvaluationValues.toMutableMap()
                newEvaluation["X"] = installationDate

                _uiState.value = _uiState.value.copy(
                    evaluationForm = newEvaluation
                )

                _uiState.value = _uiState.value.copy(
                    rowDetails = RowDetails(hisenseData, datadikData),
                    isLoading = false
                )
            }
            catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    private fun generateRejectionMessage(evaluationForm: Map<String, String>): String {
        val messages = mutableListOf<String>()
        evaluationForm.forEach { (key, selectedValue) ->
            val defaultValue = EvaluationConstants.defaultEvaluationValues[key]
            if (selectedValue != defaultValue) {
                val message = RejectionHelper.getRejectionMessage(key, selectedValue)
                message?.let { messages.add(it) }
            }
        }
        return messages.joinToString(separator = ", ")
    }

    fun updateSheetAndProceed(action: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val state = _uiState.value
                if (state.pendingRows.isEmpty()) throw IllegalStateException("Tidak ada data untuk diupdate.")

                val currentRow = state.pendingRows.first()
                val dkmData = state.rowDetails?.hisenseData ?: throw IllegalStateException("Tidak ada data DKM untuk diupdate.")
                val cookie = sessionManager.getCookie() ?: throw IllegalStateException("Cookie Hisense tidak ditemukan.")

                val validationResult = withContext(Dispatchers.IO) {
                    HisenseAuthService.validateHisenseCookie(cookie)
                }
                if (validationResult.isFailure) {
                    throw IllegalStateException("Cookie Hisense kadaluarsa atau tidak valid.")
                }

                val params = mutableMapOf<String, String>()
                params["q"] = dkmData.q ?: ""
                params["s"] = ""
                params["v"] = ""
                params["npsn"] = dkmData.npsn ?: ""
                params["iprop"] = dkmData.iprop ?: ""
                params["ikab"] = dkmData.ikab ?: ""
                params["ikec"] = dkmData.ikec ?: ""
                params["iins"] = dkmData.iins ?: ""
                params["ijenjang"] = dkmData.ijenjang ?: ""
                params["ibp"] = dkmData.ibp ?: ""
                params["iss"] = dkmData.iss ?: ""
                params["isf"] = dkmData.isf ?: ""
                params["istt"] = dkmData.istt ?: ""
                params["itgl"] = dkmData.itgl ?: ""
                params["itgla"] = dkmData.itgla ?: ""
                params["itgle"] = dkmData.itgle ?: ""
                params["ipet"] = dkmData.ipet ?: ""
                params["ihnd"] = dkmData.ihnd ?: ""

                when (action) {
                    "terima" -> {
                        params["s"] = "A"
                    }
                    "tolak" -> {
                        params["s"] = "R"
                        params["v"] = state.rejectionReasonString
                    }
                }

                val hisenseQueryString = params.map { (k, v) -> "${Uri.encode(k)}=${Uri.encode(v)}" }.joinToString("&")
                val hisensePath = "r_dkm_apr_p.php?$hisenseQueryString"

                val hisenseRes = withContext(Dispatchers.IO) {
                    HisenseService.makeHisenseRequest(hisensePath, cookie)
                }

                if (!hisenseRes.isSuccessful) {
                    throw Exception("Gagal update Hisense: ${hisenseRes.body}")
                }

                val generatedReason = generateRejectionMessage(state.evaluationForm)
                val customReason = state.rejectionReasonString
                val finalCustomReason = if (customReason.isNotEmpty() && customReason != generatedReason) {
                    customReason
                } else {
                    null
                }

                withContext(Dispatchers.IO) {
                    GoogleSheetsService.updateSheet(
                        sheetId = "1rtLbHvl6qpQiRat4h79vvLlUAqq15dc1b7p81zaQoqM",
                        rowIndex = currentRow.rowIndex,
                        action = "update",
                        updates = state.evaluationForm,
                        customReason = finalCustomReason
                    )
                }

                val newRows = state.pendingRows.drop(1)
                proceedToNextOrRefetch(newRows)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }

    fun skipAndProceed() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.pendingRows.size <= 1) {
                val newRows = state.pendingRows.drop(1)
                proceedToNextOrRefetch(newRows)
                return@launch
            }

            val newRows = state.pendingRows.drop(1) + state.pendingRows.first()
            proceedToNextOrRefetch(newRows)
        }
    }

    private fun proceedToNextOrRefetch(newRows: List<RowWithIndex>) {
        val verifierName = sessionManager.getUsername() ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = "User session not found.", isLoading = false)
            return
        }

        _uiState.value = _uiState.value.copy(pendingRows = newRows)
        cacheManager.savePendingRows(CachedData(_uiState.value.headerRow, newRows))

        if (newRows.isNotEmpty()) {
            val phpsessid = sessionManager.getCookie() ?: return
            fetchRowDetails(newRows.first(), phpsessid)
        } else {
            fetchPendingRows(verifierName, isRefetch = true)
        }
    }

    fun refreshCurrentRowDetails() {
        if (uiState.value.pendingRows.isNotEmpty()) {
            val phpsessid = sessionManager.getCookie()
            if (phpsessid != null) {
                fetchRowDetails(uiState.value.pendingRows.first(), phpsessid)
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please login again.")
            }
        }
    }

    fun stopVerval() {
        viewModelScope.launch {
            cacheManager.clearCache()
            _uiState.value = _uiState.value.copy(
                rowDetails = null,
                pendingRows = emptyList(),
                headerRow = emptyList(),
                errorMessage = null,
                isLoading = false
            )
        }
    }
}
