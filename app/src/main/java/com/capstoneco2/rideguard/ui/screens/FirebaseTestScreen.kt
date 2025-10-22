package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstoneco2.rideguard.data.CrashId
import com.capstoneco2.rideguard.data.RideGuardId
import com.capstoneco2.rideguard.viewmodel.FirebaseTestViewModel
import com.capstoneco2.rideguard.viewmodel.FirebaseTestState
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTestScreen(
    onBackClick: () -> Unit = {},
    viewModel: FirebaseTestViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("CrashId Tests", "RideGuardId Tests")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBackClick) {
                Text("← Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Firebase Collection Testing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTab) {
            0 -> CrashIdTestSection(
                state = state,
                onReportCrash = { crashId, reporterId, userId, lat, lng ->
                    coroutineScope.launch {
                        viewModel.reportCrash(crashId, reporterId, userId, lat, lng)
                    }
                },
                onGetCrash = { crashId ->
                    coroutineScope.launch {
                        viewModel.getCrash(crashId)
                    }
                },
                onMarkProcessing = { crashId ->
                    coroutineScope.launch {
                        viewModel.markCrashAsProcessing(crashId)
                    }
                },
                onGetUnprocessed = {
                    coroutineScope.launch {
                        viewModel.getUnprocessedCrashes()
                    }
                },
                onClearResults = { viewModel.clearResults() }
            )
            1 -> RideGuardIdTestSection(
                state = state,
                onConnectDevice = { deviceId, userId ->
                    coroutineScope.launch {
                        viewModel.connectUserToDevice(deviceId, userId)
                    }
                },
                onGetConnection = { deviceId ->
                    coroutineScope.launch {
                        viewModel.getRideGuardConnection(deviceId)
                    }
                },
                onGetUserDevices = { userId ->
                    coroutineScope.launch {
                        viewModel.getUserDevices(userId)
                    }
                },
                onDisconnectDevice = { deviceId ->
                    coroutineScope.launch {
                        viewModel.disconnectDevice(deviceId)
                    }
                },
                onClearResults = { viewModel.clearResults() }
            )
        }
    }
}

@Composable
fun CrashIdTestSection(
    state: FirebaseTestState,
    onReportCrash: (String, String, String, Double, Double) -> Unit,
    onGetCrash: (String) -> Unit,
    onMarkProcessing: (String) -> Unit,
    onGetUnprocessed: () -> Unit,
    onClearResults: () -> Unit
) {
    var crashId by remember { mutableStateOf("CRASH_001") }
    var reporterId by remember { mutableStateOf("DEVICE_001") }
    var userId by remember { mutableStateOf("user123") }
    var latitude by remember { mutableStateOf("-6.2088") }
    var longitude by remember { mutableStateOf("106.8456") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "CrashId Collection Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Input fields
                OutlinedTextField(
                    value = crashId,
                    onValueChange = { crashId = it },
                    label = { Text("Crash ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reporterId,
                    onValueChange = { reporterId = it },
                    label = { Text("Reporter Device ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onReportCrash(
                                crashId,
                                reporterId,
                                userId,
                                latitude.toDoubleOrNull() ?: 0.0,
                                longitude.toDoubleOrNull() ?: 0.0
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Report Crash")
                    }

                    Button(
                        onClick = { onGetCrash(crashId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Get Crash")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onMarkProcessing(crashId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Mark Processing")
                    }

                    Button(
                        onClick = { onGetUnprocessed() },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Get Unprocessed")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results section
        TestResultsSection(
            state = state,
            onClearResults = onClearResults
        )
    }
}

@Composable
fun RideGuardIdTestSection(
    state: FirebaseTestState,
    onConnectDevice: (String, String) -> Unit,
    onGetConnection: (String) -> Unit,
    onGetUserDevices: (String) -> Unit,
    onDisconnectDevice: (String) -> Unit,
    onClearResults: () -> Unit
) {
    var deviceId by remember { mutableStateOf("DEVICE_001") }
    var userId by remember { mutableStateOf("user123") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "RideGuardId Collection Test",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Input fields
                OutlinedTextField(
                    value = deviceId,
                    onValueChange = { deviceId = it },
                    label = { Text("RideGuard Device ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it },
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConnectDevice(deviceId, userId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Connect Device")
                    }
                    
                    Button(
                        onClick = { onGetConnection(deviceId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Get Connection")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onGetUserDevices(userId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Get User Devices")
                    }
                    
                    Button(
                        onClick = { onDisconnectDevice(deviceId) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isLoading
                    ) {
                        Text("Disconnect Device")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Results section
        TestResultsSection(
            state = state,
            onClearResults = onClearResults
        )
    }
}

@Composable
fun TestResultsSection(
    state: FirebaseTestState,
    onClearResults: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Test Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(onClick = onClearResults) {
                    Text("Clear")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (state.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Loading...")
                }
            }
            
            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Error: $error",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            state.results.forEach { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}