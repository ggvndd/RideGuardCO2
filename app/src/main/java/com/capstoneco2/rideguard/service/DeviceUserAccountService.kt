package com.capstoneco2.rideguard.service

import android.content.Context
import android.util.Log
import com.capstoneco2.rideguard.data.FCMToken
import kotlinx.coroutines.flow.MutableStateFlow
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
    
    private val _allDeviceUsers = MutableStateFlow<List<FCMToken>>(emptyList())
    
    /**
     * Register a new user account on this device
     * This is called when a user signs up or logs in for the first time on this device
     */
    suspend fun registerUserAccount(
        userId: String,
        userDisplayName: String,
        fcmToken: String,
        context: Context
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
                
                Log.d("DeviceUserAccountService", "Successfully registered user account: $userId ($userDisplayName)")
            }
            
            result
        } catch (e: Exception) {
            Log.e("DeviceUserAccountService", "Failed to register user account: $userId", e)
            Result.failure(e)
        }
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