package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.capstoneco2.rideguard.ui.components.BottomNavigationBar
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun MainApp(
    userName: String = "User",
    userEmail: String = "user@example.com"
) {
    var currentRoute by remember { mutableStateOf("home") }
    
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    currentRoute = route
                }
            )
        }
    ) { paddingValues ->
        when (currentRoute) {
            "home" -> {
                HomeScreen(
                    userName = userName
                )
            }
            "blackbox" -> {
                BlackboxScreen()
            }
            "tutorial" -> {
                TutorialScreen()
            }
            "settings" -> {
                SettingsScreen(
                    userName = userName,
                    userEmail = userEmail
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    MyAppTheme {
        MainApp(
            userName = "John Doe",
            userEmail = "john.doe@example.com"
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainAppDarkPreview() {
    MyAppTheme(darkTheme = true) {
        MainApp(
            userName = "John Doe",
            userEmail = "john.doe@example.com"
        )
    }
}
