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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.service.FCMTokenService
import com.capstoneco2.rideguard.ui.screens.MainApp
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import android.Manifest
import com.capstoneco2.rideguard.service.SmsService
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
    
    private val fcmTokenService = FCMTokenService(Firebase.firestore)
    private val smsService = SmsService()
    private var intentUpdateCallback: ((Intent?) -> Unit)? = null
    

    
    // Modern permission request launcher
    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readSmsGranted = permissions[Manifest.permission.READ_SMS] == true
        val receiveSmsGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        
        if (readSmsGranted && receiveSmsGranted) {
            onSmsPermissionsGranted()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get FCM registration token
        retrieveFCMToken()
        
        // Request SMS permissions and test SMS reading
        requestSmsPermissions()
        
        enableEdgeToEdge()
        setContent {
            // Create a state to track the current intent
            var currentIntent by remember { mutableStateOf(intent) }
            
            // Set up the callback to update intent state
            DisposableEffect(Unit) {
                intentUpdateCallback = { newIntent ->
                    currentIntent = newIntent
                }
                onDispose {
                    intentUpdateCallback = null
                }
            }
            
            MyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(
                        intent = currentIntent,
                        onAuthSuccess = {
                            savePendingFCMToken()
                            performPeriodicFCMTokenCleanup()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentUpdateCallback?.invoke(intent)
    }
    
    private fun retrieveFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
        if (!task.isSuccessful) {
            return@addOnCompleteListener
        }                // Get new FCM registration token
                val token = task.result
                
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
            return
        }
        
        lifecycleScope.launch {
            try {

                val currentUserId = getCurrentUserId()
                
                if (currentUserId != null) {
                    val result = fcmTokenService.saveOrUpdateFCMToken(
                        userId = currentUserId,
                        userDisplayName = getCurrentUserDisplayName() ?: "Unknown User",
                        token = token,
                        context = this@MainActivity
                    )
                    
                    // FCM token save result handled internally
                } else {
                    storeTokenForLaterSave(token)
                }
            } catch (e: Exception) {
                // Exception handled silently
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
     * Get current authenticated user display name
     */
    private fun getCurrentUserDisplayName(): String? {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return currentUser?.displayName ?: currentUser?.email?.substringBefore("@")
    }
    

    
    /**
     * Store FCM token temporarily for saving after authentication
     */
    private fun storeTokenForLaterSave(token: String) {
        val sharedPref = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString("pending_fcm_token", token)
            putLong("token_timestamp", System.currentTimeMillis())
        }
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
                    saveFCMTokenToDatabase(pendingToken)
                    
                    // Clear the pending token after saving
                    sharedPref.edit {
                        remove("pending_fcm_token")
                        remove("token_timestamp")
                    }
                }
            } catch (e: Exception) {
                // Exception handled silently
            }
        }
    }

    /**
     * Perform periodic cleanup of inactive FCM tokens
     * Now includes safety checks to avoid index errors on empty collections
     */
    private fun performPeriodicFCMTokenCleanup() {
        lifecycleScope.launch {
            try {
                val userId = getCurrentUserId()
                if (userId != null) {
                    val result = fcmTokenService.cleanupInactiveFCMTokens(this@MainActivity)
                    // Cleanup result handled internally
                }
            } catch (e: Exception) {
                // Exception handled silently
            }
        }
    }

    /**
     * Request SMS permissions for reading incoming SMS messages
     */
    private fun requestSmsPermissions() {
        val permissionsNeeded = mutableListOf<String>()
        
        // Check READ_SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS)
        }
        
        // Check RECEIVE_SMS permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS)
        }
        
        if (permissionsNeeded.isNotEmpty()) {
            smsPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            onSmsPermissionsGranted()
        }
    }
    

    
    /**
     * Called when SMS permissions are granted - initialize SMS functionality
     */
    private fun onSmsPermissionsGranted() {
        // SMS functionality initialized
    }
    

    

}

@Composable
fun MyApp(
    intent: Intent? = null,
    onAuthSuccess: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    
    // Determine initial screen based on auth state
    var currentScreen by remember { 
        mutableStateOf(
            if (authState.isSignedIn && authState.userProfile != null) {
                AppScreen.MAIN_APP
            } else {
                AppScreen.WELCOME
            }
        )
    }
    
    // Update screen when auth state changes
    LaunchedEffect(authState.isSignedIn, authState.userProfile) {
        if (authState.isSignedIn && authState.userProfile != null && currentScreen != AppScreen.MAIN_APP) {
            currentScreen = AppScreen.MAIN_APP
        } else if (!authState.isSignedIn && currentScreen == AppScreen.MAIN_APP) {
            currentScreen = AppScreen.WELCOME
        }
    }
    
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
                        onAuthSuccess()
                        currentScreen = AppScreen.MAIN_APP
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
                        onAuthSuccess()
                        currentScreen = AppScreen.MAIN_APP
                    },
                    onSignUpClick = {
                        currentScreen = AppScreen.SIGN_UP
                    },
                    onForgotPasswordClick = {
                        // Forgot password functionality can be added here
                    },
                    authViewModel = authViewModel
                )
            }
            
            AppScreen.MAIN_APP -> {
                MainApp(
                    username = authState.userProfile?.username ?: "User",
                    intent = intent,
                    onLogout = {
                    currentScreen = AppScreen.WELCOME
                },
                    authViewModel = authViewModel
                )
            }
        }
    }
}