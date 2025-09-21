package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.SecondaryButton
import com.capstoneco2.rideguard.ui.theme.MyAppTheme
import kotlinx.coroutines.delay

// Enum for dialog states
enum class AddMemberDialogState {
    SEARCH_INPUT,
    PERSON_FOUND,
    PERSON_NOT_FOUND,
    MEMBER_ADDED
}

@Composable
fun BlackboxScreen(
    onNavigateToPulsaBalance: () -> Unit = {}
) {
    var isDeviceOnline by remember { mutableStateOf(true) }
    var deletionRate by remember { mutableStateOf("3 Hours") }
    var showDeletionDropdown by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                RideGuardDetailsSection(
                    onPulsaBalanceClick = onNavigateToPulsaBalance
                )
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
                EmergencyContactsSection(
                    onAddMoreUsers = { showAddMemberDialog = true }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
            }
        }
        
        // Add Member Dialog
        if (showAddMemberDialog) {
            AddFamilyMemberDialog(
                onDismiss = { showAddMemberDialog = false }
            )
        }
    }
}

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit
) {
    var dialogState by remember { mutableStateOf(AddMemberDialogState.SEARCH_INPUT) }
    var searchText by remember { mutableStateOf("") }
    var foundPersonName by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Dialog Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .clickable(enabled = false) { }, // Prevent click through
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                when (dialogState) {
                    AddMemberDialogState.SEARCH_INPUT -> {
                        SearchInputContent(
                            searchText = searchText,
                            onSearchTextChange = { searchText = it },
                            onSearch = {
                                // Mock search logic
                                if (searchText.lowercase().contains("rafi") || 
                                    searchText.lowercase().contains("john doe") ||
                                    searchText.lowercase().contains("john")) {
                                    foundPersonName = searchText
                                    dialogState = AddMemberDialogState.PERSON_FOUND
                                } else {
                                    dialogState = AddMemberDialogState.PERSON_NOT_FOUND
                                }
                            }
                        )
                    }
                    AddMemberDialogState.PERSON_FOUND -> {
                        PersonFoundContent(
                            personName = foundPersonName,
                            onCancel = { 
                                searchText = ""
                                dialogState = AddMemberDialogState.SEARCH_INPUT 
                            },
                            onConfirm = { 
                                dialogState = AddMemberDialogState.MEMBER_ADDED 
                            }
                        )
                    }
                    AddMemberDialogState.PERSON_NOT_FOUND -> {
                        PersonNotFoundContent(
                            onTryAgain = { 
                                searchText = ""
                                dialogState = AddMemberDialogState.SEARCH_INPUT 
                            }
                        )
                    }
                    AddMemberDialogState.MEMBER_ADDED -> {
                        MemberAddedContent(
                            personName = foundPersonName,
                            onConfirm = onDismiss
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInputContent(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Adding A Family Member",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Input Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BasicTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (searchText.isEmpty()) {
                            Text(
                                text = "Search Here",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        innerTextField()
                    }
                )
                
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSearch() }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Note: Your contact must have a RideGuard App as well.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Confirm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onSearch() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
    }
}

@Composable
private fun PersonFoundContent(
    personName: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Adding A Family Member",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Result Field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = personName,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Person Found!",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Note: Your contact must have a RideGuard App as well.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onCancel() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Confirm Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onConfirm() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PersonNotFoundContent(
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Adding A Family Member",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search Result Field (Empty)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Search Here",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_search),
                    contentDescription = "Search",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Person is not found.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Note: Your contact must have a RideGuard App as well.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Black.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Try Again Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onTryAgain() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Try Again",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
    }
}

@Composable
private fun MemberAddedContent(
    personName: String,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Family Member Added!",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "You have added $personName to Your Emergency Contact List",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Confirm Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(12.dp)
                )
                .clickable { onConfirm() }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Confirm",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
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
private fun RideGuardDetailsSection(
    onPulsaBalanceClick: () -> Unit = {}
) {
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
                modifier = Modifier.clickable { onPulsaBalanceClick() }
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
private fun EmergencyContactsSection(
    onAddMoreUsers: () -> Unit = {}
) {
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
            onClick = onAddMoreUsers,
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

@Preview(showBackground = true)
@Composable
fun AddFamilyMemberDialogPreview() {
    MyAppTheme {
        AddFamilyMemberDialog(onDismiss = {})
    }
}
