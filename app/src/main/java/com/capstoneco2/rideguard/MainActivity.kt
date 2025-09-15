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
import androidx.compose.ui.Modifier
import com.capstoneco2.rideguard.ui.screens.MainApp
import com.capstoneco2.rideguard.ui.screens.SignInPage
import com.capstoneco2.rideguard.ui.screens.SignUpPage
import com.capstoneco2.rideguard.ui.screens.WelcomePage
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

enum class AppScreen {
    WELCOME,
    SIGN_UP,
    SIGN_IN,
    MAIN_APP
}

data class UserData(
    val username: String = "",
    val email: String = ""
)

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
                    RideGuardApp()
                }
            }
        }
    }
}

@Composable
fun RideGuardApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.WELCOME) }
    var userData by remember { mutableStateOf(UserData()) }
    
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
                    onSignUpClick = { username, phone, email, password, confirmPassword ->
                        // Handle sign up logic here
                        // For now, simulate successful sign up and navigate to main app
                        userData = UserData(username = username, email = email)
                        currentScreen = AppScreen.MAIN_APP
                        
                        // In a real app, you'd call your authentication API here
                        println("Sign Up successful for:")
                        println("Username: $username")
                        println("Phone: $phone")
                        println("Email: $email")
                    },
                    onSignInClick = {
                        currentScreen = AppScreen.SIGN_IN
                    }
                )
            }
            
            AppScreen.SIGN_IN -> {
                SignInPage(
                    onSignInClick = { username, password ->
                        // Handle sign in logic here
                        // For now, simulate successful sign in and navigate to main app
                        userData = UserData(username = username, email = "$username@example.com")
                        currentScreen = AppScreen.MAIN_APP
                        
                        // In a real app, you'd call your authentication API here
                        println("Sign In successful for:")
                        println("Username: $username")
                    },
                    onSignUpClick = {
                        currentScreen = AppScreen.SIGN_UP
                    },
                    onForgotPasswordClick = {
                        // Handle forgot password logic
                        println("Forgot password clicked")
                    }
                )
            }
            
            AppScreen.MAIN_APP -> {
                MainApp(username = userData.username)
            }
        }
    }
}