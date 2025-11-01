package com.capstoneco2.rideguard.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.CaptionText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.theme.Blue80
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import com.capstoneco2.rideguard.network.NetworkRepository
import com.capstoneco2.rideguard.service.SmsService
import com.capstoneco2.rideguard.notification.NotificationHelper
import com.capstoneco2.rideguard.service.EmergencyContactServiceAdapter
import com.capstoneco2.rideguard.service.UserProfileService
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SettingsScreen(
    onShowAccidentDialog: () -> Unit = { },
    onLogoutSuccess: () -> Unit = { },
    authViewModel: AuthViewModel = viewModel()
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var useAsGateway by remember { mutableStateOf(false) }
    var selectedInterval by remember { mutableStateOf("60 Seconds") }
    var showDropdown by remember { mutableStateOf(false) }
    var apiTestResult by remember { mutableStateOf<String?>(null) }
    var isApiTesting by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val networkRepository = remember { NetworkRepository.getInstance() }
    val smsService = remember { SmsService() }
    val userProfileService = remember { UserProfileService() }
    val emergencyContactService = remember { EmergencyContactServiceAdapter(userProfileService) }
    
    // Load gateway setting on startup
    LaunchedEffect(Unit) {
        useAsGateway = smsService.isGatewayEnabled(context)
    }
    
    // Get auth state and user profile
    val authState by authViewModel.authState.collectAsState()
    val userName = authState.userProfile?.username ?: "User"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp)) // Increased spacing from top more
            
            // Header - Changed color to Blue80
            MainHeader(
                text = "Settings",
                textAlign = TextAlign.Start,
                color = Blue80
            )
        }

        item {
            // User Profile Card - Horizontal layout, left aligned
            UserProfileCard(
                userName = userName,
                onSignOutClick = {
                    authViewModel.signOut()
                    onLogoutSuccess()
                }
            )
        }
        
        item {
            // Notifications Section
            SectionHeader(text = "Notifications")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Push Notifications Row - Fixed positioning
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BodyText(
                    text = "Push Notifications",
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Use As Gateway Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    BodyText(
                        text = "Use As Gateway",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    CaptionText(
                        text = "Forward SMS messages to server",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = useAsGateway,
                    onCheckedChange = { enabled ->
                        useAsGateway = enabled
                        smsService.setGatewayEnabled(context, enabled)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
            
        }
        
        item {
            // Repeat Interval Row - Text and dropdown in same row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Repeat Interval",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                
                Box {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { showDropdown = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "$selectedInterval ▼",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White
                        )
                    }
                    
                    // Dropdown Menu
                    DropdownMenu(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                    ) {
                        listOf("15 Seconds", "30 Seconds", "60 Seconds").forEach { interval ->
                            DropdownMenuItem(
                                text = {
                                    BodyText(
                                        text = interval,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    selectedInterval = interval
                                    showDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }
        
        item {
            // Testing Section
            SectionHeader(text = "Testing")
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Simulate Accident Button
            PrimaryButton(
                text = "Simulate Traffic Accident",
                onClick = onShowAccidentDialog,
                modifier = Modifier.fillMaxWidth()
            )
            
            BodyText(
                text = "This button simulates a traffic accident detection for testing purposes.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test API Button
            PrimaryButton(
                text = if (isApiTesting) "Testing API..." else "Test API Connection",
                onClick = {
                    if (!isApiTesting) {
                        isApiTesting = true
                        apiTestResult = null
                        coroutineScope.launch {
                            try {
                                val result = networkRepository.testApiConnection(phoneNumber)
                                apiTestResult = if (result.isSuccess) {
                                    "✅ API Connection Successful!"
                                } else {
                                    "❌ API Connection Failed: ${result.exceptionOrNull()?.message}"
                                }
                            } catch (e: Exception) {
                                apiTestResult = "❌ Error: ${e.message}"
                            } finally {
                                isApiTesting = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isApiTesting
            )
            
            BodyText(
                text = "Test POST request to API endpoint with JSON data.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test Emergency Notification Button
            SecondaryButton(
                text = "Test Emergency Notification",
                onClick = {
                    try {
                        NotificationHelper.showEmergencyNotification(
                            context = context,
                            title = "Emergency Detection Test",
                            body = "From: +1-555-TEST — Emergency keywords detected in SMS message. Tap to view details.",
                            isCrashData = false
                        )
                        apiTestResult = "✅ Emergency notification sent! Check your notification panel."
                    } catch (e: Exception) {
                        apiTestResult = "❌ Notification Error: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            BodyText(
                text = "Test regular emergency notification style (dismissible).",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Test Crash Notification Button
            SecondaryButton(
                text = "Test Crash Notification",
                onClick = {
                    coroutineScope.launch {
                        try {
                            // Show crash notification for the victim
                            NotificationHelper.showEmergencyNotification(
                                context = context,
                                title = "🚨 CRASH DETECTED",
                                body = "From: +1-555-CRASH — crash_id: TEST, rideguard_id: DEMO, longitude: -7.7676, latitude: 110.3698. Emergency response required!",
                                isCrashData = true,
                                crashId = "TEST",
                                rideguardId = "DEMO", 
                                userId = "user123",
                                latitude = -7.7676,
                                longitude = 110.3698
                            )
                            
                            // Get current user and notify their emergency contacts
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            if (currentUser != null) {
                                val currentUserProfile = userProfileService.getUserProfile(currentUser.uid).getOrNull()
                                val victimName = currentUserProfile?.username ?: currentUser.displayName ?: "Unknown User"
                                
                                val emergencyContacts = emergencyContactService.getEmergencyContacts(currentUser.uid).getOrNull()
                                if (!emergencyContacts.isNullOrEmpty()) {
                                    var notifiedContacts = 0
                                    
                                    // Send notification to each emergency contact
                                    // Note: In a real app, this would use FCM to send to other devices
                                    // For testing, we create local notifications to simulate multiple devices
                                    emergencyContacts.forEach { contact ->
                                        NotificationHelper.showEmergencyContactNotification(
                                            context = context,
                                            crashVictimName = victimName,
                                            latitude = -7.7676,
                                            longitude = 110.3698,
                                            crashId = "TEST_EC_${contact.contactId}",
                                            rideguardId = "EMERGENCY_${contact.username}"
                                        )
                                        notifiedContacts++
                                    }
                                    
                                    apiTestResult = "✅ Crash notification sent! Also notified $notifiedContacts emergency contact(s): ${emergencyContacts.map { it.username }.joinToString(", ")}. Tap notifications to test both perspectives."
                                } else {
                                    apiTestResult = "✅ Crash notification sent! No emergency contacts configured to notify. Add emergency contacts in the Home screen."
                                }
                            } else {
                                apiTestResult = "✅ Crash notification sent! (Not logged in - cannot notify emergency contacts)"
                            }
                        } catch (e: Exception) {
                            apiTestResult = "❌ Notification Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            BodyText(
                text = "Test crash notification (persistent, red) + notify your emergency contacts if configured.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Test Emergency Contact Notification Button
            SecondaryButton(
                text = "Test Emergency Contact Alert",
                onClick = {
                    try {
                        NotificationHelper.showEmergencyContactNotification(
                            context = context,
                            crashVictimName = "Alex Johnson",
                            latitude = -7.7956,
                            longitude = 110.3695
                        )
                        apiTestResult = "✅ Emergency Contact alert sent! Tap it to experience the emergency contact perspective."
                    } catch (e: Exception) {
                        apiTestResult = "❌ Emergency Contact Notification Error: ${e.message}"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            BodyText(
                text = "Test emergency contact notification (orange, shows Alex Johnson crashed, you help them).",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            

            

            
            // Display API test result
            apiTestResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                BodyText(
                    text = result,
                    color = if (result.startsWith("✅")) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        

        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun UserProfileCard(
    userName: String,
    onSignOutClick: () -> Unit
) {
    Column {
        // Profile Label - Same size as Notifications section
        SectionHeader(
            text = "Profile",
            textAlign = TextAlign.Start
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Section - Horizontal layout, left aligned
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Profile Picture
            Image(
                painter = painterResource(id = R.drawable.motorcycle_welcome_image),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Name and Date Column
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                // User Name
                SectionHeader(
                    text = userName,
                    textAlign = TextAlign.Start
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date Joined
                BodyText(
                    text = "Date Joined",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sign Out Button
        SecondaryButton(
            text = "Sign Out",
            onClick = onSignOutClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MyAppTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        SettingsScreen()
    }
}
