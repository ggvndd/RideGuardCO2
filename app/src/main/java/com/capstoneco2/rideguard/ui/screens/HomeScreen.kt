package com.capstoneco2.rideguard.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.theme.Blue80
import com.capstoneco2.rideguard.ui.theme.Black80
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import com.capstoneco2.rideguard.data.EmergencyContactInfo
import com.capstoneco2.rideguard.ui.components.AddEmergencyContactDialog
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import com.capstoneco2.rideguard.viewmodel.EmergencyContactViewModel

// Wi-Fi connectivity data classes (from BlackboxScreen)
data class WiFiDeviceInfo(
    val ssid: String,
    val isConnected: Boolean = false,
    val signalStrength: Int = 0
)

// Real Wi-Fi utility function for device detection
private fun getCurrentWiFiDeviceInfo(context: Context): WiFiDeviceInfo? {
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
                wifiInfo.ssid == "<unknown ssid>" -> null
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
            
            return WiFiDeviceInfo(
                ssid = ssid,
                isConnected = true,
                signalStrength = signalLevel
            )
        }
    } catch (e: Exception) {
        Log.e("WiFiUtils", "Error getting current WiFi info", e)
    }
    
    return null
}

@Composable
fun HomeScreen(
    userName: String = "User",
    onNavigateToPulsaBalance: () -> Unit = {},
    showAccidentCard: Boolean = false,
    onShowAccidentDialog: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    emergencyContactViewModel: EmergencyContactViewModel = viewModel()
) {
    val context = LocalContext.current
    var isBlackboxOnline by remember { mutableStateOf(false) }
    var batteryLevel by remember { mutableStateOf("100%") }
    var blackboxSerialNumber by remember { mutableStateOf("No Device Connected") }
    var connectedDeviceName by remember { mutableStateOf("No Device Connected") }
    var showAddContactDialog by remember { mutableStateOf(false) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasWifiPermissions by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val emergencyContactState by emergencyContactViewModel.state.collectAsState()
    val currentUserUid = authState.user?.uid ?: ""
    
    // Permission launcher for Wi-Fi access
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
        
        // Get current Wi-Fi device info if permissions are granted
        if (hasLocationPermission && hasWifiPermissions) {
            getCurrentWiFiDeviceInfo(context)?.let { deviceInfo ->
                // Any connected Wi-Fi network is treated as RideGuard device
                connectedDeviceName = deviceInfo.ssid
                blackboxSerialNumber = deviceInfo.ssid
                isBlackboxOnline = true
                Log.d("HomeScreen", "RideGuard device detected: ${deviceInfo.ssid}")
            } ?: run {
                connectedDeviceName = "No Device Connected"
                blackboxSerialNumber = "No Device Connected"
                isBlackboxOnline = false
                Log.d("HomeScreen", "No RideGuard device connected")
            }
        } else {
            Log.w("HomeScreen", "Wi-Fi permissions not granted, cannot detect device")
        }
    }
    
    // Load emergency contacts when screen loads
    LaunchedEffect(currentUserUid) {
        Log.d("HomeScreen", "LaunchedEffect triggered. CurrentUserUid: $currentUserUid")
        if (currentUserUid.isNotEmpty()) {
            Log.d("HomeScreen", "Loading emergency contacts for user: $currentUserUid")
            emergencyContactViewModel.loadEmergencyContacts(currentUserUid)
        } else {
            Log.w("HomeScreen", "CurrentUserUid is empty, not loading emergency contacts")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
            // Welcome Header with Dark Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Black80,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "Welcome,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "$userName!",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        item {
            // Status Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "RideGuard Status:",
                    value = if (isBlackboxOnline) "Connected" else "Not Connected",
                    valueColor = if (isBlackboxOnline) Color(0xFF06A759) else Color(0xFFFF6B6B),
                    onClick = {
                        if (!hasLocationPermission || !hasWifiPermissions) {
                            permissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE
                            ))
                        }
                    }
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Battery:",
                    value = if (isBlackboxOnline) batteryLevel else "--",
                    valueColor = if (isBlackboxOnline) Color(0xFF06A759) else Color(0xFFBBBBBB),
                    onClick = { /* Handle battery click */ }
                )
            }
        }
        
        item {
            // Blackbox Connected Card with Image Background
            BlackboxConnectedCard(
                blackboxName = connectedDeviceName,
                isOnline = isBlackboxOnline,
                onClick = {
                    if (!hasLocationPermission || !hasWifiPermissions) {
                        permissionLauncher.launch(arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.CHANGE_WIFI_STATE
                        ))
                    }
                }
            )
        }
        
        // Traffic Accident Card (conditional)
        if (showAccidentCard) {
            item {
                TrafficAccidentCard(
                    userName = authState.user?.displayName ?: "Current User",
                    onMoreDetailsClick = onShowAccidentDialog
                )
            }
        }
        
        item {
            // Pulsa Balance Section
            PulsaBalanceSection(
                onNavigateToPulsaBalance = onNavigateToPulsaBalance
            )
        }
        
        item {
            // Emergency Contacts Section
            HomeEmergencyContactsSection(
                emergencyContacts = emergencyContactState.contacts,
                onAddAnotherClick = { showAddContactDialog = true },
                isLoading = emergencyContactState.isLoading
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
        }
    }
    
    // Add Emergency Contact Dialog
    if (showAddContactDialog) {
        AddEmergencyContactDialog(
            currentUserUid = currentUserUid,
            onDismiss = { showAddContactDialog = false },
            onContactAdded = { 
                showAddContactDialog = false
                // No need to manually reload - ViewModel handles this automatically
            },
            viewModel = emergencyContactViewModel
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = valueColor,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun BlackboxConnectedCard(
    blackboxName: String,
    isOnline: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 2.dp,
                color = if (isOnline) Blue80 else Color(0xFFFF6B6B),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color.Transparent else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image (only show when online)
            if (isOnline) {
                Image(
                    painter = painterResource(id = R.drawable.motorcycle_welcome_image),
                    contentDescription = "Blackbox Background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Dark Overlay for better text visibility (only when online)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Color.Black.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                )
            }
            
            // Content
            // Content - Centered both vertically and horizontally
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Label
                Box(
                    modifier = Modifier
                        .background(
                            if (isOnline) MaterialTheme.colorScheme.primary else Color(0xFFFF6B6B),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isOnline) "RideGuard Device Connected" else "No RideGuard Device",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Main Text - Centered
                Text(
                    text = if (isOnline) blackboxName else "Tap to Connect",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isOnline) Color.White else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (!isOnline) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grant permissions to detect your RideGuard device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun PulsaBalanceSection(
    onNavigateToPulsaBalance: () -> Unit = {}
) {
   Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Pulsa Balance",
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rp5000,00",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        
            // Custom button with smaller font
            Card(
                modifier = Modifier
                    .width(150.dp)
                    .clickable { onNavigateToPulsaBalance() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add More Pulsa",
                        style = MaterialTheme.typography.bodySmall, // Made smaller
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

       HorizontalDivider(
           Modifier, DividerDefaults.Thickness, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
       )
    }
}

@Composable
private fun HomeEmergencyContactsSection(
    emergencyContacts: List<EmergencyContactInfo>,
    onAddAnotherClick: () -> Unit,
    isLoading: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Emergency Contacts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = if (emergencyContacts.size >= 5) "Limit Reached" else "Add Another",
                style = MaterialTheme.typography.bodyMedium,
                color = if (emergencyContacts.size >= 5) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(enabled = emergencyContacts.size < 5) { 
                    if (emergencyContacts.size < 5) {
                        onAddAnotherClick() 
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Log.d("HomeScreen", "Rendering emergency contacts section. isLoading: $isLoading, emergencyContacts.size: ${emergencyContacts.size}")
        
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Loading emergency contacts...")
            }
        } else if (emergencyContacts.isEmpty()) {
            Text(
                text = "No emergency contacts added yet. Tap 'Add Another' to add your first contact.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Emergency Contact Chips in Grid (2x2)
            val contactUsernames = emergencyContacts.map { it.username }
            val contactRows = contactUsernames.chunked(2)
        
            contactRows.forEach { rowContacts ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowContacts.forEach { contact ->
                        EmergencyContactChip(
                            name = contact,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if odd number of contacts
                    if (rowContacts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (rowContacts != contactRows.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TrafficAccidentCard(
    userName: String,
    onMoreDetailsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Traffic Accident Detected",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "User: $userName",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // More Details Button
            Box(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onMoreDetailsClick() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "More Details",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        HomeScreen(
            userName = "John Doe"
        )
    }
}
