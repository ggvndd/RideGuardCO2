package com.capstoneco2.rideguard.ui.components

import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri

// Enum for accident dialog states
enum class AccidentDialogState {
    ACCIDENT_DETECTED,
    LOCATION_VIEW,
    EMERGENCY_SERVICES
}

@Composable
fun TrafficAccidentDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onCheckLocation: () -> Unit = {},
    onCallEmergencyServices: () -> Unit = {},
    onClose: () -> Unit,
    latitude: Double = -7.7956, // Default to Yogyakarta coordinates for testing
    longitude: Double = 110.3695
) {
    var dialogState by remember { mutableStateOf(AccidentDialogState.ACCIDENT_DETECTED) }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + scaleIn(
            animationSpec = tween(300, easing = EaseOutBack)
        ),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(
            animationSpec = tween(200, easing = EaseInBack)
        )
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable(enabled = false) { },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    when (dialogState) {
                        AccidentDialogState.ACCIDENT_DETECTED -> {
                            AccidentDetectedContent(
                                onCheckLocation = {
                                    onCheckLocation()
                                    dialogState = AccidentDialogState.LOCATION_VIEW
                                },
                                onCallEmergency = {
                                    onCallEmergencyServices()
                                    dialogState = AccidentDialogState.EMERGENCY_SERVICES
                                },
                                onClose = onClose
                            )
                        }
                        AccidentDialogState.LOCATION_VIEW -> {
                            LocationViewContent(
                                onClose = onClose,
                                latitude = latitude,
                                longitude = longitude
                            )
                        }
                        AccidentDialogState.EMERGENCY_SERVICES -> {
                            EmergencyServicesContent(
                                onCheckLocation = { 
                                    dialogState = AccidentDialogState.LOCATION_VIEW 
                                },
                                onClose = onClose
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
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Traffic Accident\nDetected!",
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
            text = "User: Lorem Ipsum",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "The location is approximately at Jl. Grafika No.2, Senolowo, Sinduadi, Kec. Mlati, Kabupaten Sleman, Daerah Istimewa Yogyakarta 55281",
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
                    text = "Check Location",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
            
            // Call Emergency Services Button - Outlined
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onCallEmergency() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Call Emergency Services",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Close Button - Filled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onClose() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun LocationViewContent(
    onClose: () -> Unit,
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Accident\nLocation",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Location coordinates display
        Text(
            text = "Coordinates: $latitude, $longitude",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Embedded Google Maps using WebView
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    Color.Gray.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        
                        // For now, load a simple map view (you'll need to add your Google Maps API key)
                        val simpleMapUrl = "https://maps.google.com/maps?q=$latitude,$longitude&t=&z=15&ie=UTF8&iwloc=&output=embed"
                        loadUrl(simpleMapUrl)
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Open in Google Maps App Button - Filled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { 
                        // Open Google Maps app with the coordinates
                        try {
                            val gmmIntentUri =
                                "geo:$latitude,$longitude?q=$latitude,$longitude(Accident Location)".toUri()
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            
                            // If Google Maps app is not available, open in browser
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                // Fallback to browser
                                val browserIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    "https://maps.google.com/?q=$latitude,$longitude".toUri()
                                )
                                context.startActivity(browserIntent)
                            }
                        } catch (e: Exception) {
                            // Handle error - could show a toast or log
                            e.printStackTrace()
                        }
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Open in Google Maps",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
            
            // Call Emergency Services Button - Outlined
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { /* Call emergency */ }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Call Emergency Services",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            // Close Button - Filled
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.error,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onClose() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EmergencyServicesContent(
    onCheckLocation: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier.padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emergency Service\nRedirected to Phone",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
                    text = "Check Location",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
            
            // Close Button - Outlined
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onClose() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Close",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}