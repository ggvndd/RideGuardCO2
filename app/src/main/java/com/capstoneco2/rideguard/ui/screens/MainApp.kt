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

@Composable
fun MainApp(
    username: String,
    intent: Intent? = null,
    onLogout: () -> Unit = { },
    authViewModel: AuthViewModel = viewModel()
) {
    // Create shared ViewModels at MainApp level
    val emergencyContactViewModel: EmergencyContactViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPulsaBalanceScreen by remember { mutableStateOf(false) }
    var showAccidentCard by remember { mutableStateOf(false) }
    var showAccidentDialog by remember { mutableStateOf(false) }
    
    // State for crash data from notification
    var crashLatitude by remember { mutableStateOf(-7.7956) } // Default coordinates
    var crashLongitude by remember { mutableStateOf(110.3695) }
    
    // Check for crash notification intent
    LaunchedEffect(intent) {
        intent?.let { notificationIntent ->
            if (notificationIntent.getStringExtra("emergency_type") == "crash") {
                // Extract crash data from intent
                crashLatitude = notificationIntent.getDoubleExtra("latitude", -7.7956)
                crashLongitude = notificationIntent.getDoubleExtra("longitude", 110.3695)
                
                // Show accident dialog immediately for crash notifications
                showAccidentDialog = true
                
                // Navigate to blackbox screen (crash management)
                selectedTab = 1
            }
        }
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
                showAccidentCard = true // Show the card on home screen after closing
            },
            latitude = crashLatitude,
            longitude = crashLongitude
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