// Debug code to check Firebase Auth status
// Add this to your EmergencyContactServiceAdapter or UserProfileService

private fun debugAuthStatus() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser != null) {
        Log.d("DEBUG_AUTH", "User is authenticated: ${currentUser.uid}")
        Log.d("DEBUG_AUTH", "Email: ${currentUser.email}")
    } else {
        Log.e("DEBUG_AUTH", "User is NOT authenticated!")
    }
}

// Call this before making Firestore queries:
// debugAuthStatus()