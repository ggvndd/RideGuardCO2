package com.capstoneco2.rideguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.capstoneco2.rideguard.ui.screens.SignInPage
import com.capstoneco2.rideguard.ui.screens.SignUpPage
import com.capstoneco2.rideguard.ui.screens.WelcomePage
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

enum class OnboardingScreen {
    WELCOME,
    SIGN_UP,
    SIGN_IN
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
                    OnboardingNavigation()
                }
            }
        }
    }
}

@Composable
fun OnboardingNavigation() {
    var currentScreen by remember { mutableStateOf(OnboardingScreen.WELCOME) }
    
    when (currentScreen) {
        OnboardingScreen.WELCOME -> {
            WelcomePage(
                onGetStartedClick = {
                    currentScreen = OnboardingScreen.SIGN_UP
                }
            )
        }
        
        OnboardingScreen.SIGN_UP -> {
            SignUpPage(
                onSignUpClick = { username, phone, email, password, confirmPassword ->
                    // Handle sign up logic here
                    // For now, just show a simple success (in real app, you'd call your API)
                    println("Sign Up attempted with:")
                    println("Username: $username")
                    println("Phone: $phone")
                    println("Email: $email")
                    // You might navigate to a main app screen or show success
                },
                onSignInClick = {
                    currentScreen = OnboardingScreen.SIGN_IN
                }
            )
        }
        
        OnboardingScreen.SIGN_IN -> {
            SignInPage(
                onSignInClick = { username, password ->
                    // Handle sign in logic here
                    // For now, just show a simple success (in real app, you'd call your API)
                    println("Sign In attempted with:")
                    println("Username: $username")
                    // You might navigate to a main app screen or show success
                },
                onSignUpClick = {
                    currentScreen = OnboardingScreen.SIGN_UP
                },
                onForgotPasswordClick = {
                    // Handle forgot password logic
                    println("Forgot password clicked")
                }
            )
        }
    }
}