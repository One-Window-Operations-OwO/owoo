package com.example.owoo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.owoo.ui.auth.AuthViewModel
import com.example.owoo.ui.auth.AuthState
import com.example.owoo.ui.auth.LoginScreen
import com.example.owoo.ui.home.HomeScreen
import com.example.owoo.ui.theme.OwooTheme

import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OwooTheme {
                OwooApp()
            }
        }
    }
}

@Composable
fun OwooApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (authState) {
            AuthState.LOADING -> {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            }
            AuthState.LOGGED_OUT -> {
                LoginScreen(errorMessage = errorMessage) {
                    username, password -> authViewModel.login(username, password)
                }
            }
            AuthState.LOGGED_IN -> {
                HomeScreen(userName = userName, onLogoutClicked = { authViewModel.logout() })
            }
        }
    }
}
