package com.capstoneco2.rideguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstoneco2.rideguard.ui.screens.MainApp
import com.capstoneco2.rideguard.ui.screens.SignInPage
import com.capstoneco2.rideguard.ui.screens.SignUpPage
import com.capstoneco2.rideguard.ui.screens.WelcomePage
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import com.capstoneco2.rideguard.viewmodel.AuthViewModel

enum class AppScreen {
    WELCOME,
    SIGN_UP,
    SIGN_IN,
    MAIN_APP
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            val slideDirection = when (targetState) {
                AppScreen.WELCOME -> -1 // Slide from left
                AppScreen.SIGN_UP -> if (initialState == AppScreen.WELCOME) 1 else -1 // Slide from right if coming from welcome, left if from sign in
                AppScreen.SIGN_IN -> if (initialState == AppScreen.SIGN_UP) 1 else -1 // Slide from right if coming from sign up
                AppScreen.MAIN_APP -> 1 // Slide from right
            }
            
            slideInHorizontally(
                initialOffsetX = { fullWidth -> slideDirection * fullWidth }
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -slideDirection * fullWidth }
            )
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            AppScreen.WELCOME -> {
                WelcomePage(
                    onGetStartedClick = {
                        currentScreen = AppScreen.SIGN_UP
                    }
                )
            }
            
            AppScreen.SIGN_UP -> {
                SignUpPage(
                    onSignUpSuccess = {
                        // Firebase auth success - navigate to main app
                        currentScreen = AppScreen.MAIN_APP
                        println("Sign Up successful with Firebase")
                    },
                    onSignInClick = {
                        currentScreen = AppScreen.SIGN_IN
                    },
                    authViewModel = authViewModel
                )
            }
            
            AppScreen.SIGN_IN -> {
                SignInPage(
                    onSignInSuccess = {
                        // Firebase auth success - navigate to main app
                        currentScreen = AppScreen.MAIN_APP
                        println("Sign In successful with Firebase")
                    },
                    onSignUpClick = {
                        currentScreen = AppScreen.SIGN_UP
                    },
                    onForgotPasswordClick = {
                        // Handle forgot password logic
                        println("Forgot password clicked")
                    },
                    authViewModel = authViewModel
                )
            }
            
            AppScreen.MAIN_APP -> {
                MainApp(
                    username = authState.userProfile?.username ?: "User",
                    onLogout = {
                        // Navigate back to welcome screen after logout
                        currentScreen = AppScreen.WELCOME
                    },
                    authViewModel = authViewModel
                )
            }
        }
    }
}