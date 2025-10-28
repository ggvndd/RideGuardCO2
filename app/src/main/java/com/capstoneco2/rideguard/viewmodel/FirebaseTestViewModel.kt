package com.capstoneco2.rideguard.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import com.capstoneco2.rideguard.service.FCMTokenService
import javax.inject.Inject

data class FirebaseTestState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val results: List<String> = emptyList()
)

@HiltViewModel
class FirebaseTestViewModel @Inject constructor(
    private val fcmTokenService: FCMTokenService
) : ViewModel()