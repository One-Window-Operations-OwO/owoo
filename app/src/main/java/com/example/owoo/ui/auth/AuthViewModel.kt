package com.example.owoo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.owoo.data.LoginRequest
import com.example.owoo.data.ValidationRequest
import com.example.owoo.network.RetrofitInstance
import com.example.owoo.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthState {
    LOADING,
    LOGGED_IN,
    LOGGED_OUT
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitInstance.api

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState = _authState.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName = _userName.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val cookie = sessionManager.getCookie()
            if (cookie == null) {
                _authState.value = AuthState.LOGGED_OUT
                return@launch
            }

            try {
                val response = apiService.validateCookie(ValidationRequest(cookie))
                if (response.isSuccessful && response.body()?.valid == true) {
                    _userName.value = response.body()?.name ?: ""
                    _authState.value = AuthState.LOGGED_IN
                } else {
                    // Cookie is invalid, try to re-login
                    relogin()
                }
            } catch (e: Exception) {
                // Network error, try to re-login
                relogin()
            }
        }
    }

    private fun relogin() {
        viewModelScope.launch {
            val username = sessionManager.getUsername()
            val password = sessionManager.getPassword()

            if (username != null && password != null) {
                login(username, password, isRelogin = true)
            } else {
                _authState.value = AuthState.LOGGED_OUT
                sessionManager.clearData() // Clear any invalid partial data
            }
        }
    }

    fun login(username: String, password: String, isRelogin: Boolean = false) {
        viewModelScope.launch {
            if (!isRelogin) {
                _authState.value = AuthState.LOADING
            }
            _errorMessage.value = ""

            try {
                val response = apiService.login(LoginRequest(username, password))
                val responseBody = response.body()

                if (response.isSuccessful && responseBody != null) {
                    if (responseBody.phpsessid != null) {
                        // Login Success
                        val newCookie = responseBody.phpsessid
                        sessionManager.saveAuthData(newCookie, username, password)

                        // Optimistic UI update
                        _authState.value = AuthState.LOGGED_IN
                        validateAndFetchFullName(newCookie, fallbackName = username)
                    } else {
                        // Login Failed - Use error message from API
                        _errorMessage.value = responseBody.error ?: "Login failed: Unknown error"
                        _authState.value = AuthState.LOGGED_OUT
                    }
                } else {
                    // Non-200 response or other issue
                    _errorMessage.value = "Login failed: Invalid response from server"
                    _authState.value = AuthState.LOGGED_OUT
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: Network error"
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    private fun validateAndFetchFullName(cookie: String, fallbackName: String) {
        // Immediately update with the stored username
        _userName.value = fallbackName

        // Launch a separate coroutine to fetch the full name without blocking the UI
        viewModelScope.launch {
            try {
                val response = apiService.validateCookie(ValidationRequest(cookie))
                if (response.isSuccessful && response.body()?.valid == true) {
                    // Update with the full name from API if successful
                    _userName.value = response.body()?.name ?: fallbackName
                }
            } catch (e: Exception) {
                // Network might fail, but it's okay. The UI already shows the fallback name.
            }
        }
    }

    fun logout() {
        sessionManager.clearData()
        _authState.value = AuthState.LOGGED_OUT
        _userName.value = ""
    }
}
