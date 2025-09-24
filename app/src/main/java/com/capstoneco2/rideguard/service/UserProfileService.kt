package com.capstoneco2.rideguard.service

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.capstoneco2.rideguard.data.UserProfile
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
                val userProfile = UserProfile(
                    uid = document.getString("uid") ?: "",
                    username = document.getString("username") ?: "",
                    email = document.getString("email") ?: "",
                    phoneNumber = document.getString("phoneNumber") ?: "",
                    profileImageUrl = document.getString("profileImageUrl"),
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
}