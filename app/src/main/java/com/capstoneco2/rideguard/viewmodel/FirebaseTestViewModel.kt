package com.capstoneco2.rideguard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneco2.rideguard.data.CrashId
import com.capstoneco2.rideguard.data.CrashIdService
import com.capstoneco2.rideguard.data.RideGuardId
import com.capstoneco2.rideguard.data.RideGuardIdService
import com.capstoneco2.rideguard.service.FCMTokenService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FirebaseTestState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val results: List<String> = emptyList()
)

@HiltViewModel
class FirebaseTestViewModel @Inject constructor(
    private val fcmTokenService: FCMTokenService
) : ViewModel() {
    private val crashIdService = CrashIdService(Firebase.firestore)
    private val rideGuardIdService = RideGuardIdService(Firebase.firestore)
    
    private val _state = MutableStateFlow(FirebaseTestState())
    val state: StateFlow<FirebaseTestState> = _state.asStateFlow()
    
    // Individual state flows for each test section
    private val _crashIdTestResult = MutableStateFlow("")
    val crashIdTestResult: StateFlow<String> = _crashIdTestResult.asStateFlow()
    
    private val _rideGuardIdTestResult = MutableStateFlow("")
    val rideGuardIdTestResult: StateFlow<String> = _rideGuardIdTestResult.asStateFlow()
    
    private val _fcmTokenTestResult = MutableStateFlow("")
    val fcmTokenTestResult: StateFlow<String> = _fcmTokenTestResult.asStateFlow()
    
    private fun addResult(result: String) {
        val currentResults = _state.value.results.toMutableList()
        currentResults.add(0, "[${getCurrentTime()}] $result") // Add to top
        _state.value = _state.value.copy(results = currentResults.take(20)) // Keep only last 20 results
    }
    
    private fun addFCMTokenResult(result: String) {
        val currentResult = _fcmTokenTestResult.value
        val newResult = if (currentResult.isBlank()) {
            "[${getCurrentTime()}] $result"
        } else {
            "$currentResult\n[${getCurrentTime()}] $result"
        }
        _fcmTokenTestResult.value = newResult
    }
    
    private fun getCurrentTime(): String {
        val formatter = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
        return formatter.format(java.util.Date())
    }
    
    // CrashId Service Tests
    
    fun reportCrash(crashId: String, reporterId: String, userId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = crashIdService.reportCrash(crashId, reporterId, userId, latitude, longitude)
                
                if (result.isSuccess) {
                    val isNewCrash = result.getOrNull() ?: false
                    if (isNewCrash) {
                        addResult("✅ NEW CRASH REPORTED: $crashId by $reporterId")
                    } else {
                        addResult("⚠️ DUPLICATE CRASH: $crashId by $reporterId (added as additional report)")
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addResult("❌ FAILED to report crash: $error")
                    _state.value = _state.value.copy(error = error)
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun getCrash(crashId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val crash = crashIdService.getCrashById(crashId)
                
                if (crash != null) {
                    addResult("📋 CRASH FOUND: $crashId")
                    addResult("   Reporter: ${crash.reporterId}")
                    addResult("   User: ${crash.userId}")
                    addResult("   Location: ${crash.latitude}, ${crash.longitude}")
                    addResult("   Processed: ${crash.isProcessed}")
                    addResult("   Additional reports: ${crash.additionalReports.size}")
                } else {
                    addResult("❌ CRASH NOT FOUND: $crashId")
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun markCrashAsProcessing(crashId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = crashIdService.markCrashAsProcessing(crashId)
                
                if (result.isSuccess) {
                    val wasMarked = result.getOrNull() ?: false
                    if (wasMarked) {
                        addResult("✅ CRASH MARKED FOR PROCESSING: $crashId")
                    } else {
                        addResult("⚠️ CRASH ALREADY PROCESSED: $crashId")
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addResult("❌ FAILED to mark crash for processing: $error")
                    _state.value = _state.value.copy(error = error)
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun getUnprocessedCrashes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val crashes = crashIdService.getUnprocessedCrashes()
                addResult("📋 UNPROCESSED CRASHES: ${crashes.size} found")
                crashes.take(5).forEach { crash ->
                    addResult("   - ${crash.crashId} (${crash.reporterId})")
                }
                if (crashes.size > 5) {
                    addResult("   ... and ${crashes.size - 5} more")
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    // RideGuardId Service Tests
    
    fun connectUserToDevice(deviceId: String, userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = rideGuardIdService.connectUserToDevice(deviceId, userId)
                
                if (result.isSuccess) {
                    addResult("✅ USER CONNECTED TO DEVICE: $userId → $deviceId")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addResult("❌ FAILED to connect user to device: $error")
                    _state.value = _state.value.copy(error = error)
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun getRideGuardConnection(deviceId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val connection = rideGuardIdService.getRideGuardConnection(deviceId)
                
                if (connection != null) {
                    addResult("📋 DEVICE CONNECTION: $deviceId")
                    addResult("   Current User: ${connection.currentUserId}")
                    addResult("   Previous User: ${connection.previousUserId ?: "None"}")
                    addResult("   Active: ${connection.isActive}")
                    addResult("   Connected: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(connection.connectedAt))}")
                } else {
                    addResult("❌ DEVICE NOT FOUND: $deviceId")
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun getUserDevices(userId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val devices = rideGuardIdService.getUserDevices(userId)
                addResult("📋 USER DEVICES: ${devices.size} found for $userId")
                devices.forEach { device ->
                    addResult("   - ${device.id} (Active: ${device.isActive})")
                }
                if (devices.isEmpty()) {
                    addResult("   No devices connected to this user")
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun disconnectDevice(deviceId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            try {
                val result = rideGuardIdService.disconnectDevice(deviceId)
                
                if (result.isSuccess) {
                    addResult("✅ DEVICE DISCONNECTED: $deviceId")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addResult("❌ FAILED to disconnect device: $error")
                    _state.value = _state.value.copy(error = error)
                }
            } catch (e: Exception) {
                addResult("❌ EXCEPTION: ${e.message}")
                _state.value = _state.value.copy(error = e.message)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun clearResults() {
        _state.value = _state.value.copy(results = emptyList(), error = null)
    }
    
    fun clearFCMTokenTestResult() {
        _fcmTokenTestResult.value = ""
    }
    
    // FCM Token Service Tests
    
    fun testAddFCMToken(userId: String, userDisplayName: String, fcmToken: String, context: Context) {
        viewModelScope.launch {
            try {
                addFCMTokenResult("🔄 Adding/updating FCM token for user: $userId ($userDisplayName)")
                
                val result = fcmTokenService.saveOrUpdateFCMToken(
                    userId = userId,
                    userDisplayName = userDisplayName,
                    token = fcmToken,
                    context = context
                )
                
                if (result.isSuccess) {
                    addFCMTokenResult("✅ Successfully saved/updated FCM token for user: $userId")
                    addFCMTokenResult("   Display Name: $userDisplayName")
                    addFCMTokenResult("   FCM Token: ${fcmToken.take(20)}...")
                    addFCMTokenResult("   Document ID: ${result.getOrNull()}")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addFCMTokenResult("❌ Failed to save FCM token: $error")
                }
            } catch (e: Exception) {
                addFCMTokenResult("❌ Exception: ${e.message}")
            }
        }
    }
    
    fun testSwitchUser(userId: String, userDisplayName: String, context: Context) {
        viewModelScope.launch {
            try {
                addFCMTokenResult("🔄 Switching device to user: $userId ($userDisplayName)")
                
                val result = fcmTokenService.updateDeviceUser(userId, userDisplayName, context)
                
                if (result.isSuccess) {
                    addFCMTokenResult("✅ Successfully switched device to user: $userId")
                    addFCMTokenResult("   New user: $userDisplayName")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addFCMTokenResult("❌ Failed to switch user: $error")
                }
            } catch (e: Exception) {
                addFCMTokenResult("❌ Exception: ${e.message}")
            }
        }
    }
    
    fun testGetDeviceToken(context: Context) {
        viewModelScope.launch {
            try {
                addFCMTokenResult("🔄 Getting current device token...")
                
                val result = fcmTokenService.getDeviceToken(context)
                
                if (result.isSuccess) {
                    val token = result.getOrNull()
                    if (token != null) {
                        addFCMTokenResult("✅ Device token found:")
                        addFCMTokenResult("   Current User: ${token.userDisplayName} (${token.userId})")
                        addFCMTokenResult("   Device: ${token.deviceName}")
                        addFCMTokenResult("   Last Used: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(token.lastUsedAt))}")
                        addFCMTokenResult("   Token: ${token.token.take(20)}...")
                        addFCMTokenResult("   Document ID: ${token.id}")
                    } else {
                        addFCMTokenResult("⚠️ No FCM token found for this device")
                    }
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addFCMTokenResult("❌ Failed to get device token: $error")
                }
            } catch (e: Exception) {
                addFCMTokenResult("❌ Exception: ${e.message}")
            }
        }
    }
    
    fun testDeactivateToken(context: Context) {
        viewModelScope.launch {
            try {
                addFCMTokenResult("🔄 Deactivating device token...")
                
                val result = fcmTokenService.deactivateDeviceToken(context)
                
                if (result.isSuccess) {
                    addFCMTokenResult("✅ Successfully deactivated device token")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addFCMTokenResult("❌ Failed to deactivate token: $error")
                }
            } catch (e: Exception) {
                addFCMTokenResult("❌ Exception: ${e.message}")
            }
        }
    }
    
    fun testGetActiveTokenCount() {
        viewModelScope.launch {
            try {
                addFCMTokenResult("🔄 Getting active token count...")
                
                val result = fcmTokenService.getActiveTokenCount()
                
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    addFCMTokenResult("✅ Total active FCM tokens: $count")
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Unknown error"
                    addFCMTokenResult("❌ Failed to get token count: $error")
                }
            } catch (e: Exception) {
                addFCMTokenResult("❌ Exception: ${e.message}")
            }
        }
    }
}