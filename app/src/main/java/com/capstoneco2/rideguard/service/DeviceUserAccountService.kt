package com.capstoneco2.rideguard.service

import android.content.Context
import android.util.Log
import com.capstoneco2.rideguard.data.FCMToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing multiple user accounts on a single device
 * Handles user account switching, primary user management, and device-level operations
 */
@Singleton
class DeviceUserAccountService @Inject constructor(
    private val fcmTokenService: FCMTokenService
) {
    
    private val _currentUser = MutableStateFlow<FCMToken?>(null)
    val currentUser: StateFlow<FCMToken?> = _currentUser.asStateFlow()
    
    private val _allDeviceUsers = MutableStateFlow<List<FCMToken>>(emptyList())
    val allDeviceUsers: StateFlow<List<FCMToken>> = _allDeviceUsers.asStateFlow()
    
    /**
     * Register a new user account on this device
     * This is called when a user signs up or logs in for the first time on this device
     */
    suspend fun registerUserAccount(
        userId: String,
        userDisplayName: String,
        fcmToken: String,
        context: Context,
        appVersion: String = "1.0"
    ): Result<String> {
        return try {
            // Save FCM token for this user account
            val result = fcmTokenService.saveOrUpdateFCMToken(
                userId = userId,
                userDisplayName = userDisplayName,
                token = fcmToken,
                context = context
            )
            
            if (result.isSuccess) {
                // If this is the first user on the device, make them primary
                val userCount = fcmTokenService.getDeviceUserCount(context).getOrNull() ?: 0
                if (userCount == 1) {
                    fcmTokenService.setPrimaryUser(userId, context)
                }
                
                // Refresh device users list
                refreshDeviceUsers(context)
                
                // Set as current user
                setCurrentUser(userId, context)
                
                Log.d("DeviceUserAccountService", "Successfully registered user account: $userId ($userDisplayName)")
            }
            
            result
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to register user account: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Switch to a different user account on this device
     */
    suspend fun switchUserAccount(userId: String, context: Context): Result<Unit> {
        return try {
            // Update last used time for the user
            fcmTokenService.updateLastUsed(userId, context)
            
            // Set as current user
            setCurrentUser(userId, context)
            
            // Refresh device users list to update last used times
            refreshDeviceUsers(context)
            
            Log.d("DeviceUserAccountService", "Switched to user account: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to switch user account: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Set a user as the primary user on this device
     */
    suspend fun setPrimaryUserAccount(userId: String, context: Context): Result<Unit> {
        return try {
            val result = fcmTokenService.setPrimaryUser(userId, context)
            
            if (result.isSuccess) {
                // Refresh device users list to reflect primary status change
                refreshDeviceUsers(context)
                
                // Set as current user if not already
                setCurrentUser(userId, context)
                
                Log.d("DeviceUserAccountService", "Set primary user account: $userId")
            }
            
            result
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to set primary user account: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Remove a user account from this device (when user logs out)
     */
    suspend fun removeUserAccount(userId: String, context: Context): Result<Unit> {
        return try {
            // Deactivate the user's FCM token
            val result = fcmTokenService.deactivateUserToken(userId, context)
            
            if (result.isSuccess) {
                // If this was the current user, clear current user
                if (_currentUser.value?.userId == userId) {
                    _currentUser.value = null
                }
                
                // Refresh device users list
                refreshDeviceUsers(context)
                
                // If there are still users, set the most recently used as current
                val remainingUsers = _allDeviceUsers.value
                if (remainingUsers.isNotEmpty()) {
                    val nextUser = remainingUsers.maxByOrNull { it.lastUsedAt }
                    nextUser?.let { setCurrentUser(it.userId, context) }
                }
                
                Log.d("DeviceUserAccountService", "Removed user account: $userId")
            }
            
            result
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to remove user account: $userId", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get all user accounts on this device
     */
    suspend fun getDeviceUserAccounts(context: Context): Result<List<FCMToken>> {
        return try {
            val result = fcmTokenService.getDeviceUserTokens(context)
            
            if (result.isSuccess) {
                val users = result.getOrNull() ?: emptyList()
                _allDeviceUsers.value = users
                
                Log.d("DeviceUserAccountService", "Retrieved ${users.size} user accounts on device")
            }
            
            result
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to get device user accounts", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get the primary user account on this device
     */
    suspend fun getPrimaryUserAccount(context: Context): Result<FCMToken?> {
        return try {
            fcmTokenService.getPrimaryUser(context)
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to get primary user account", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if a user account exists on this device
     */
    suspend fun hasUserAccount(userId: String, context: Context): Boolean {
        return try {
            val users = fcmTokenService.getDeviceUserTokens(context).getOrNull() ?: emptyList()
            users.any { it.userId == userId }
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to check user account existence: $userId", e)
            false
        }
    }
    
    /**
     * Initialize the service by loading existing user accounts
     */
    suspend fun initialize(context: Context): Result<Unit> {
        return try {
            // Load all device users
            refreshDeviceUsers(context)
            
            // Load primary user as current if no current user is set
            if (_currentUser.value == null) {
                val primaryUser = fcmTokenService.getPrimaryUser(context).getOrNull()
                _currentUser.value = primaryUser
            }
            
            Log.d("DeviceUserAccountService", "Initialized device user account service")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to initialize device user account service", e)
            Result.failure(e)
        }
    }
    
    /**
     * Private helper to set current user
     */
    private suspend fun setCurrentUser(userId: String, context: Context) {
        val users = _allDeviceUsers.value
        val user = users.find { it.userId == userId }
        _currentUser.value = user
    }
    
    /**
     * Private helper to refresh device users list
     */
    private suspend fun refreshDeviceUsers(context: Context) {
        val result = fcmTokenService.getDeviceUserTokens(context)
        if (result.isSuccess) {
            _allDeviceUsers.value = result.getOrNull() ?: emptyList()
        }
    }
}