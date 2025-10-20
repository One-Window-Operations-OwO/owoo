package com.example.owoo.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    onLogoutClicked: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by homeViewModel.uiState.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf<String?>(null) }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { homeViewModel.onFileSelected(it) }
        }
    )

    if (showConfirmationDialog != null) {
        val action = showConfirmationDialog!!
        val isTolak = action == "tolak"
        val actionText = if (isTolak) "menolak" else "menerima"

        AlertDialog(
            onDismissRequest = { showConfirmationDialog = null },
            title = { Text("Konfirmasi Tindakan") },
            text = {
                Column {
                    Text("Apakah Anda yakin ingin $actionText data ini?")
                    if (isTolak) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = uiState.rejectionReasonString,
                            onValueChange = { homeViewModel.updateRejectionReason(it) },
                            label = { Text("Alasan Penolakan") },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        homeViewModel.updateSheetAndProceed(action)
                        showConfirmationDialog = null
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = null }
                ) {
                    Text("Tidak")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = userName,
                onLogoutClicked = onLogoutClicked,
                onPickFileClicked = { filePickerLauncher.launch("application/json") },
                homeViewModel = homeViewModel
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${uiState.pendingRows.size} Data lagi") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        if (uiState.rowDetails != null) {
                            val isTolak = uiState.rejectionMessages.isNotEmpty()
                            val actionText = if (isTolak) "Tolak" else "Terima"
                            val action = if (isTolak) "tolak" else "terima"

                            Button(
                                onClick = { showConfirmationDialog = action },
                                enabled = !uiState.isLoading,
                                colors = if (isTolak) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                } else {
                                    ButtonDefaults.buttonColors()
                                }
                            ) {
                                Text(actionText)
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(top=6.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }

                uiState.rowDetails?.let { details ->
                    com.example.owoo.ui.feature.HisenseDetailScreen(homeState = uiState, viewModel = homeViewModel)
                }

                if (uiState.rowDetails == null && !uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { homeViewModel.fetchPendingRows(userName) },
                        enabled = uiState.serviceAccountJson != null
                    ) {
                        Text("Tarik Data")
                    }

                    if (uiState.pendingRows.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { homeViewModel.fetchFirstRowDetails() }
                        ) {
                            Text("Mulai Verval")
                        }
                    }
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AppDrawer(
    userName: String,
    onLogoutClicked: () -> Unit,
    onPickFileClicked: () -> Unit,
    homeViewModel: HomeViewModel
) {
    val uiState by homeViewModel.uiState.collectAsState()

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = userName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.rowDetails != null) {
                Button(onClick = { homeViewModel.stopVerval() }) {
                    Text("Berhenti Verval")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPickFileClicked) {
                Text("Pilih File")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogoutClicked) {
                Text("Logout")
            }
        }
    }
}
