package com.capstoneco2.rideguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneco2.rideguard.auth.AuthRepository
import com.capstoneco2.rideguard.auth.AuthState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    val authState: StateFlow<AuthState> = authRepository.authState
    
    fun signUp(email: String, password: String, username: String, phoneNumber: String) {
        viewModelScope.launch {
            authRepository.signUp(email, password, username, phoneNumber)
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signIn(email, password)
        }
    }
    
    fun signOut() {
        authRepository.signOut()
    }
    
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email)
        }
    }
    
    fun clearError() {
        authRepository.clearError()
    }
}