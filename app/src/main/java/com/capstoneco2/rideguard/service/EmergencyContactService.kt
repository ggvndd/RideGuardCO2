package com.capstoneco2.rideguard.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.data.EmergencyContact
import com.capstoneco2.rideguard.data.EmergencyContactInfo
import com.capstoneco2.rideguard.data.UserProfile
import com.capstoneco2.rideguard.data.toMap
import com.capstoneco2.rideguard.service.UserProfileService
import kotlinx.coroutines.tasks.await

/**
 * Service for managing emergency contact relationships in Firestore
 */
class EmergencyContactService {
    
    private val db: FirebaseFirestore = Firebase.firestore
    private val emergencyContactsCollection = db.collection("emergency_contacts")
    private val usersCollection = db.collection("users")
    private val userProfileService = UserProfileService()
    
    companion object {
        const val MAX_EMERGENCY_CONTACTS = 5
    }
    
    /**
     * Add an emergency contact by username
     */
    suspend fun addEmergencyContact(ownerUid: String, contactUsername: String): Result<String> {
        return try {
            // First, check if owner already has 5 contacts
            val currentContacts = getEmergencyContacts(ownerUid).getOrNull() ?: emptyList()
            if (currentContacts.size >= MAX_EMERGENCY_CONTACTS) {
                return Result.failure(Exception("Maximum of $MAX_EMERGENCY_CONTACTS emergency contacts allowed"))
            }
            
            // Find the user by username
            val contactUserQuery = usersCollection
                .whereEqualTo("username", contactUsername)
                .get()
                .await()
            
            if (contactUserQuery.isEmpty) {
                return Result.failure(Exception("User with username '$contactUsername' not found"))
            }
            
            val contactUserDoc = contactUserQuery.documents.first()
            val contactUid = contactUserDoc.getString("uid") ?: ""
            
            // Check if user is trying to add themselves
            if (ownerUid == contactUid) {
                return Result.failure(Exception("Cannot add yourself as emergency contact"))
            }
            
            // Check if contact already exists
            val existingContactQuery = emergencyContactsCollection
                .whereEqualTo("ownerUid", ownerUid)
                .whereEqualTo("contactUid", contactUid)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            if (!existingContactQuery.isEmpty) {
                return Result.failure(Exception("$contactUsername is already your emergency contact"))
            }
            
            // Create new emergency contact
            val emergencyContact = EmergencyContact(
                ownerUid = ownerUid,
                contactUsername = contactUsername,
                contactUid = contactUid
            )
            
            val docRef = emergencyContactsCollection.add(emergencyContact.toMap()).await()
            Result.success(docRef.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all emergency contacts for a user
     */
    suspend fun getEmergencyContacts(ownerUid: String): Result<List<EmergencyContactInfo>> {
        return try {
            val emergencyContactsQuery = emergencyContactsCollection
                .whereEqualTo("ownerUid", ownerUid)
                .whereEqualTo("isActive", true)
                .orderBy("addedAt", Query.Direction.ASCENDING)
                .get()
                .await()
            
            val contactInfoList = mutableListOf<EmergencyContactInfo>()
            
            for (doc in emergencyContactsQuery.documents) {
                val contactUid = doc.getString("contactUid") ?: continue
                val contactUsername = doc.getString("contactUsername") ?: continue
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
            
            Result.success(contactInfoList)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove an emergency contact
     */
    suspend fun removeEmergencyContact(contactId: String): Result<Unit> {
        return try {
            emergencyContactsCollection.document(contactId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search users by username (for adding contacts)
     */
    suspend fun searchUsersByUsername(query: String, currentUserUid: String): Result<List<UserProfile>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }
            
            // Search for users whose username starts with the query
            val usersQuery = usersCollection
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + '\uf8ff')
                .limit(10)
                .get()
                .await()
            
            val userProfiles = mutableListOf<UserProfile>()
            
            for (doc in usersQuery.documents) {
                val uid = doc.getString("uid") ?: continue
                
                // Exclude current user from search results
                if (uid == currentUserUid) continue
                
                val userProfile = UserProfile(
                    uid = uid,
                    username = doc.getString("username") ?: "",
                    email = doc.getString("email") ?: "",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    createdAt = doc.getLong("createdAt") ?: 0L,
                    updatedAt = doc.getLong("updatedAt") ?: 0L
                )
                userProfiles.add(userProfile)
            }
            
            Result.success(userProfiles)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get count of emergency contacts for a user
     */
    suspend fun getEmergencyContactCount(ownerUid: String): Result<Int> {
        return try {
            val query = emergencyContactsCollection
                .whereEqualTo("ownerUid", ownerUid)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            Result.success(query.size())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}