package com.capstoneco2.rideguard.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

enum class AccidentDialogState {
    ACCIDENT_DETECTED,
    LOCATION_VIEW,
    EMERGENCY_SERVICES,
    EMERGENCY_CALL_CONFIRMATION,
    HELP_ON_THE_WAY
}

enum class UserRole {
    CRASH_VICTIM,
    EMERGENCY_CONTACT
}

@Composable
fun TrafficAccidentDialog(
    isVisible: Boolean,
    onClose: () -> Unit,
    latitude: Double = -7.7956,
    longitude: Double = 110.3695,
    userRole: UserRole = UserRole.CRASH_VICTIM,
    crashVictimName: String = "Lorem Ipsum",
    onEmergencyServicesCalled: () -> Unit = {},
    onHelpConfirmed: () -> Unit = {}
) {
    val dialogState = remember { mutableStateOf(AccidentDialogState.ACCIDENT_DETECTED) }

    if (isVisible) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(300)) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(300, easing = EaseOutBack)
                        ),
                exit = fadeOut(animationSpec = tween(200)) +
                        scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(200, easing = EaseInBack)
                        )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp
                    )
                ) {
                    when (dialogState.value) {
                        AccidentDialogState.ACCIDENT_DETECTED -> {
                            AccidentDetectedContent(
                                onCheckLocation = { 
                                    dialogState.value = AccidentDialogState.LOCATION_VIEW 
                                },
                                onCallEmergency = { 
                                    dialogState.value = AccidentDialogState.EMERGENCY_SERVICES 
                                },
                                onClose = onClose,
                                latitude = latitude,
                                longitude = longitude,
                                userRole = userRole,
                                crashVictimName = crashVictimName
                            )
                        }
                        AccidentDialogState.LOCATION_VIEW -> {
                            LocationViewContent(
                                onBack = { 
                                    dialogState.value = AccidentDialogState.ACCIDENT_DETECTED 
                                },
                                onClose = onClose,
                                latitude = latitude,
                                longitude = longitude,
                                userRole = userRole,
                                crashVictimName = crashVictimName
                            )
                        }
                        AccidentDialogState.EMERGENCY_SERVICES -> {
                            EmergencyServicesContent(
                                onCheckLocation = { 
                                    dialogState.value = AccidentDialogState.LOCATION_VIEW 
                                },
                                onClose = onClose,
                                onEmergencyServicesCalled = {
                                    dialogState.value = AccidentDialogState.EMERGENCY_CALL_CONFIRMATION
                                    onEmergencyServicesCalled()
                                },
                                userRole = userRole,
                                crashVictimName = crashVictimName
                            )
                        }
                        AccidentDialogState.EMERGENCY_CALL_CONFIRMATION -> {
                            EmergencyCallConfirmationContent(
                                onConfirmHelp = {
                                    dialogState.value = AccidentDialogState.HELP_ON_THE_WAY
                                    onHelpConfirmed()
                                },
                                onClose = onClose,
                                userRole = userRole
                            )
                        }
                        AccidentDialogState.HELP_ON_THE_WAY -> {
                            HelpOnTheWayContent(
                                onClose = onClose,
                                userRole = userRole,
                                crashVictimName = crashVictimName
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccidentDetectedContent(
    onCheckLocation: () -> Unit,
    onCallEmergency: () -> Unit,
    onClose: () -> Unit,
    latitude: Double = -7.7956,
    longitude: Double = 110.3695,
    userRole: UserRole = UserRole.CRASH_VICTIM,
    crashVictimName: String = "Lorem Ipsum"
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Traffic Accident Detected!"
                UserRole.EMERGENCY_CONTACT -> "Emergency Alert!"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Car crash icon - using Warning as placeholder
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Traffic Accident",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Are you okay?"
                UserRole.EMERGENCY_CONTACT -> "Contact: $crashVictimName"
            },
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "A traffic accident has been detected. If you're conscious and able to respond, please check your condition and call for help if needed."
                UserRole.EMERGENCY_CONTACT -> "$crashVictimName has been involved in a traffic accident. You can view their location and call emergency services on their behalf."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Check Location Button - Filled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onCheckLocation() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (userRole) {
                        UserRole.CRASH_VICTIM -> "Check Location"
                        UserRole.EMERGENCY_CONTACT -> "View ${crashVictimName}'s Location"
                    },
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Call Emergency Services Button - Outlined
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onCallEmergency() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (userRole) {
                        UserRole.CRASH_VICTIM -> "Call Emergency Services"
                        UserRole.EMERGENCY_CONTACT -> "Call Emergency Services"
                    },
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Close Button - Text only
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LocationViewContent(
    onBack: () -> Unit,
    onClose: () -> Unit,
    latitude: Double = -7.7956,
    longitude: Double = 110.3695,
    userRole: UserRole = UserRole.CRASH_VICTIM,
    crashVictimName: String = "Lorem Ipsum"
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Your Location"
                UserRole.EMERGENCY_CONTACT -> "${crashVictimName}'s Location"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Native Compose Location Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Location marker icon
                Text(
                    text = "📍",
                    fontSize = 48.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Crash Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Lat: ${String.format("%.6f", latitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Lng: ${String.format("%.6f", longitude)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Tap 'Open in Google Maps' for navigation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Open in Google Maps Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        val gmmIntentUri = android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Accident+Location)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Fallback to web maps
                            val webIntent = Intent(Intent.ACTION_VIEW, 
                                android.net.Uri.parse("https://maps.google.com/?q=$latitude,$longitude"))
                            context.startActivity(webIntent)
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open in Google Maps",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Back Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onBack() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmergencyServicesContent(
    onCheckLocation: () -> Unit,
    onClose: () -> Unit,
    onEmergencyServicesCalled: () -> Unit = {},
    userRole: UserRole = UserRole.CRASH_VICTIM,
    crashVictimName: String = "Lorem Ipsum"
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Emergency Services"
                UserRole.EMERGENCY_CONTACT -> "Emergency Services"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🚨",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "In case of emergency, please contact:"
                UserRole.EMERGENCY_CONTACT -> "You can call emergency services on behalf of ${crashVictimName}:"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🚔 Police: 110",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "🚑 Ambulance: 118/119",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = "🚒 Fire Department: 113",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Call Ambulance Button (Primary for medical emergencies)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:119")
                        }
                        context.startActivity(intent)
                        onEmergencyServicesCalled()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Call Ambulance (119)",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Call Police Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:110")
                        }
                        context.startActivity(intent)
                        onEmergencyServicesCalled()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Call Police (110)",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Call Fire Department Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:113")
                        }
                        context.startActivity(intent)
                        onEmergencyServicesCalled()
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Call Fire Dept (113)",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
            
            // Check Location Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onCheckLocation() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Check Location",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Close Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClose() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Composable
private fun EmergencyCallConfirmationContent(
    onConfirmHelp: () -> Unit,
    onClose: () -> Unit,
    userRole: UserRole = UserRole.CRASH_VICTIM
) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Emergency Call Made"
                UserRole.EMERGENCY_CONTACT -> "Emergency Services Contacted"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "📞",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Did you successfully contact emergency services?"
                UserRole.EMERGENCY_CONTACT -> "Have you successfully contacted emergency services?"
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Please confirm if help is on the way so we can notify your emergency contacts."
                UserRole.EMERGENCY_CONTACT -> "Please confirm if help has been dispatched to the accident location."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Yes, Help is Coming Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onConfirmHelp() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Yes, Help is On The Way",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // No, Try Again Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onClose() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No, Try Again",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun HelpOnTheWayContent(
    onClose: () -> Unit,
    userRole: UserRole = UserRole.CRASH_VICTIM,
    crashVictimName: String = "Lorem Ipsum"
) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Help is On The Way!"
                UserRole.EMERGENCY_CONTACT -> "${crashVictimName} is in Good Hands!"
            },
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "✅",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Emergency services have been contacted and are on their way to your location."
                UserRole.EMERGENCY_CONTACT -> "Emergency services have been contacted and are responding to ${crashVictimName}'s location."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Black
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = when (userRole) {
                UserRole.CRASH_VICTIM -> "Your emergency contacts have been notified. Stay safe and wait for help to arrive."
                UserRole.EMERGENCY_CONTACT -> "You will be notified of any updates. Thank you for helping ${crashVictimName}."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Close Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onClose() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Close",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
