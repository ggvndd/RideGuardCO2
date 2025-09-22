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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.capstoneco2.rideguard.ui.components.BottomNavigationBar
import com.capstoneco2.rideguard.ui.components.TrafficAccidentDialog
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun MainApp(username: String) {
    var selectedTab by remember { mutableStateOf(0) }
    var showPulsaBalanceScreen by remember { mutableStateOf(false) }
    var showAccidentCard by remember { mutableStateOf(false) }
    var showAccidentDialog by remember { mutableStateOf(false) }

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
                                onNavigateToBlackbox = { 
                                    showPulsaBalanceScreen = false
                                    selectedTab = 1 
                                },
                                showAccidentCard = showAccidentCard,
                                onShowAccidentDialog = { showAccidentDialog = true }
                            )
                            1 -> BlackboxScreen(
                                onNavigateToPulsaBalance = { showPulsaBalanceScreen = true }
                            )
                            2 -> TutorialScreen()
                            3 -> SettingsScreen(
                                onShowAccidentDialog = { showAccidentDialog = true }
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
            onDismiss = { showAccidentDialog = false },
            onCheckLocation = { 
                // Handle check location logic
            },
            onCallEmergencyServices = { 
                // Handle emergency services logic
            },
            onClose = { 
                showAccidentDialog = false
                showAccidentCard = true // Show the card on home screen after closing
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