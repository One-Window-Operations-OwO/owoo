package com.example.owoo.ui.home

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.owoo.network.GoogleSheetsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.owoo.data.datadik.DatadikData
import com.example.owoo.data.hisense.HisenseData
import com.example.owoo.network.DatadikService
import com.example.owoo.network.HisenseService
import com.example.owoo.util.CacheManager
import com.example.owoo.util.CachedData
import com.example.owoo.util.SessionManager

data class RowDetails(
    val hisenseData: HisenseData,
    val datadikData: DatadikData
)

data class HomeState(
    val serviceAccountJson: String? = null,
    val headerRow: List<String> = emptyList(),
    val pendingRows: List<List<String>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val rowDetails: RowDetails? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val cacheManager = CacheManager(application)
    private val sessionManager = SessionManager(application)
    private val _uiState = MutableStateFlow(HomeState())
    val uiState = _uiState.asStateFlow()

    init {
        val cachedData = cacheManager.loadPendingRows()
        val savedJson = sessionManager.getServiceAccountJson()
        if (cachedData != null) {
            _uiState.value = _uiState.value.copy(headerRow = cachedData.header, pendingRows = cachedData.rows, serviceAccountJson = savedJson)
        } else {
            _uiState.value = _uiState.value.copy(serviceAccountJson = savedJson)
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonContent = getApplication<Application>().contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                if (jsonContent != null) {
                    sessionManager.saveServiceAccountJson(jsonContent)
                    _uiState.value = _uiState.value.copy(serviceAccountJson = jsonContent, errorMessage = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun fetchPendingRows(verifierName: String) {
        val json = _uiState.value.serviceAccountJson ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, pendingRows = emptyList())
            try {
                val (header, rows) = withContext(Dispatchers.IO) {
                    GoogleSheetsService.getPendingRows(json, verifierName)
                }
                _uiState.value = _uiState.value.copy(headerRow = header, pendingRows = rows, isLoading = false)
                cacheManager.savePendingRows(CachedData(header, rows))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
                cacheManager.clearCache()
            }
        }
    }

    fun fetchRowDetails(row: List<String>, phpsessid: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val npsnIndex = _uiState.value.headerRow.indexOf("NPSN")
                if (npsnIndex == -1) {
                    throw IllegalStateException("Kolom NPSN tidak ditemukan.")
                }
                val npsn = row[npsnIndex]
                val hisenseData = withContext(Dispatchers.IO) {
                    HisenseService.getHisense(npsn, phpsessid)
                }
                val datadikData = withContext(Dispatchers.IO) {
                    DatadikService.getDatadik(hisenseData.q)
                }
                _uiState.value = _uiState.value.copy(
                    rowDetails = RowDetails(hisenseData, datadikData),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message, isLoading = false)
            }
        }
    }
}
