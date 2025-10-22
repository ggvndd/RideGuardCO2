package com.capstoneco2.rideguard.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.capstoneco2.rideguard.viewmodel.FirebaseTestViewModel
import com.capstoneco2.rideguard.viewmodel.FirebaseTestState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseTestScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: FirebaseTestViewModel = hiltViewModel()
    val crashIdTestResult by viewModel.crashIdTestResult.collectAsState()
    val rideGuardIdTestResult by viewModel.rideGuardIdTestResult.collectAsState()
    val fcmTokenTestResult by viewModel.fcmTokenTestResult.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("CrashId", "RideGuardId", "FCM Tokens")
    
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = "Firebase Testing",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tab Content
        when (selectedTab) {
            0 -> CrashIdTestTab(viewModel)
            1 -> RideGuardIdTestTab(viewModel)
            2 -> FCMTokenTestTab(viewModel, fcmTokenTestResult, context)
        }
    }
}

@Composable
fun CrashIdTestTab(viewModel: FirebaseTestViewModel) {
    val state by viewModel.state.collectAsState()
    var crashId by remember { mutableStateOf("CRASH_001") }
    var reporterId by remember { mutableStateOf("REPORTER_001") }
    var userId by remember { mutableStateOf("user123") }
    var latitude by remember { mutableStateOf("14.5995") }
    var longitude by remember { mutableStateOf("120.9842") }

    CrashIdTestSection(
        state = state,
        onReportCrash = { cId, rId, uId, lat, lng ->
            viewModel.reportCrash(cId, rId, uId, lat.toDouble(), lng.toDouble())
        },
        onGetCrash = { cId -> viewModel.getCrash(cId) },
        onMarkProcessing = { cId -> viewModel.markCrashAsProcessing(cId) },
        onGetUnprocessed = { viewModel.getUnprocessedCrashes() },
        onClearResults = { viewModel.clearResults() }
    )
}

@Composable
fun RideGuardIdTestTab(viewModel: FirebaseTestViewModel) {
    val state by viewModel.state.collectAsState()

    RideGuardIdTestSection(
        state = state,
        onConnectDevice = { deviceId, userId -> viewModel.connectUserToDevice(deviceId, userId) },
        onGetConnection = { deviceId -> viewModel.getRideGuardConnection(deviceId) },
        onGetUserDevices = { userId -> viewModel.getUserDevices(userId) },
        onDisconnectDevice = { deviceId -> viewModel.disconnectDevice(deviceId) },
        onClearResults = { viewModel.clearResults() }
    )
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

@Composable
fun FCMTokenTestTab(
    viewModel: FirebaseTestViewModel,
    testResult: String,
    context: Context
) {
    var userDisplayName by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var fcmToken by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "FCM Token Multi-User Testing",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Test FCM token management for multiple users on a single device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "User Input",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = userId,
                        onValueChange = { userId = it },
                        label = { Text("User ID") },
                        placeholder = { Text("e.g., user123") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = userDisplayName,
                        onValueChange = { userDisplayName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g., John Doe") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = fcmToken,
                        onValueChange = { fcmToken = it },
                        label = { Text("FCM Token") },
                        placeholder = { Text("Generate or paste FCM token") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { 
                            fcmToken = "mock_fcm_token_${System.currentTimeMillis()}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Generate Mock FCM Token")
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "FCM Token Operations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.testAddFCMToken(userId, userDisplayName, fcmToken, context)
                            },
                            enabled = userId.isNotBlank() && userDisplayName.isNotBlank() && fcmToken.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Add User")
                        }
                        
                        Button(
                            onClick = {
                                viewModel.testSetPrimaryUser(userId, context)
                            },
                            enabled = userId.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Set Primary")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.testSwitchUser(userId, context)
                            },
                            enabled = userId.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Switch User")
                        }
                        
                        Button(
                            onClick = {
                                viewModel.testRemoveUser(userId, context)
                            },
                            enabled = userId.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Remove User")
                        }
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Device Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.testGetDeviceUsers(context)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Get Device Users")
                        }
                        
                        Button(
                            onClick = {
                                viewModel.testGetPrimaryUser(context)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Get Primary User")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            viewModel.testGetUserCount(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Get User Count")
                    }
                }
            }
        }
        
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Test Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 300.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        if (testResult.isBlank()) {
                            Text(
                                text = "No test results yet. Run a test to see results here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn {
                                item {
                                    Text(
                                        text = testResult,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.clearFCMTokenTestResult() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Clear Results")
                    }
                }
            }
        }
    }
}