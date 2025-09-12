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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun SettingsScreen(
    userName: String = "John Doe",
    userEmail: String = "john.doe@example.com"
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoTrackingEnabled by remember { mutableStateOf(false) }
    var darkModeEnabled by remember { mutableStateOf(false) }
    var locationSharingEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header
            MainHeader(
                text = "Settings",
                textAlign = TextAlign.Start
            )
            
            BodyText(
                text = "Manage your preferences and account settings",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        item {
            // User Profile Card
            UserProfileCard(
                userName = userName,
                userEmail = userEmail,
                onEditClick = { /* Handle edit profile */ }
            )
        }
        
        item {
            // Notifications Section - Simplified
            SectionHeader(text = "Notifications")
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BodyText(
                    text = "Push Notifications",
                    color = MaterialTheme.colorScheme.onBackground
                )
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BodyText(
                        text = "Notification Repeat Interval",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    BodyText(
                        text = "60 Seconds",
                        color = Color.White
                    )
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun UserProfileCard(
    userName: String,
    userEmail: String,
    onEditClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Label
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            BodyText(
                text = "Profile",
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Avatar - Circular
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.Gray.copy(alpha = 0.3f),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder for profile image
            BodyText(
                text = userName.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // User Name
        SectionHeader(
            text = userName,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Date Joined
        BodyText(
            text = "Date Joined",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Edit Button
        PrimaryButton(
            text = "Edit",
            onClick = onEditClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val modifier = if (onClick != null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }
    
    Row(
        modifier = modifier.padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BodyText(text = icon)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            BodyText(
                text = title,
                color = MaterialTheme.colorScheme.onBackground
            )
            BodyText(
                text = subtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        trailingContent?.let {
            Spacer(modifier = Modifier.width(8.dp))
            it()
        } ?: onClick?.let {
            BodyText(
                text = "›",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MyAppTheme {
        SettingsScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        SettingsScreen()
    }
}
