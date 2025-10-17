package com.capstoneco2.rideguard.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.data.UserProfile
import com.capstoneco2.rideguard.data.EmergencyContactRelation
import com.capstoneco2.rideguard.data.toMap
import kotlinx.coroutines.tasks.await

/**
 * Service for managing user profile data in Firebase Firestore
 */
class UserProfileService {
    
    private val db: FirebaseFirestore = Firebase.firestore
    private val usersCollection = db.collection("users")
    
    /**
     * Save user profile to Firestore
     * Creates or updates the user document in the 'users' collection
     */
    suspend fun saveUserProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val updatedProfile = userProfile.copy(updatedAt = System.currentTimeMillis())
            usersCollection.document(userProfile.uid)
                .set(updatedProfile.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile from Firestore by UID
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> {
        return try {
            val document = usersCollection.document(uid).get().await()
            
            if (document.exists()) {
                // Parse emergency contacts from nested array
                val emergencyContactsData = document.get("emergencyContacts") as? List<Map<String, Any>> ?: emptyList()
                val emergencyContacts = emergencyContactsData.map { contactMap ->
                    EmergencyContactRelation(
                        contactUsername = contactMap["contactUsername"] as? String ?: "",
                        contactUid = contactMap["contactUid"] as? String ?: "",
                        addedAt = contactMap["addedAt"] as? Long ?: 0L,
                        isActive = contactMap["isActive"] as? Boolean ?: true
                    )
                }
                
                val userProfile = UserProfile(
                    uid = document.getString("uid") ?: "",
                    username = document.getString("username") ?: "",
                    email = document.getString("email") ?: "",
                    phoneNumber = document.getString("phoneNumber") ?: "",
                    profileImageUrl = document.getString("profileImageUrl"),
                    emergencyContacts = emergencyContacts,
                    createdAt = document.getLong("createdAt") ?: 0L,
                    updatedAt = document.getLong("updatedAt") ?: 0L
                )
                Result.success(userProfile)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update specific fields of user profile
     */
    suspend fun updateUserProfile(uid: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            val updatedFields = updates.toMutableMap()
            updatedFields["updatedAt"] = System.currentTimeMillis()
            
            usersCollection.document(uid)
                .update(updatedFields)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete user profile from Firestore
     */
    suspend fun deleteUserProfile(uid: String): Result<Unit> {
        return try {
            usersCollection.document(uid)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if username is available
     */
    suspend fun isUsernameAvailable(username: String, excludeUid: String? = null): Result<Boolean> {
        return try {
            var query = usersCollection.whereEqualTo("username", username)
            
            val documents = query.get().await()
            
            val isAvailable = if (excludeUid != null) {
                // When updating profile, exclude current user's document
                documents.documents.none { it.id != excludeUid }
            } else {
                documents.isEmpty
            }
            
            Result.success(isAvailable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add emergency contact to user profile
     */
    suspend fun addEmergencyContact(userUid: String, contactUsername: String, contactUid: String): Result<Unit> {
        return try {
            val userProfile = getUserProfile(userUid).getOrNull()
            
            if (userProfile != null) {
                // Check if contact already exists
                val existingContact = userProfile.emergencyContacts.find { 
                    it.contactUid == contactUid && it.isActive 
                }
                
                if (existingContact == null) {
                    val newContact = EmergencyContactRelation(
                        contactUsername = contactUsername,
                        contactUid = contactUid,
                        addedAt = System.currentTimeMillis(),
                        isActive = true
                    )
                    
                    val updatedContacts = userProfile.emergencyContacts + newContact
                    val updatedProfile = userProfile.copy(
                        emergencyContacts = updatedContacts,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    usersCollection.document(userUid)
                        .set(updatedProfile.toMap())
                        .await()
                    
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Emergency contact already exists"))
                }
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove emergency contact from user profile
     */
    suspend fun removeEmergencyContact(userUid: String, contactUid: String): Result<Unit> {
        return try {
            val userProfile = getUserProfile(userUid).getOrNull()
            
            if (userProfile != null) {
                val updatedContacts = userProfile.emergencyContacts.map { contact ->
                    if (contact.contactUid == contactUid) {
                        contact.copy(isActive = false)
                    } else {
                        contact
                    }
                }
                
                val updatedProfile = userProfile.copy(
                    emergencyContacts = updatedContacts,
                    updatedAt = System.currentTimeMillis()
                )
                
                usersCollection.document(userUid)
                    .set(updatedProfile.toMap())
                    .await()
                
                Result.success(Unit)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get active emergency contacts for a user
     */
    suspend fun getEmergencyContacts(userUid: String): Result<List<EmergencyContactRelation>> {
        return try {
            val userProfile = getUserProfile(userUid).getOrNull()
            
            if (userProfile != null) {
                val activeContacts = userProfile.emergencyContacts.filter { it.isActive }
                Result.success(activeContacts)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search users by username (basic implementation)
     * Note: This is a basic implementation. For production, consider using a search service like Algolia
     */
    suspend fun searchUsersByUsername(query: String, excludeUid: String): Result<List<UserProfile>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }
            
            // Get all users and filter client-side
            // This is not efficient for large user bases, but works for now
            val querySnapshot = usersCollection.get().await()
            
            val matchingUsers = querySnapshot.documents
                .mapNotNull { document ->
                    try {
                        // Parse emergency contacts from nested array
                        val emergencyContactsData = document.get("emergencyContacts") as? List<Map<String, Any>> ?: emptyList()
                        val emergencyContacts = emergencyContactsData.map { contactMap ->
                            EmergencyContactRelation(
                                contactUsername = contactMap["contactUsername"] as? String ?: "",
                                contactUid = contactMap["contactUid"] as? String ?: "",
                                addedAt = contactMap["addedAt"] as? Long ?: 0L,
                                isActive = contactMap["isActive"] as? Boolean ?: true
                            )
                        }
                        
                        UserProfile(
                            uid = document.getString("uid") ?: "",
                            username = document.getString("username") ?: "",
                            email = document.getString("email") ?: "",
                            phoneNumber = document.getString("phoneNumber") ?: "",
                            profileImageUrl = document.getString("profileImageUrl"),
                            emergencyContacts = emergencyContacts,
                            createdAt = document.getLong("createdAt") ?: 0L,
                            updatedAt = document.getLong("updatedAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                .filter { user ->
                    user.uid != excludeUid && // Exclude current user
                    user.username.contains(query, ignoreCase = true)
                }
                .take(10) // Limit results
                
            Result.success(matchingUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}