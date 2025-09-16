package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun BlackboxScreen() {
    var isDeviceOnline by remember { mutableStateOf(true) }
    var deletionRate by remember { mutableStateOf("3 Hours") }
    var showDeletionDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp)) // Increased spacing from top
            
            // Header
            MainHeader(
                text = "RideGuard",
                textAlign = TextAlign.Start,
                color = com.capstoneco2.rideguard.ui.theme.Blue80
            )
            
            BodyText(
                text = "This will set-up your rideguard device.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        item {
            // BlackBox Device Card
            BlackBoxDeviceCard(
                deviceName = "BlackBox A",
                isOnline = isDeviceOnline,
                onToggleStatus = { isDeviceOnline = !isDeviceOnline }
            )
        }
        
        item {
            // Change RideGuard Device Button
            SecondaryButton(
                text = "Change RideGuard Device",
                onClick = { /* Handle device change */ },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BodyText(
                text = "Note: You can only pair to one device",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        item {
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            )
        }
        
        item {
            // RideGuard Details
            RideGuardDetailsSection()
        }
        
        item {
            // Storage Settings
            StorageSettingsSection(
                deletionRate = deletionRate,
                onDeletionRateChange = { deletionRate = it }
            )
        }
        
        item {
            // Emergency Contacts
            EmergencyContactsSection()
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun BlackBoxDeviceCard(
    deviceName: String,
    isOnline: Boolean,
    onToggleStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                BodyText(
                    text = "📱",
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                BodyText(
                    text = deviceName,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                BodyText(
                    text = "Status: ${if (isOnline) "Online" else "Offline"}",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
private fun RideGuardDetailsSection() {
    Column {
        SectionHeader(
            text = "RideGuard Details",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Last Check - Aligned to right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyText(
                text = "Last Check at:",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f, false)
            )
            Text(
                text = "DD/MM/YYYY",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Details Row - Battery Level and Pulsa Balance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyText(
                text = "Battery Level",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f, false)
            )
            Text(
                text = "100%",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BodyText(
                text = "Pulsa Balance",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f, false)
            )
            Text(
                text = "Rp5000,00",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { /* Handle pulsa balance click */ }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Refresh Data Button
        PrimaryButton(
            text = "Refresh Data",
            onClick = { /* Handle refresh */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StorageSettingsSection(
    deletionRate: String,
    onDeletionRateChange: (String) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    
    Column {
        SectionHeader(
            text = "Storage Settings",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Deletion Rate",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Box {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { showDropdown = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$deletionRate ▼",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                        color = Color.White
                    )
                }
                
                if (showDropdown) {
                    // Dropdown implementation would go here
                    // For now, we'll just cycle through options
                    onDeletionRateChange(
                        when (deletionRate) {
                            "3 Hours" -> "2 Hours"
                            "2 Hours" -> "1 Hours"
                            "1 Hours" -> "3 Hours"
                            else -> "3 Hours"
                        }
                    )
                    showDropdown = false
                }
            }
        }
    }
}

@Composable
private fun EmergencyContactsSection() {
    Column {
        SectionHeader(
            text = "Emergency Contacts",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact Items
        EmergencyContactItem(
            name = "John Doe",
            role = "Family Leader"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        EmergencyContactItem(
            name = "Jonathan Joestar",
            role = "Family Member"
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        EmergencyContactItem(
            name = "Mike Shinoda",
            role = "Family Member"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add More Users Button
        PrimaryButton(
            text = "Add More Users",
            onClick = { /* Handle add users */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EmergencyContactItem(
    name: String,
    role: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyText(
            text = name,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f, false)
        )
        
        Text(
            text = role,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BlackboxScreenPreview() {
    MyAppTheme {
        BlackboxScreen()
    }
}
