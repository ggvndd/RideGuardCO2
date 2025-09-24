package com.capstoneco2.rideguard.auth

import com.capstoneco2.rideguard.data.UserProfile
import com.capstoneco2.rideguard.service.UserProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {
    private val authService = FirebaseAuthService()
    private val userProfileService = UserProfileService()
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    init {
        // Initialize with current user state
        _authState.value = AuthState(
            isSignedIn = authService.isUserSignedIn,
            user = authService.currentUser
        )
        
        // Note: Initial profile loading will be handled by the ViewModel when needed
    }
    
    suspend fun signUp(email: String, password: String, username: String, phoneNumber: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        when (val result = authService.signUp(email, password)) {
            is AuthResult.Success -> {
                // Create user profile in Firestore
                val userProfile = UserProfile(
                    uid = result.user?.uid ?: "",
                    username = username,
                    email = email,
                    phoneNumber = phoneNumber
                )
                
                val profileResult = userProfileService.saveUserProfile(userProfile)
                if (profileResult.isSuccess) {
                    _authState.value = AuthState(
                        isSignedIn = true,
                        user = result.user,
                        userProfile = userProfile,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "Failed to create user profile: ${profileResult.exceptionOrNull()?.message}"
                    )
                }
            }
            is AuthResult.Error -> {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
    
    suspend fun signIn(email: String, password: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        when (val result = authService.signIn(email, password)) {
            is AuthResult.Success -> {
                // Load user profile from Firestore
                result.user?.uid?.let { uid ->
                    loadUserProfile(uid)
                } ?: run {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        error = "User UID is null"
                    )
                }
            }
            is AuthResult.Error -> {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
    
    fun signOut() {
        authService.signOut()
        _authState.value = AuthState(
            isSignedIn = false,
            user = null,
            userProfile = null,
            isLoading = false,
            error = null
        )
    }
    
    private suspend fun loadUserProfile(uid: String) {
        val profileResult = userProfileService.getUserProfile(uid)
        
        if (profileResult.isSuccess) {
            val userProfile = profileResult.getOrNull()
            _authState.value = AuthState(
                isSignedIn = true,
                user = authService.currentUser,
                userProfile = userProfile,
                isLoading = false,
                error = null
            )
        } else {
            _authState.value = _authState.value.copy(
                isLoading = false,
                error = "Failed to load user profile: ${profileResult.exceptionOrNull()?.message}"
            )
        }
    }
    
    suspend fun sendPasswordResetEmail(email: String) {
        _authState.value = _authState.value.copy(isLoading = true, error = null)
        
        when (val result = authService.sendPasswordResetEmail(email)) {
            is AuthResult.Success -> {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
            is AuthResult.Error -> {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
    
    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}

// Data class for auth state
data class AuthState(
    val isSignedIn: Boolean = false,
    val user: com.google.firebase.auth.FirebaseUser? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)