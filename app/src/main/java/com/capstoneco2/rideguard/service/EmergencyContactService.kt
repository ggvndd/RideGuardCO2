package com.capstoneco2.rideguard.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.data.EmergencyContactInfo
import kotlinx.coroutines.tasks.await

/**
 * DEPRECATED: Service for managing emergency contact relationships in Firestore
 * 
 * This service is deprecated and should be replaced with EmergencyContactServiceAdapter
 * Emergency contacts are now stored as nested relations in user profiles instead of a separate collection.
 * 
 * Migration path:
 * 1. Replace EmergencyContactService with EmergencyContactServiceAdapter in dependency injection
 * 2. Update UI components to use the new adapter
 * 3. Remove this file once all references are updated
 */
@Deprecated("Use EmergencyContactServiceAdapter instead. Emergency contacts are now nested in user profiles.")
class EmergencyContactService {
    
    private val db: FirebaseFirestore = Firebase.firestore
    private val emergencyContactsCollection = db.collection("emergency_contacts")
    private val usersCollection = db.collection("users")
    private val userProfileService = UserProfileService()
    
    companion object {
        const val MAX_EMERGENCY_CONTACTS = 5
    }
    

    
    /**
     * Get all emergency contacts for a user
     */
    suspend fun getEmergencyContacts(ownerUid: String): Result<List<EmergencyContactInfo>> {
        return try {
            Log.d("EmergencyContactService", "Querying emergency contacts for ownerUid: $ownerUid")
            
            val emergencyContactsQuery = emergencyContactsCollection
                .whereEqualTo("ownerUid", ownerUid)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            Log.d("EmergencyContactService", "Found ${emergencyContactsQuery.size()} emergency contact documents")
            
            val contactInfoList = mutableListOf<EmergencyContactInfo>()
            
            for (doc in emergencyContactsQuery.documents) {
                val contactUid = doc.getString("contactUid") ?: continue
                val addedAt = doc.getLong("addedAt") ?: 0L
                
                // Get full user profile for this contact
                val userProfileResult = userProfileService.getUserProfile(contactUid)
                if (userProfileResult.isSuccess) {
                    val userProfile = userProfileResult.getOrNull()
                    if (userProfile != null) {
                        contactInfoList.add(
                            EmergencyContactInfo(
                                contactId = doc.id,
                                username = userProfile.username,
                                uid = userProfile.uid,
                                email = userProfile.email,
                                phoneNumber = userProfile.phoneNumber,
                                addedAt = addedAt
                            )
                        )
                    }
                }
            }
            
            // Sort by addedAt in memory since we removed orderBy to avoid index requirement
            val sortedContacts = contactInfoList.sortedBy { it.addedAt }
            
            Log.d("EmergencyContactService", "Returning ${sortedContacts.size} emergency contacts: $sortedContacts")
            Result.success(sortedContacts)
            
        } catch (e: Exception) {
            Log.e("EmergencyContactService", "Error loading emergency contacts", e)
            Result.failure(e)
        }
    }
    

}