package com.capstoneco2.rideguard

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.capstoneco2.rideguard.service.FCMTokenService
import com.capstoneco2.rideguard.ui.screens.MainApp
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import android.content.Context
import com.capstoneco2.rideguard.ui.screens.SignInPage
import com.capstoneco2.rideguard.ui.screens.SignUpPage
import com.capstoneco2.rideguard.ui.screens.WelcomePage
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import kotlinx.coroutines.launch

enum class AppScreen {
    WELCOME,
    SIGN_UP,
    SIGN_IN,
    MAIN_APP
}

class MainActivity : ComponentActivity() {
    
    private val fcmTokenService = FCMTokenService()
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get FCM registration token
        retrieveFCMToken()
        
        enableEdgeToEdge()
        setContent {
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(
                        onAuthSuccess = {
                            savePendingFCMToken()
                            performPeriodicFCMTokenCleanup()
                        }
                    )
                }
            }
        }
    }
    
    private fun retrieveFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "FCM Registration Token: $token")
                Log.i(TAG, "Token length: ${token?.length}")
                
                // Print token in a way that's easy to copy from logs
                println("=== FCM REGISTRATION TOKEN ===")
                println(token)
                println("==============================")
                
                // Save token to database (when user is authenticated)
                saveFCMTokenToDatabase(token)
            }
    }
    
    /**
     * Save FCM token to Firestore database
     * Note: This will only work when user is authenticated
     */
    private fun saveFCMTokenToDatabase(token: String?) {
        if (token == null) {
            Log.w(TAG, "FCM token is null, cannot save to database")
            return
        }
        
        lifecycleScope.launch {
            try {
                // TODO: Get current user ID from AuthViewModel
                // For now, we'll use a placeholder or wait for authentication
                
                // Mock implementation - replace with actual user ID when available
                val currentUserId = getCurrentUserId()
                
                if (currentUserId != null) {
                    Log.d(TAG, "Saving FCM token to database for user: $currentUserId")
                    
                    val result = fcmTokenService.saveOrUpdateFCMToken(
                        userId = currentUserId,
                        token = token,
                        context = this@MainActivity,
                        appVersion = getAppVersion()
                    )
                    
                    if (result.isSuccess) {
                        Log.i(TAG, "Successfully saved FCM token to database")
                    } else {
                        Log.e(TAG, "Failed to save FCM token: ${result.exceptionOrNull()?.message}")
                    }
                } else {
                    Log.d(TAG, "User not authenticated yet, FCM token will be saved after login")
                    // TODO: Store token temporarily and save it after user authentication
                    storeTokenForLaterSave(token)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while saving FCM token", e)
            }
        }
    }
    
    /**
     * Get current authenticated user ID
     */
    private fun getCurrentUserId(): String? {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
    }
    
    /**
     * Get current app version
     */
    private fun getAppVersion(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
    
    /**
     * Store FCM token temporarily for saving after authentication
     */
    private fun storeTokenForLaterSave(token: String) {
        Log.d(TAG, "Storing FCM token for later save: ${token.takeLast(10)}")
        
        val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        sharedPref.edit()
            .putString("pending_fcm_token", token)
            .putLong("token_timestamp", System.currentTimeMillis())
            .apply()
        
        Log.i(TAG, "FCM token stored temporarily in SharedPreferences, will be saved after user authentication")
    }

    /**
     * Save any pending FCM token after user authentication
     */
    private fun savePendingFCMToken() {
        lifecycleScope.launch {
            try {
                val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                val pendingToken = sharedPref.getString("pending_fcm_token", null)
                
                if (pendingToken != null && getCurrentUserId() != null) {
                    Log.d(TAG, "Found pending FCM token, saving to database")
                    saveFCMTokenToDatabase(pendingToken)
                    
                    // Clear the pending token after saving
                    sharedPref.edit()
                        .remove("pending_fcm_token")
                        .remove("token_timestamp")
                        .apply()
                    
                    Log.i(TAG, "Pending FCM token saved and cleared from temporary storage")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save pending FCM token", e)
            }
        }
    }

    /**
     * Perform periodic cleanup of inactive FCM tokens
     */
    private fun performPeriodicFCMTokenCleanup() {
        lifecycleScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    Log.d(TAG, "Performing periodic FCM token cleanup")
                    
                    val result = fcmTokenService.cleanupInactiveFCMTokens(userId)
                    if (result.isSuccess) {
                        val deletedCount = result.getOrNull() ?: 0
                        Log.i(TAG, "Periodic cleanup completed: $deletedCount inactive tokens removed")
                    } else {
                        Log.w(TAG, "Periodic cleanup failed: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during periodic FCM token cleanup", e)
            }
        }
    }
}

@Composable
fun MyApp(
    onAuthSuccess: () -> Unit = {}
) {
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
                        // Firebase auth success - save pending FCM token, cleanup old tokens, and navigate to main app
                        onAuthSuccess()
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
                        // Firebase auth success - save pending FCM token, cleanup old tokens, and navigate to main app
                        onAuthSuccess()
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