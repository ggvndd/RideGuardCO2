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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.service.FCMTokenService
import com.capstoneco2.rideguard.ui.screens.MainApp
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    
    companion object {
        private const val TAG = "MainActivity"
        private const val SMS_PERMISSION_REQUEST_CODE = 123
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get FCM registration token
        retrieveFCMToken()
        
        // Request SMS permissions and test SMS reading
        requestSmsPermissions()
        
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
                        userDisplayName = getCurrentUserDisplayName() ?: "Unknown User",
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
     * Get current authenticated user display name
     */
    private fun getCurrentUserDisplayName(): String? {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        return currentUser?.displayName ?: currentUser?.email?.substringBefore("@")
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
                    
                    val result = fcmTokenService.cleanupInactiveFCMTokens()
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
            Log.d(TAG, "Requesting SMS permissions: ${permissionsNeeded.joinToString(", ")}")
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                SMS_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.i(TAG, "SMS permissions already granted")
            onSmsPermissionsGranted()
        }
    }
    
    /**
     * Handle permission request results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            SMS_PERMISSION_REQUEST_CODE -> {
                val readSmsGranted = grantResults.isNotEmpty() && 
                    permissions.contains(Manifest.permission.READ_SMS) &&
                    grantResults[permissions.indexOf(Manifest.permission.READ_SMS)] == PackageManager.PERMISSION_GRANTED
                
                val receiveSmsGranted = grantResults.isNotEmpty() && 
                    permissions.contains(Manifest.permission.RECEIVE_SMS) &&
                    grantResults[permissions.indexOf(Manifest.permission.RECEIVE_SMS)] == PackageManager.PERMISSION_GRANTED
                
                if (readSmsGranted && receiveSmsGranted) {
                    Log.i(TAG, "✅ SMS permissions granted successfully!")
                    onSmsPermissionsGranted()
                } else {
                    Log.w(TAG, "❌ SMS permissions denied. SMS reading functionality will not work.")
                    Log.w(TAG, "READ_SMS granted: $readSmsGranted")
                    Log.w(TAG, "RECEIVE_SMS granted: $receiveSmsGranted")
                }
            }
        }
    }
    
    /**
     * Called when SMS permissions are granted - initialize SMS functionality
     */
    private fun onSmsPermissionsGranted() {
        Log.i(TAG, "🎉 SMS functionality initialized!")
        Log.i(TAG, "📱 The app will now log all incoming SMS messages to console")
        Log.i(TAG, "📱 Emergency keywords will be detected and highlighted")
        
        // Test reading existing SMS messages for debugging
        testReadExistingSmsMessages()
        
        // Log that the receiver is ready
        Log.i(TAG, "📡 SMS Receiver is now active and waiting for incoming messages...")
        Log.i(TAG, "📡 Send an SMS to this device to see it logged in the console!")
        
        // Print some debugging info
        printSmsDebuggingInfo()
    }
    
    /**
     * Test reading existing SMS messages from device
     */
    private fun testReadExistingSmsMessages() {
        Log.d(TAG, "🧪 Testing SMS reading functionality...")
        
        lifecycleScope.launch {
            try {
                val existingMessages = smsService.getAllSmsMessages(this@MainActivity)
                Log.i(TAG, "📊 Found ${existingMessages.size} existing SMS messages")
                
                if (existingMessages.isEmpty()) {
                    Log.i(TAG, "📱 No existing SMS messages found. Send an SMS to test the functionality!")
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error testing SMS reading", e)
            }
        }
    }
    
    /**
     * Print debugging information for SMS functionality
     */
    private fun printSmsDebuggingInfo() {
        Log.i(TAG, "╔════════════════════════════════════════════════════════════════")
        Log.i(TAG, "║ SMS DEBUGGING INFORMATION")
        Log.i(TAG, "╠════════════════════════════════════════════════════════════════")
        Log.i(TAG, "║ 📱 SMS functionality is now ACTIVE")
        Log.i(TAG, "║ 📡 Listening for incoming SMS messages...")
        Log.i(TAG, "║ 🚨 Emergency keyword detection is ENABLED")
        Log.i(TAG, "║ 📝 All SMS will be logged to Android Studio console")
        Log.i(TAG, "║ ")
        Log.i(TAG, "║ Emergency Keywords Monitored:")
        Log.i(TAG, "║ • English: help, emergency, urgent, accident, danger, etc.")
        Log.i(TAG, "║ • Spanish: socorro, emergencia, urgente, accidente, etc.")
        Log.i(TAG, "║ ")
        Log.i(TAG, "║ To Test:")
        Log.i(TAG, "║ 1. Send an SMS to this device from another phone")
        Log.i(TAG, "║ 2. Check Android Studio Logcat for SMS logs")
        Log.i(TAG, "║ 3. Try sending SMS with word 'emergency' to test detection")
        Log.i(TAG, "╚════════════════════════════════════════════════════════════════")
        
        // Also print to system console for easier debugging
        println("=== SMS FUNCTIONALITY ACTIVE ===")
        println("📱 Ready to intercept and log SMS messages")
        println("🚨 Emergency detection enabled")
        println("📝 Check Android Studio Logcat for detailed SMS logs")
        println("================================")
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