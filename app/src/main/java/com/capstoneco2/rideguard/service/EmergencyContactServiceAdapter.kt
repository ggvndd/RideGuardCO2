package com.capstoneco2.rideguard.service

import com.capstoneco2.rideguard.data.EmergencyContactInfo
import com.capstoneco2.rideguard.data.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Adapter service for emergency contacts to maintain compatibility with existing UI
 * Now uses nested emergency contacts in user profiles instead of separate collection
 */
@Singleton
class EmergencyContactServiceAdapter @Inject constructor(
    private val userProfileService: UserProfileService
) {
    companion object {
        const val MAX_EMERGENCY_CONTACTS = 5
    }
    
    /**
     * Add an emergency contact by username (compatible with old interface)
     */
    suspend fun addEmergencyContact(ownerUid: String, contactUsername: String): Result<String> {
        return try {
            // First, check if owner already has 5 contacts
            val currentContacts = getEmergencyContacts(ownerUid).getOrNull() ?: emptyList()
            if (currentContacts.size >= MAX_EMERGENCY_CONTACTS) {
                return Result.failure(Exception("Maximum of $MAX_EMERGENCY_CONTACTS emergency contacts allowed"))
            }
            
            // Find the user by username
            val contactUserProfile = searchUsersByUsername(contactUsername, ownerUid).getOrNull()?.firstOrNull()
            
            if (contactUserProfile == null) {
                return Result.failure(Exception("User with username '$contactUsername' not found"))
            }
            
            val contactUid = contactUserProfile.uid
            
            // Check if user is trying to add themselves
            if (ownerUid == contactUid) {
                return Result.failure(Exception("Cannot add yourself as emergency contact"))
            }
            
            // Add emergency contact to user profile
            val result = userProfileService.addEmergencyContact(ownerUid, contactUsername, contactUid)
            
            if (result.isSuccess) {
                Result.success(contactUid) // Return UID as the "contact ID"
            } else {
                result.map { contactUid }
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all emergency contacts for a user (compatible with old interface)
     */
    suspend fun getEmergencyContacts(ownerUid: String): Result<List<EmergencyContactInfo>> {
        return try {
            val userProfile = userProfileService.getUserProfile(ownerUid).getOrNull()
            
            if (userProfile != null) {
                val contactInfoList = mutableListOf<EmergencyContactInfo>()
                
                for (contact in userProfile.emergencyContacts.filter { it.isActive }) {
                    // Get full user profile for this contact
                    val contactProfile = userProfileService.getUserProfile(contact.contactUid).getOrNull()
                    if (contactProfile != null) {
                        contactInfoList.add(
                            EmergencyContactInfo(
                                contactId = contact.contactUid, // Use UID as contact ID
                                username = contactProfile.username,
                                uid = contactProfile.uid,
                                email = contactProfile.email,
                                phoneNumber = contactProfile.phoneNumber,
                                addedAt = contact.addedAt
                            )
                        )
                    }
                }
                
                // Sort by addedAt
                val sortedContacts = contactInfoList.sortedBy { it.addedAt }
                Result.success(sortedContacts)
            } else {
                Result.success(emptyList())
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove an emergency contact (compatible with old interface)
     */
    suspend fun removeEmergencyContact(ownerUid: String, contactId: String): Result<Unit> {
        return try {
            // contactId is now the contactUid
            userProfileService.removeEmergencyContact(ownerUid, contactId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search users by username (compatible with old interface)
     */
    suspend fun searchUsersByUsername(query: String, currentUserUid: String): Result<List<UserProfile>> {
        return try {
            userProfileService.searchUsersByUsername(query, currentUserUid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get count of emergency contacts for a user (compatible with old interface)
     */
    suspend fun getEmergencyContactCount(ownerUid: String): Result<Int> {
        return try {
            val contacts = userProfileService.getEmergencyContacts(ownerUid).getOrNull() ?: emptyList()
            Result.success(contacts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}