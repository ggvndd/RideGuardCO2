package com.capstoneco2.rideguard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capstoneco2.rideguard.data.EmergencyContactInfo
import com.capstoneco2.rideguard.data.UserProfile
import com.capstoneco2.rideguard.service.EmergencyContactService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EmergencyContactState(
    val contacts: List<EmergencyContactInfo> = emptyList(),
    val searchResults: List<UserProfile> = emptyList(),
    val isLoading: Boolean = false,
    val isSearching: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class EmergencyContactViewModel : ViewModel() {
    private val emergencyContactService = EmergencyContactService()
    
    private val _state = MutableStateFlow(EmergencyContactState())
    val state: StateFlow<EmergencyContactState> = _state.asStateFlow()
    
    /**
     * Load emergency contacts for the current user
     */
    fun loadEmergencyContacts(userUid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = emergencyContactService.getEmergencyContacts(userUid)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    contacts = result.getOrNull() ?: emptyList(),
                    isLoading = false
                )
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load emergency contacts"
                )
            }
        }
    }
    
    /**
     * Search for users by username
     */
    fun searchUsers(query: String, currentUserUid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSearching = true, error = null)
            
            val result = emergencyContactService.searchUsersByUsername(query, currentUserUid)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    searchResults = result.getOrNull() ?: emptyList(),
                    isSearching = false
                )
            } else {
                _state.value = _state.value.copy(
                    isSearching = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to search users"
                )
            }
        }
    }
    
    /**
     * Add an emergency contact
     */
    fun addEmergencyContact(ownerUid: String, contactUsername: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = emergencyContactService.addEmergencyContact(ownerUid, contactUsername)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Successfully added $contactUsername as emergency contact",
                    searchResults = emptyList() // Clear search results
                )
                // Reload contacts to show the new addition
                loadEmergencyContacts(ownerUid)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to add emergency contact"
                )
            }
        }
    }
    
    /**
     * Remove an emergency contact
     */
    fun removeEmergencyContact(contactId: String, userUid: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = emergencyContactService.removeEmergencyContact(contactId)
            if (result.isSuccess) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Emergency contact removed successfully"
                )
                // Reload contacts to reflect the removal
                loadEmergencyContacts(userUid)
            } else {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to remove emergency contact"
                )
            }
        }
    }
    
    /**
     * Clear messages
     */
    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }
    
    /**
     * Clear search results
     */
    fun clearSearchResults() {
        _state.value = _state.value.copy(searchResults = emptyList())
    }
}