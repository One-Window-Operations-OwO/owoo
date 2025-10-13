package com.example.owoo.ui.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.foundation.lazy.LazyColumn

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

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let { homeViewModel.onFileSelected(it) }
        }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                userName = userName,
                onLogoutClicked = onLogoutClicked,
                onPickFileClicked = { filePickerLauncher.launch("application/json") },
                onFetchDataClicked = { homeViewModel.fetchPendingRows(userName) },
                isFetchDataEnabled = uiState.serviceAccountJson != null && !uiState.isLoading,
                onFetchDetailClicked = { homeViewModel.fetchFirstRowDetails() },
                isFetchDetailEnabled = uiState.pendingRows.isNotEmpty() && !uiState.isLoading
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Home (${uiState.pendingRows.size} pending)") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator()
                }

                uiState.rowDetails?.let { details ->
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    val hisenseJson = gson.toJson(details.hisenseData)
                    val datadikJson = gson.toJson(details.datadikData)

                    LazyColumn {
                        item {
                            Text("Hisense Data", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = hisenseJson,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            Text("Datadik Data", style = MaterialTheme.typography.headlineSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = datadikJson,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(8.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                if (uiState.pendingRows.isNotEmpty() && uiState.rowDetails == null && !uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    val firstRow = uiState.pendingRows.first()
                    val header = uiState.headerRow
                    val jsonMap = header.zip(firstRow).toMap()
                    val jsonPretty = try {
                        org.json.JSONObject(jsonMap).toString(4)
                    } catch (e: Exception) {
                        jsonMap.toString()
                    }

                    Text(
                        text = "Pending Row:\n$jsonPretty",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
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
    onFetchDataClicked: () -> Unit,
    isFetchDataEnabled: Boolean,
    onFetchDetailClicked: () -> Unit,
    isFetchDetailEnabled: Boolean
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = userName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onPickFileClicked) {
                Text("Pilih File")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onFetchDataClicked, enabled = isFetchDataEnabled) {
                Text("Tarik Data")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onFetchDetailClicked, enabled = isFetchDetailEnabled) {
                Text("Tarik Detail")
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = onLogoutClicked) {
                Text("Logout")
            }
        }
    }
}
