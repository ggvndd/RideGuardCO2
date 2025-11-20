package com.capstoneco2.rideguard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiNetworkSpecifier
import android.net.NetworkRequest
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.data.EmergencyContactInfo
import com.capstoneco2.rideguard.ui.components.AddEmergencyContactDialog
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import com.capstoneco2.rideguard.viewmodel.EmergencyContactViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FieldValue



// Data classes for Wi-Fi management
data class WiFiNetwork(
    val ssid: String,
    val bssid: String,
    val isSecure: Boolean,
    val signalStrength: Int,
    val frequency: Int,
    val capabilities: String,
    val isConnected: Boolean = false,
    val scanResult: ScanResult? = null
)

data class WiFiConnectionState(
    val isScanning: Boolean = false,
    val availableNetworks: List<WiFiNetwork> = emptyList(),
    val connectedNetwork: WiFiNetwork? = null,
    val connectionStatus: WiFiConnectionStatus = WiFiConnectionStatus.DISCONNECTED
)

enum class WiFiConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED
}

// Enum for blackbox pairing states
enum class BlackboxPairingState {
    PAIRING_INPUT,
    WIFI_SCANNING,
    DEVICE_FOUND,
    DEVICE_NOT_FOUND,
    PAIRING_SUCCESS
}

@Composable
fun BlackboxScreen(
    onNavigateToPulsaBalance: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    emergencyContactViewModel: EmergencyContactViewModel = viewModel()
) {
    // Collect auth and emergency contact states
    val authState by authViewModel.authState.collectAsState()
    val emergencyContactState by emergencyContactViewModel.state.collectAsState()
    
    var showAddContactDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var contactToDelete by remember { mutableStateOf<EmergencyContactInfo?>(null) }
    
    // Load emergency contacts when screen loads
    LaunchedEffect(authState.user) {
        authState.user?.uid?.let { userId ->
            emergencyContactViewModel.loadEmergencyContacts(userId)
        }
    }
    val context = LocalContext.current
    var isDeviceOnline by remember { mutableStateOf(false) }
    var deletionRate by remember { mutableStateOf("3 Hours") }
    var showPairingDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("No Device Connected") }
    var connectedWifiName by remember { mutableStateOf("RideGuard_WiFi") }
    var isVisible by remember { mutableStateOf(false) }
    var wifiState by remember { mutableStateOf(WiFiConnectionState()) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasWifiPermissions by remember { mutableStateOf(false) }
    var batteryResult by remember { mutableStateOf<String?>(null) }
    var sdCardResult by remember { mutableStateOf<String?>(null) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false ||
                              permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        hasWifiPermissions = permissions[Manifest.permission.ACCESS_WIFI_STATE] ?: false &&
                           permissions[Manifest.permission.CHANGE_WIFI_STATE] ?: false
    }
    
    // Check permissions and get current Wi-Fi connection status
    LaunchedEffect(Unit) {
        // Check permissions
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        hasWifiPermissions = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.CHANGE_WIFI_STATE
        ) == PackageManager.PERMISSION_GRANTED
        
        // Get current Wi-Fi info if permissions are granted
        if (hasLocationPermission && hasWifiPermissions) {
            getCurrentWiFiInfo(context)?.let { currentNetwork ->
                deviceName = currentNetwork.ssid
                connectedWifiName = currentNetwork.ssid
                wifiState = wifiState.copy(
                    connectedNetwork = currentNetwork,
                    connectionStatus = WiFiConnectionStatus.CONNECTED
                )
                
                // Set device as online based on Wi-Fi connection
                isDeviceOnline = true
            } ?: run {
                isDeviceOnline = false
                deviceName = "No Device Connected"
            }
        }
    }
    
    // Trigger visibility animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                animationSpec = tween(600, easing = androidx.compose.animation.core.EaseOutQuart),
                initialOffsetY = { it / 3 }
            ),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            item {
                Spacer(modifier = Modifier.height(48.dp)) // Increased spacing from top more
                
                // Header
                MainHeader(
                    text = "RideGuard",
                    textAlign = TextAlign.Start,
                    color = com.capstoneco2.rideguard.ui.theme.Blue80
                )
                
                BodyText(
                    text = "This will set-up your rideguard device.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            item {
                // BlackBox Device Card
                BlackBoxDeviceCard(
                    deviceName = deviceName,
                    isOnline = isDeviceOnline,
                    onToggleStatus = { 
                        // Simple toggle based on Wi-Fi connectivity
                        // Device status is automatically updated through Wi-Fi monitoring
                    }
                )
            }
            
            item {
                // Change RideGuard Device Button
                SecondaryButton(
                    text = if (isDeviceOnline && deviceName != "No Device Connected") 
                           "Change RideGuard Device" 
                           else "Connect to RideGuard Device",
                    onClick = { showPairingDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BodyText(
                    text = if (isDeviceOnline && deviceName != "No Device Connected") 
                           "Note: You can only pair to one device" 
                           else "Note: Connect to your RideGuard device to get started",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                )
            }
            
            item {
                // RideGuard Details
                RideGuardDetailsSection(
                    onPulsaBalanceClick = onNavigateToPulsaBalance,
                    isDeviceConnected = isDeviceOnline,
                    batteryResult = batteryResult
                )
            }
            
            item {
                // Camera Settings
                CameraSettingsSection(
                    deletionRate = deletionRate,
                    onDeletionRateChange = { deletionRate = it },
                    sdCardResult = sdCardResult
                )
            }
            
            item {
                // Emergency Contacts
                EmergencyContactsSection(
                    emergencyContacts = emergencyContactState.contacts,
                    isLoading = emergencyContactState.isLoading,
                    onAddMoreUsers = {
                        if (emergencyContactState.contacts.size < 5) {
                            showAddContactDialog = true
                        }
                    },
                    onDeleteContact = { contact ->
                        contactToDelete = contact
                        showDeleteConfirmDialog = true
                    },
                    isDeviceConnected = isDeviceOnline,
                    onBatteryResult = { result -> batteryResult = result },
                    currentUserUid = authState.user?.uid,
                    currentUserName = authState.userProfile?.username ?: authState.user?.email?.substringBefore("@"),
                    sdCardResult = sdCardResult,
                    onSdCardResult = { result -> sdCardResult = result }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp)) // Space for bottom nav
            }
        }
        }
        
        // Add Emergency Contact Dialog
        if (showAddContactDialog) {
            authState.user?.uid?.let { currentUserUid ->
                AddEmergencyContactDialog(
                    currentUserUid = currentUserUid,
                    onDismiss = { showAddContactDialog = false },
                    onContactAdded = {
                        // No need to manually reload - ViewModel handles this automatically
                    },
                    viewModel = emergencyContactViewModel
                )
            }
        }
        
        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog && contactToDelete != null) {
            DeleteContactConfirmationDialog(
                contactName = contactToDelete!!.username,
                onConfirm = {
                    contactToDelete?.let { contact: EmergencyContactInfo ->
                        authState.user?.uid?.let { currentUserUid: String ->
                            emergencyContactViewModel.deleteEmergencyContact(
                                currentUserUid = currentUserUid,
                                contactUid = contact.uid
                            )
                        }
                    }
                    showDeleteConfirmDialog = false
                    contactToDelete = null
                },
                onDismiss = {
                    showDeleteConfirmDialog = false
                    contactToDelete = null
                }
            )
        }
        
        // Blackbox Pairing Dialog with Animation
        AnimatedVisibility(
            visible = showPairingDialog,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOutBack)
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                animationSpec = tween(200, easing = androidx.compose.animation.core.EaseInBack)
            )
        ) {
            BlackboxPairingDialog(
                context = context,
                wifiState = wifiState,
                hasLocationPermission = hasLocationPermission,
                hasWifiPermissions = hasWifiPermissions,
                onWifiStateChange = { newState -> wifiState = newState },
                onRequestPermissions = {
                    permissionLauncher.launch(arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.CHANGE_WIFI_STATE
                    ))
                },
                onDismiss = { showPairingDialog = false },
                onDeviceConnected = { wifiName ->
                    deviceName = wifiName
                    connectedWifiName = wifiName
                    isDeviceOnline = true
                }
            )
        }
    }
}



@Composable
fun BlackboxPairingDialog(
    context: Context,
    wifiState: WiFiConnectionState,
    hasLocationPermission: Boolean,
    hasWifiPermissions: Boolean,
    onWifiStateChange: (WiFiConnectionState) -> Unit,
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit,
    onDeviceConnected: (String) -> Unit
) {
    var dialogState by remember { mutableStateOf(BlackboxPairingState.PAIRING_INPUT) }
    var selectedNetwork by remember { mutableStateOf<WiFiNetwork?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Dialog Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .clickable(enabled = false) { }, // Prevent click through
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                when (dialogState) {
                    BlackboxPairingState.PAIRING_INPUT -> {
                        PairingInputContent(
                            context = context,
                            wifiState = wifiState,
                            hasLocationPermission = hasLocationPermission,
                            hasWifiPermissions = hasWifiPermissions,
                            onWifiStateChange = onWifiStateChange,
                            onRequestPermissions = onRequestPermissions,
                            onStartScanning = {
                                if (hasLocationPermission && hasWifiPermissions) {
                                    dialogState = BlackboxPairingState.WIFI_SCANNING
                                    coroutineScope.launch {
                                        startRealWiFiScan(context, onWifiStateChange)
                                    }
                                } else {
                                    onRequestPermissions()
                                }
                            }
                        )
                    }
                    BlackboxPairingState.WIFI_SCANNING -> {
                        WiFiScanningContent(
                            wifiState = wifiState,
                            onNetworkSelected = { network ->
                                selectedNetwork = network
                                // Comment out device type checking - treat any network as RideGuard device
                                // if (network.ssid.contains("RideGuard", ignoreCase = true) ||
                                //     network.ssid.contains("ra sah jaluk", ignoreCase = true)) {
                                    dialogState = BlackboxPairingState.DEVICE_FOUND
                                // } else {
                                //     dialogState = BlackboxPairingState.DEVICE_NOT_FOUND
                                // }
                            }
                        )
                    }
                    BlackboxPairingState.DEVICE_FOUND -> {
                        DeviceFoundContent(
                            network = selectedNetwork!!,
                            onConfirm = { 
                                // Simulate immediate connection success without actual Android Wi-Fi connection
                                onWifiStateChange(WiFiConnectionState(
                                    connectedNetwork = selectedNetwork!!.copy(isConnected = true),
                                    connectionStatus = WiFiConnectionStatus.CONNECTED
                                ))
                                dialogState = BlackboxPairingState.PAIRING_SUCCESS
                            }
                        )
                    }
                    BlackboxPairingState.DEVICE_NOT_FOUND -> {
                        DeviceNotFoundContent(
                            selectedNetwork = selectedNetwork,
                            onTryAgain = { 
                                selectedNetwork = null
                                dialogState = BlackboxPairingState.PAIRING_INPUT 
                                onWifiStateChange(wifiState.copy(availableNetworks = emptyList()))
                            }
                        )
                    }
                    BlackboxPairingState.PAIRING_SUCCESS -> {
                        PairingSuccessContent(
                            networkName = selectedNetwork?.ssid ?: "RideGuard Device",
                            onConfirm = {
                                onDeviceConnected(selectedNetwork?.ssid ?: "RideGuard Device")
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

// Real Wi-Fi utility functions
/*
 * REAL Wi-Fi Implementation for IoT Device Connection
 * 
 * This implementation provides actual Wi-Fi scanning and connection capabilities:
 * 
 * Features:
 * - Real network scanning using Android's WifiManager
 * - Actual network connection for both open and secured networks
 * - Permission handling for location and Wi-Fi access
 * - Support for Android 10+ (API 29+) with WifiNetworkSpecifier
 * - Backward compatibility with older Android versions
 * - Signal strength detection and sorting
 * - IoT device detection (networks starting with "RideGuard" or "ra sah jaluk")
 * 
 * Usage for IoT devices:
 * 1. Device creates a Wi-Fi hotspot (e.g., "RideGuard_Device_001" or "ra sah jaluk_001")
 * 2. App scans for available networks
 * 3. User selects the IoT device network
 * 4. App connects to the device network
 * 5. Device configuration can be performed over HTTP/TCP
 * 
 * Note: For secured IoT networks, you may need to:
 * - Prompt user for password
 * - Use default IoT device credentials
 * - Implement WPS or other pairing methods
 */
private fun getCurrentWiFiInfo(context: Context): WiFiNetwork? {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (!wifiManager.isWifiEnabled) return null
        
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        
        if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) {
            val wifiInfo = wifiManager.connectionInfo
            
            // Handle different cases for SSID
            val ssid = when {
                wifiInfo.ssid == null -> null
                wifiInfo.ssid == "<unknown ssid>" -> {
                    // Try to get SSID from network info if available
                    null
                }
                wifiInfo.ssid.startsWith("\"") && wifiInfo.ssid.endsWith("\"") -> {
                    wifiInfo.ssid.substring(1, wifiInfo.ssid.length - 1)
                }
                else -> wifiInfo.ssid
            } ?: return null
            
            // Skip if SSID is still unknown
            if (ssid == "<unknown ssid>" || ssid.isEmpty()) return null
            
            val signalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                wifiInfo.rssi.let { rssi ->
                    when {
                        rssi >= -50 -> 4
                        rssi >= -60 -> 3
                        rssi >= -70 -> 2
                        rssi >= -80 -> 1
                        else -> 0
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                WifiManager.calculateSignalLevel(wifiInfo.rssi, 5)
            }
            
            return WiFiNetwork(
                ssid = ssid,
                bssid = wifiInfo.bssid ?: "",
                isSecure = true, // Assume secure for connected networks
                signalStrength = signalLevel,
                frequency = wifiInfo.frequency,
                capabilities = "",
                isConnected = true
            )
        }
    } catch (e: Exception) {
        // Error getting WiFi info
    }
    
    return null
}

private suspend fun startRealWiFiScan(context: Context, onWifiStateChange: (WiFiConnectionState) -> Unit) {
    try {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        
        onWifiStateChange(WiFiConnectionState(isScanning = true))
        
        if (!wifiManager.isWifiEnabled) {
            onWifiStateChange(WiFiConnectionState(
                isScanning = false,
                availableNetworks = emptyList()
            ))
            return
        }
        
        // Start WiFi scan
        wifiManager.startScan()
        
        // Wait for scan to complete
        delay(3000)
        
        // Get scan results
        val scanResults = wifiManager.scanResults ?: emptyList()
        
        val networks = scanResults
            .filter { !it.SSID.isNullOrEmpty() && it.SSID != "<unknown ssid>" }
            .distinctBy { it.SSID }
            .map { scanResult ->
                val signalLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    scanResult.level.let { level ->
                        when {
                            level >= -50 -> 4
                            level >= -60 -> 3
                            level >= -70 -> 2
                            level >= -80 -> 1
                            else -> 0
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager.calculateSignalLevel(scanResult.level, 5)
                }
                
                WiFiNetwork(
                    ssid = scanResult.SSID,
                    bssid = scanResult.BSSID,
                    isSecure = scanResult.capabilities.contains("WPA") || 
                              scanResult.capabilities.contains("WEP") ||
                              scanResult.capabilities.contains("PSK") ||
                              scanResult.capabilities.contains("EAP"),
                    signalStrength = signalLevel,
                    frequency = scanResult.frequency,
                    capabilities = scanResult.capabilities,
                    scanResult = scanResult
                )
            }
            // Comment out device prioritization - show all networks equally
            // .sortedWith(compareByDescending<WiFiNetwork> { 
            //     it.ssid.contains("RideGuard", ignoreCase = true) || 
            //     it.ssid.contains("ra sah jaluk", ignoreCase = true)
            // }.thenByDescending { it.signalStrength })
            .sortedByDescending { it.signalStrength }
        
        onWifiStateChange(WiFiConnectionState(
            isScanning = false,
            availableNetworks = networks
        ))
    } catch (e: Exception) {
        onWifiStateChange(WiFiConnectionState(
            isScanning = false,
            availableNetworks = emptyList()
        ))
    }
}

private suspend fun connectToRealWiFi(
    context: Context, 
    network: WiFiNetwork, 
    onWifiStateChange: (WiFiConnectionState) -> Unit,
    onResult: (Boolean) -> Unit
) {
    try {
        onWifiStateChange(WiFiConnectionState(
            connectedNetwork = null,
            connectionStatus = WiFiConnectionStatus.CONNECTING
        ))
        
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use WifiNetworkSpecifier for Android 10+
            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(network.ssid)
                .apply {
                    if (network.isSecure) {
                        // For secured networks, you'd typically prompt for password
                        // For IoT devices, they might use a default password or be open
                        // This is where you'd set the password if known

                    }
                }
                .build()
            
            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(specifier)
                .build()
            
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(androidNetwork: Network) {
                    onWifiStateChange(WiFiConnectionState(
                        connectedNetwork = network.copy(isConnected = true),
                        connectionStatus = WiFiConnectionStatus.CONNECTED
                    ))
                    onResult(true)
                }
                
                override fun onUnavailable() {
                    onWifiStateChange(WiFiConnectionState(
                        connectionStatus = WiFiConnectionStatus.FAILED
                    ))
                    onResult(false)
                }
                
                override fun onLost(androidNetwork: Network) {
                    onWifiStateChange(WiFiConnectionState(
                        connectionStatus = WiFiConnectionStatus.DISCONNECTED
                    ))
                }
            }
            
            connectivityManager.requestNetwork(request, networkCallback)
            
            // Timeout after 15 seconds
            delay(15000)
            
        } else {
            // Use WifiConfiguration for older Android versions
            @Suppress("DEPRECATION")
            val wifiConfig = WifiConfiguration().apply {
                SSID = "\"${network.ssid}\""
                
                if (!network.isSecure) {
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                } else {
                    // For secured networks, you'd need to handle different security types
                    // This is a simplified approach - in real apps, you'd prompt for password
                    allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                    // preSharedKey = "\"your_password_here\""
                }
            }
            
            @Suppress("DEPRECATION")
            val networkId = wifiManager.addNetwork(wifiConfig)
            
            if (networkId != -1) {
                @Suppress("DEPRECATION")
                val connected = wifiManager.enableNetwork(networkId, true)
                
                if (connected) {
                    delay(5000) // Wait for connection
                    
                    // Check if connected
                    val currentWifi = getCurrentWiFiInfo(context)
                    val success = currentWifi?.ssid == network.ssid
                    
                    if (success) {
                        onWifiStateChange(WiFiConnectionState(
                            connectedNetwork = network.copy(isConnected = true),
                            connectionStatus = WiFiConnectionStatus.CONNECTED
                        ))
                    } else {
                        onWifiStateChange(WiFiConnectionState(
                            connectionStatus = WiFiConnectionStatus.FAILED
                        ))
                    }
                    
                    onResult(success)
                } else {
                    onWifiStateChange(WiFiConnectionState(
                        connectionStatus = WiFiConnectionStatus.FAILED
                    ))
                    onResult(false)
                }
            } else {
                onWifiStateChange(WiFiConnectionState(
                    connectionStatus = WiFiConnectionStatus.FAILED
                ))
                onResult(false)
            }
        }
    } catch (e: Exception) {
        onWifiStateChange(WiFiConnectionState(
            connectionStatus = WiFiConnectionStatus.FAILED
        ))
        onResult(false)
    }
}

@Composable
private fun PairingInputContent(
    context: Context,
    wifiState: WiFiConnectionState,
    hasLocationPermission: Boolean,
    hasWifiPermissions: Boolean,
    onWifiStateChange: (WiFiConnectionState) -> Unit,
    onRequestPermissions: () -> Unit,
    onStartScanning: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { it / 4 }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Pairing Ongoing",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Scan for available Wi-Fi networks\nto register as your RideGuard device",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Current Wi-Fi Status
        getCurrentWiFiInfo(context)?.let { currentNetwork ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Currently connected to:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = currentNetwork.ssid,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Instructions
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "Select any Wi-Fi network to register as your RideGuard device",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Permission status and scan button
        if (!hasLocationPermission || !hasWifiPermissions) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "Location and WiFi permissions are required to scan for devices",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onRequestPermissions() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Grant Permissions",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        } else {
            // Check if WiFi is enabled
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (!wifiManager.isWifiEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Please enable WiFi to scan for devices",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Scan for Networks Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (wifiManager.isWifiEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled = wifiManager.isWifiEnabled) { onStartScanning() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Scan for Wi-Fi Networks",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
        }
    }
}

@Composable
private fun WiFiScanningContent(
    wifiState: WiFiConnectionState,
    onNetworkSelected: (WiFiNetwork) -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (wifiState.isScanning) "Scanning for Devices..." else "Available Networks",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (wifiState.isScanning) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Please wait while we scan for\navailable networks...",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(wifiState.availableNetworks.size) { index ->
                    val network = wifiState.availableNetworks[index]
                    WiFiNetworkItem(
                        network = network,
                        onSelect = { onNetworkSelected(network) }
                    )
                }
            }
            
            if (wifiState.availableNetworks.isEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No networks found.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.Black.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Make sure:\n• WiFi is enabled\n• You're in range of networks\n• Networks are broadcasting",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = "Found ${wifiState.availableNetworks.size} network(s). Select any network to register as your RideGuard device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun WiFiNetworkItem(
    network: WiFiNetwork,
    onSelect: () -> Unit
) {
    // Comment out device-specific highlighting - treat all networks equally
    // val isRideGuard = network.ssid.contains("RideGuard", ignoreCase = true) ||
    //                   network.ssid.contains("ra sah jaluk", ignoreCase = true)
    val isRideGuard = false // Disable special highlighting
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isRideGuard) 2.dp else 1.dp,
                color = if (isRideGuard) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .background(
                if (isRideGuard) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = network.ssid,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isRideGuard) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isRideGuard) MaterialTheme.colorScheme.primary else Color.Black
                )
                Text(
                    text = if (network.isSecure) "Secured" else "Open",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            
            // Signal strength indicator
            Row {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(((index + 1) * 4).dp)
                            .background(
                                if (index < network.signalStrength) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    Color.Gray.copy(alpha = 0.3f),
                                RoundedCornerShape(1.dp)
                            )
                    )
                    if (index < 3) Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}

@Composable
private fun DeviceFoundContent(
    network: WiFiNetwork,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + scaleIn(
            animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutBack)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Pairing Ongoing",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Register the selected\nnetwork as your RideGuard device?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Selected Network Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = network.ssid,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (network.isSecure) "Secured Network" else "Open Network",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Animated success text
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(500, delayMillis = 200)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Text(
                text = "RideGuard Device Connected!",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "This network will be registered as your RideGuard device",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Confirm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onConfirm() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
        }
    }
}

@Composable
private fun DeviceNotFoundContent(
    selectedNetwork: WiFiNetwork?,
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connection Failed",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = selectedNetwork?.let { "Could not connect to\n${it.ssid}" } ?: "Connection Failed",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Selected Network Info Display
        selectedNetwork?.let { network ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = network.ssid,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (network.isSecure) "Secured Network" else "Open Network",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
//            text = "Connection timeout - Device may be offline",
            // Comment out device type checking - any network can be a RideGuard device
             text = if (selectedNetwork?.ssid?.let { ssid ->
                        !ssid.contains("STM", ignoreCase = true)
                    } == true)
                    "Selected network is not a RideGuard device"
                    else "Connection timeout - Device may be offline",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please make sure:\n• RideGuard device is powered on\n• You're within range of the device\n• Device is in pairing mode",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Try Again Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onTryAgain() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PairingSuccessContent(
    networkName: String,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showSuccessText by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
        delay(300)
        showSuccessText = true
    }
    
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animated success title
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Text(
                text = "Pairing Successful!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Animated success message
        AnimatedVisibility(
            visible = showSuccessText,
            enter = fadeIn(tween(600)) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { it / 4 }
            )
        ) {
            Column {
                Text(
                    text = "Successfully connected to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = networkName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Success! This network has been registered as your RideGuard device.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Confirm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onConfirm() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun BlackBoxDeviceCard(
    deviceName: String,
    isOnline: Boolean,
    onToggleStatus: () -> Unit = {}
) {
    
    // Animate card color transition
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isOnline) 1f else 0.8f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleStatus() }
            .padding(2.dp), // Extra padding for scale animation
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOnline) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon
            
            // Custom device icon - uses painterResource for vector drawables
            Icon(
                painter = painterResource(id = R.drawable.box),
                contentDescription = "RideGuard Device", 
                tint = Color.White, // Apply white tint for visibility on primary background
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Column {
                        Text(
                            text = if (deviceName == "No Device Connected") "No Device Connected" else deviceName,
                            color = Color.White,
                            fontWeight = if (deviceName == "No Device Connected") FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (deviceName == "No Device Connected") FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        BodyText(
                            text = "Status: ${if (isOnline) "Online" else "Not connected"}",
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideGuardDetailsSection(
    onPulsaBalanceClick: () -> Unit = {},
    isDeviceConnected: Boolean = false,
    batteryResult: String? = null
) {
    Column {
        SectionHeader(
            text = "RideGuard Details",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Last Connected - Aligned to right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyText(
                text = "Last Connected:",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f, false)
            )
            Text(
                text = if (isDeviceConnected) "DD/MM/YYYY" else "---",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Pulsa Balance Section  
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyText(
                text = "Pulsa Balance",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f, false)
            )
            if (isDeviceConnected) {
                Text(
                    text = "Check Here",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { onPulsaBalanceClick() }
                )
            } else {
                Text(
                    text = "---",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun CameraSettingsSection(
    deletionRate: String,
    onDeletionRateChange: (String) -> Unit,
    sdCardResult: String? = null
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    Column {
        SectionHeader(
            text = "Camera Settings",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Deletion Rate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Box {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showDropdown = !showDropdown }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$deletionRate ▼",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    val deletionRateOptions = listOf("1 Hour", "2 Hours", "3 Hours", "6 Hours", "12 Hours", "1 Day")
                    
                    deletionRateOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (option == deletionRate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onDeletionRateChange(option)
                                showDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Parse SD card information - format: "FREE=7000MB"
        val (displayText, usedPercentage) = if (sdCardResult != null && !sdCardResult.startsWith("Failed:") && !sdCardResult.startsWith("Invalid")) {
            try {
                // Parse plain text format "FREE=7000MB"
                val freeMBStr = sdCardResult.trim()
                    .replace("FREE=", "")
                    .replace("MB", "")
                    .trim()
                val freeMB = freeMBStr.toIntOrNull() ?: 0
                val totalMB = 8192 // 8GB = 8192MB
                val usedMB = totalMB - freeMB
                val usedGB = usedMB / 1024.0
                val totalGB = totalMB / 1024.0
                val percentage = usedMB.toFloat() / totalMB.toFloat()
                
                "%.1f GB of %.0f GB".format(usedGB, totalGB) to percentage
            } catch (e: Exception) {
                "--- GB of 8 GB" to 0f
            }
        } else {
            "--- GB of 8 GB" to 0f
        }
        
        // SD Card Capacity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SD Card Capacity",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // SD Card Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                    RoundedCornerShape(4.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(usedPercentage)
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

@Composable
fun EmergencyContactsSection(
    emergencyContacts: List<EmergencyContactInfo>,
    isLoading: Boolean = false,
    onAddMoreUsers: () -> Unit = {},
    onDeleteContact: (EmergencyContactInfo) -> Unit = {},
    isDeviceConnected: Boolean = false,
    onBatteryResult: (String?) -> Unit = {},
    currentUserUid: String? = null,
    currentUserName: String? = null,
    sdCardResult: String? = null,
    onSdCardResult: (String?) -> Unit = {}
) {
    var isVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isGettingBattery by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        delay(200) // Small delay for staggered appearance
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500)) + slideInVertically(
            animationSpec = tween(500),
            initialOffsetY = { it / 6 }
        )
    ) {
        Column {
        SectionHeader(
            text = "Emergency Contacts",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading indicator or contact list
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            // Contact Items - Dynamic list from database
            emergencyContacts.forEach { contact ->
                EmergencyContactItem(
                    name = contact.username,
                    role = "Emergency Contact",
                    onDelete = { onDeleteContact(contact) }
                )
                
                if (contact != emergencyContacts.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // Show message if no contacts
            if (emergencyContacts.isEmpty()) {
                Text(
                    text = "No emergency contacts added yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add More Users Button with limit check
        val canAddMore = emergencyContacts.size < 5
        
        PrimaryButton(
            text = "Add More Users",
            onClick = onAddMoreUsers,
            modifier = Modifier.fillMaxWidth(),
            enabled = canAddMore
        )
        
        // Show limit message when at maximum
        if (!canAddMore) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You can only add up to 5 emergency contacts",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Sync Contact Data Button
            SecondaryButton(
                text = if (isSyncing) "Syncing..." else "Sync Configuration to Device",
                onClick = {
                    if (!isSyncing && isDeviceConnected) {
                        isSyncing = true
                        syncResult = null

                        // Use passed auth values
                        val userUid = currentUserUid ?: "unknown"
                        val userName = currentUserName ?: "Unknown"
                        
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                // IP dan Port ESP01
                                val espIp = "192.168.4.1"
                                val espPort = 80

                                // === AMBIL SEMUA NOMOR DARI emergencyContacts ===
                                val numberList = emergencyContacts.map { it.phoneNumber }

                                // Buat JSON string
                                val jsonToSend = """
                        {
                            "number": ${numberList.joinToString(
                                    prefix = "[\"",
                                    postfix = "\"]",
                                    separator = "\",\""
                                )} 
                        } FIN
                    """.trimIndent()

                                // --- Update Firestore device document before sending TCP ---
                                try {
                                    val firestore = Firebase.firestore
                                    val deviceDoc = firestore.collection("rideguard_id").document("DEVICE_001")

                                    val currentUserData = mapOf(
                                        "uid" to userUid,
                                        "username" to userName,
                                        "lastSyncAt" to System.currentTimeMillis()
                                    )

                                    // Overwrite with current user data (only one user at a time)
                                    val deviceData = mapOf("currentUser" to currentUserData)
                                    deviceDoc.set(deviceData)
                                        .addOnSuccessListener {
                                            Log.d("BlackboxScreen", "Device document updated with current user: $userName")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("BlackboxScreen", "Failed to update device document: ${e.message}")
                                        }
                                } catch (e: Exception) {
                                    // Firestore update failed - log but proceed with TCP send
                                    Log.w("BlackboxScreen", "Failed to update device document: ${e.message}")
                                }

                                // Koneksi TCP
                                val socket = Socket(espIp, espPort)
                                val out = PrintWriter(socket.getOutputStream(), true)

                                // Kirim JSON
                                out.println(jsonToSend)

                                socket.close()

                                withContext(Dispatchers.Main) {
                                    syncResult = "Send Success"
                                    isSyncing = false
                                }

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    syncResult = "Failed: ${e.message}"
                                    isSyncing = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isDeviceConnected && !isSyncing
            )
        
        Spacer(modifier = Modifier.height(12.dp))
        


            SecondaryButton(
                text = if (isGettingBattery) "Getting..." else "Get SD Card Info",
                onClick = {
                    if (!isGettingBattery && isDeviceConnected) {
                        isGettingBattery = true
                        onSdCardResult(null)

                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val espIp = "192.168.4.1"
                                val espPort = 80

                                // === KONEKSI TCP ===
                                val socket = Socket(espIp, espPort)

                                val out = PrintWriter(socket.getOutputStream(), true)
                                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                                // === KIRIM PERINTAH GET ===
                                out.println("GET FIN")

                                // === TERIMA DATA ===
                                val response = input.readLine()  // contoh: FREE=7000MB

                                socket.close()

                                // === PARSE PLAIN TEXT RESPONSE ===
                                val sdValue = try {
                                    response?.trim() ?: "Invalid response"
                                } catch (ex: Exception) {
                                    null
                                }

                                withContext(Dispatchers.Main) {
                                    val result = sdValue ?: "Invalid response: $response"
                                    onSdCardResult(result)

                                    isGettingBattery = false
                                }

                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    val errorResult = "Failed: ${e.message}"
                                    onSdCardResult(errorResult)
                                    isGettingBattery = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isDeviceConnected && !isGettingBattery
            )
            
            // Sync result message at bottom
            syncResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (result.startsWith("Send Success")) "data synced" else "data cannot sync",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (result.startsWith("Send Success")) 
                        Color(0xFF4CAF50) // Green color
                    else 
                        Color(0xFFF44336), // Red color
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

        }
    }
}

@Composable
fun DeleteContactConfirmationDialog(
    contactName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Delete Contact",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Are you sure you want to remove \"$contactName\" from your emergency contacts?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button (No)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onDismiss() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    // Delete Button (Yes)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.error,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onConfirm() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Yes",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyContactItem(
    name: String,
    role: String,
    onDelete: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyText(
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f, false)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (role == "Family Leader") "$role (You)" else role,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            // Delete button
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_delete),
                    contentDescription = "Delete Contact",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlackboxScreenPreview() {
    MyAppTheme {
        BlackboxScreen()
    }
}


