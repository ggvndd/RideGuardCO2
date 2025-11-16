package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.components.TutorialItemCard
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun TutorialScreen() {
    var selectedTutorial by remember { mutableStateOf<TutorialItem?>(null) }
    
    if (selectedTutorial != null) {
        TutorialDetailDialog(
            tutorial = selectedTutorial!!,
            onDismiss = { selectedTutorial = null }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(48.dp)) // Increased spacing from top more
            
            // Header
            MainHeader(
                text = "Tutorial",
                textAlign = TextAlign.Start,
                color = com.capstoneco2.rideguard.ui.theme.Blue80
            )
            
            BodyText(
                text = "Learn how to use RideGuard optimally with\nthese step-by-step guides",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Tutorial Items
        items(getTutorialItems()) { tutorial ->
            TutorialItemCard(
                title = tutorial.title,
                onClick = { selectedTutorial = tutorial }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

data class TutorialItem(
    val title: String,
    val content: List<TutorialStep>
)

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val tip: String? = null
)

fun getTutorialItems(): List<TutorialItem> {
    return listOf(
        // Tutorial 1: Emergency Contacts
        TutorialItem(
            title = "Managing Emergency Contacts",
            content = listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Navigate to Emergency Contacts",
                    description = "Go to the Blackbox tab in the bottom navigation. Scroll down to find the Emergency Contacts section.",
                    tip = "You can also access emergency contacts from the Home screen."
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Add Emergency Contact",
                    description = "Tap the 'Add More Users' button. Enter the contact's username in the dialog that appears and tap 'Add Contact'.",
                    tip = "Make sure the username exists in the system before adding."
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Verify Contact Added",
                    description = "The new contact should appear in your emergency contacts list. You can add up to 5 emergency contacts maximum.",
                    tip = "Emergency contacts will be notified automatically in case of an emergency."
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Delete Emergency Contact",
                    description = "To remove a contact, find them in the list and tap the red delete button (trash icon) next to their name.",
                    tip = "Always keep at least one emergency contact for safety."
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Confirm Deletion",
                    description = "A confirmation dialog will appear. Tap 'Delete' to confirm removal or 'Cancel' to keep the contact.",
                    tip = "Deleted contacts can be re-added anytime using their username."
                )
            )
        ),
        
        // Tutorial 2: Device Connection
        TutorialItem(
            title = "Connecting to RideGuard Device",
            content = listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Enable Required Permissions",
                    description = "Make sure your phone has Wi-Fi and Location permissions enabled for RideGuard app. You can check this in your device settings.",
                    tip = "These permissions are needed to detect nearby RideGuard devices."
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Access Device Pairing",
                    description = "Go to the Blackbox tab and tap 'Connect to RideGuard Device' button. If you see 'Change RideGuard Device', your device is already connected.",
                    tip = "You can only pair to one device at a time."
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Grant Permissions",
                    description = "If prompted, tap 'Grant Permissions' to allow the app to access Wi-Fi and location services for device detection.",
                    tip = "Without these permissions, the app cannot detect RideGuard devices."
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Scan for Networks",
                    description = "Tap 'Scan for Wi-Fi Networks' to search for available networks. The app will show all nearby Wi-Fi networks.",
                    tip = "Make sure your RideGuard device is powered on and broadcasting its Wi-Fi signal."
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Select Your Device",
                    description = "Choose any Wi-Fi network that represents your RideGuard device from the list. The app will treat any selected network as a RideGuard device.",
                    tip = "Look for networks with names like 'RideGuard_Device' or similar."
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Confirm Connection",
                    description = "Tap 'Confirm' when prompted to complete the pairing process. You should see 'Pairing Successful!' message.",
                    tip = "Once connected, the device status will show as 'Online' on the Home screen."
                )
            )
        ),
        
        // Tutorial 3: Authentication
        TutorialItem(
            title = "Login & Account Registration",
            content = listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Access Authentication Screen",
                    description = "When you first open RideGuard, you'll see the login screen. If you're already logged in, you can access this by logging out from Profile tab.",
                    tip = "The authentication screen is your gateway to all RideGuard features."
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Create New Account (Sign Up)",
                    description = "If you don't have an account, tap 'Sign Up' at the bottom. Enter your email address, create a strong password, and optionally add your display name.",
                    tip = "Use a strong password with a mix of letters, numbers, and special characters."
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Complete Registration",
                    description = "Tap 'Sign Up' button to create your account. You'll be automatically logged in after successful registration.",
                    tip = "Make sure to remember your email and password for future logins."
                ),
                TutorialStep(
                    stepNumber = 4,
                    title = "Login to Existing Account",
                    description = "If you already have an account, enter your registered email and password in the respective fields on the login screen.",
                    tip = "Double-check your email and password if you encounter login issues."
                ),
                TutorialStep(
                    stepNumber = 5,
                    title = "Access Your Account",
                    description = "Tap 'Login' to access your RideGuard account. You'll be taken to the Home screen where you can start using all features.",
                    tip = "Your login session will be remembered, so you won't need to login every time."
                ),
                TutorialStep(
                    stepNumber = 6,
                    title = "Switching Between Login/Signup",
                    description = "You can switch between Login and Sign Up modes by tapping the respective buttons at the bottom of the authentication screen.",
                    tip = "All your data is securely stored and synced across devices when logged in."
                )
            )
        )
    )
}

@Composable
fun TutorialDetailDialog(
    tutorial: TutorialItem,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                item {
                    Column {
                        Text(
                            text = tutorial.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = MaterialTheme.typography.headlineSmall.lineHeight * 1.1f
                            ),
                            color = com.capstoneco2.rideguard.ui.theme.Blue80,
                            maxLines = 2
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${tutorial.content.size} steps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Tutorial Steps
                items(tutorial.content.size) { index ->
                    val step = tutorial.content[index]
                    TutorialStepCard(step = step)
                }
                
                // Close Button
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryButton(
                        text = "Close Tutorial",
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TutorialStepCard(step: TutorialStep) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Step number and title
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = step.stepNumber.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.padding(8.dp))
                
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
            
            // Tip (if available)
            step.tip?.let { tip ->
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_dialog_info),
                            contentDescription = "Tip",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.padding(4.dp))
                        
                        Column {
                            Text(
                                text = "💡 Tip:",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialScreenPreview() {
    MyAppTheme {
        TutorialScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TutorialScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        TutorialScreen()
    }
}
