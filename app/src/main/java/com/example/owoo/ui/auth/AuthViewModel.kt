package com.example.owoo.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.owoo.network.HisenseAuthService
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

            val result = HisenseAuthService.validateHisenseCookie(cookie)
            if (result.isSuccess) {
                _userName.value = result.getOrNull() ?: sessionManager.getUsername() ?: ""
                _authState.value = AuthState.LOGGED_IN
            } else {
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
                sessionManager.clearData()
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
                val result = HisenseAuthService.loginHisense(username, password)

                if (result.isSuccess) {
                    val phpsessid = result.getOrNull()
                    if (phpsessid != null) {
                        sessionManager.saveAuthData(phpsessid, username, password)
                        // After successful login, validate the cookie to get the user's name
                        val validationResult = HisenseAuthService.validateHisenseCookie(phpsessid)
                        if(validationResult.isSuccess){
                            _userName.value = validationResult.getOrNull() ?: username
                        } else {
                            _userName.value = username // fallback to username
                        }
                        _authState.value = AuthState.LOGGED_IN
                    } else {
                        _errorMessage.value = "Login failed: No cookie received."
                        _authState.value = AuthState.LOGGED_OUT
                    }
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed: Unknown error"
                    _authState.value = AuthState.LOGGED_OUT
                }
            } catch (e: Exception) {
                _errorMessage.value = "Login failed: Network error"
                _authState.value = AuthState.LOGGED_OUT
            }
        }
    }

    fun logout() {
        sessionManager.clearData()
        _authState.value = AuthState.LOGGED_OUT
        _userName.value = ""
    }
}
