package com.capstoneco2.rideguard.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstoneco2.rideguard.ui.components.BottomNavigationBar
import com.capstoneco2.rideguard.ui.components.TrafficAccidentDialog
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import com.capstoneco2.rideguard.viewmodel.EmergencyContactViewModel
import android.content.Intent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.capstoneco2.rideguard.service.EmergencyNotificationManager

@Composable
fun MainApp(
    username: String,
    intent: Intent? = null,
    onLogout: () -> Unit = { },
    authViewModel: AuthViewModel = viewModel()
) {
    // Create shared ViewModels at MainApp level
    val emergencyContactViewModel: EmergencyContactViewModel = viewModel()
    val context = LocalContext.current
    val emergencyNotificationManager = remember { EmergencyNotificationManager() }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPulsaBalanceScreen by remember { mutableStateOf(false) }
    var showAccidentCard by remember { mutableStateOf(false) }
    var showAccidentDialog by remember { mutableStateOf(false) }
    
    // State for crash data from notification
    var crashLatitude by remember { mutableStateOf(-7.7956) } // Default coordinates
    var crashLongitude by remember { mutableStateOf(110.3695) }
    var userRole by remember { mutableStateOf(com.capstoneco2.rideguard.ui.components.UserRole.CRASH_VICTIM) }
    var crashVictimName by remember { mutableStateOf("Unknown User") }
    
    // State for emergency help confirmation
    var helpConfirmed by remember { mutableStateOf(false) }
    
    // Check for crash notification intent
    LaunchedEffect(intent) {
        intent?.let { notificationIntent ->
            android.util.Log.d("MainApp", "🎯 Intent received with extras:")
            notificationIntent.extras?.keySet()?.forEach { key ->
                android.util.Log.d("MainApp", "🎯   $key = ${notificationIntent.extras?.get(key)}")
            }
            
            val emergencyType = notificationIntent.getStringExtra("emergency_type")
            val userRoleString = notificationIntent.getStringExtra("user_role")
            android.util.Log.d("MainApp", "🔍 Emergency type: '$emergencyType', User role: '$userRoleString'")
            
            if (emergencyType == "crash") {
                android.util.Log.d("MainApp", "🚨 Processing crash notification intent")
                // Extract crash data from intent
                crashLatitude = notificationIntent.getDoubleExtra("latitude", -7.7956)
                crashLongitude = notificationIntent.getDoubleExtra("longitude", 110.3695)
                
                // Determine user role from intent
                val roleFromIntent = notificationIntent.getStringExtra("user_role") ?: "crash_victim"
                userRole = if (roleFromIntent == "emergency_contact") {
                    com.capstoneco2.rideguard.ui.components.UserRole.EMERGENCY_CONTACT
                } else {
                    com.capstoneco2.rideguard.ui.components.UserRole.CRASH_VICTIM
                }
                
                // Extract crash victim name for emergency contacts
                crashVictimName = notificationIntent.getStringExtra("crash_victim_name") ?: "Unknown User"
                
                // Show accident dialog immediately for crash notifications
                showAccidentDialog = true
                
                // Navigate to blackbox screen (crash management)
                selectedTab = 1
                
                // Dismiss emergency notification since user is now in the app
                val crashId = notificationIntent.getStringExtra("crash_id") ?: "unknown"
                emergencyNotificationManager.dismissEmergencyNotificationOnAppEnter(context, crashId)
            } else {
                android.util.Log.d("MainApp", "❌ Intent does not match crash emergency type. Emergency type: '$emergencyType'")
            }
        } ?: android.util.Log.d("MainApp", "❌ No intent received")
    }
    
    // Emergency contacts are now managed by EmergencyContactViewModel

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
            // Main content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (showPulsaBalanceScreen) {
                    PulsaBalanceScreen(
                        onBackClick = { showPulsaBalanceScreen = false }
                    )
                } else {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            slideInHorizontally(
                                initialOffsetX = { width ->
                                    if (targetState > initialState) width else -width
                                }
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { width ->
                                    if (targetState > initialState) -width else width
                                }
                            )
                        },
                        label = "tab_transition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> HomeScreen(
                                userName = username,
                                onNavigateToPulsaBalance = { showPulsaBalanceScreen = true },
                                showAccidentCard = showAccidentCard,
                                onShowAccidentDialog = { showAccidentDialog = true },
                                authViewModel = authViewModel,
                                emergencyContactViewModel = emergencyContactViewModel
                            )
                            1 -> BlackboxScreen(
                                onNavigateToPulsaBalance = { showPulsaBalanceScreen = true },
                                authViewModel = authViewModel,
                                emergencyContactViewModel = emergencyContactViewModel
                            )
                            2 -> TutorialScreen()
                            3 -> SettingsScreen(
                                onShowAccidentDialog = { showAccidentDialog = true },
                                onLogoutSuccess = onLogout
                            )
                        }
                    }
                }
            }

            // Bottom Navigation
            BottomNavigationBar(
                currentRoute = when (selectedTab) {
                    0 -> "home"
                    1 -> "blackbox" 
                    2 -> "tutorial"
                    3 -> "settings"
                    else -> "home"
                },
                onNavigate = { route ->
                    // Close PulsaBalance screen and navigate to requested tab
                    showPulsaBalanceScreen = false
                    selectedTab = when (route) {
                        "home" -> 0
                        "blackbox" -> 1
                        "tutorial" -> 2
                        "settings" -> 3
                        else -> 0
                    }
                }
            )
        }
        
        // Traffic Accident Dialog - Global dialog that can be triggered from anywhere
        TrafficAccidentDialog(
            isVisible = showAccidentDialog,
            onClose = { 
                showAccidentDialog = false
                // Show accident card on home screen for both crash victim and emergency contact
                showAccidentCard = true
            },
            latitude = crashLatitude,
            longitude = crashLongitude,
            userRole = userRole,
            crashVictimName = crashVictimName,
            onEmergencyServicesCalled = {
                // Dismiss the emergency notification since services are being contacted
                val crashId = intent?.getStringExtra("crash_id") ?: "emergency_services_called"
                emergencyNotificationManager.dismissEmergencyNotificationOnConfirmed(context, crashId)
                
                // Cross-device notification logic
                when (userRole) {
                    com.capstoneco2.rideguard.ui.components.UserRole.CRASH_VICTIM -> {
                        // Crash victim called emergency services → Notify emergency contacts
                        com.capstoneco2.rideguard.notification.NotificationHelper.showEmergencyNotification(
                            context = context,
                            title = "Emergency Services Contacted",
                            body = "${username} has contacted emergency services. Help is being dispatched to their location.",
                            isCrashData = false
                        )
                    }
                    com.capstoneco2.rideguard.ui.components.UserRole.EMERGENCY_CONTACT -> {
                        // Emergency contact called for crash victim → Notify crash victim
                        com.capstoneco2.rideguard.notification.NotificationHelper.showEmergencyNotification(
                            context = context,
                            title = "Help Is On The Way",
                            body = "Your emergency contact has called emergency services for you. Help has been dispatched to your location.",
                            isCrashData = false
                        )
                    }
                }
            },
            onHelpConfirmed = {
                helpConfirmed = true
                showAccidentDialog = false
                showAccidentCard = false  // Hide the accident card on home screen too
                
                // Dismiss the emergency notification since help is confirmed
                val crashId = intent?.getStringExtra("crash_id") ?: "help_confirmed"
                emergencyNotificationManager.dismissEmergencyNotificationOnConfirmed(context, crashId)
                
                // Send final confirmation notification to all parties
                when (userRole) {
                    com.capstoneco2.rideguard.ui.components.UserRole.CRASH_VICTIM -> {
                        // Crash victim confirmed help → Notify emergency contacts
                        com.capstoneco2.rideguard.notification.NotificationHelper.showEmergencyNotification(
                            context = context,
                            title = "✅ Emergency Response Confirmed",
                            body = "${username} has confirmed that emergency services are on the way. The emergency is being handled.",
                            isCrashData = false
                        )
                    }
                    com.capstoneco2.rideguard.ui.components.UserRole.EMERGENCY_CONTACT -> {
                        // Emergency contact confirmed help → Notify crash victim  
                        com.capstoneco2.rideguard.notification.NotificationHelper.showEmergencyNotification(
                            context = context,
                            title = "✅ Your Safety Confirmed",
                            body = "Your emergency contact has confirmed that help is on the way. Emergency services are responding to your location.",
                            isCrashData = false
                        )
                    }
                }
            }
        )
    }

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    MyAppTheme {
        MainApp(username = "John Doe")
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainAppDarkPreview() {
    MyAppTheme(darkTheme = true) {
        MainApp(username = "John Doe")
    }
}