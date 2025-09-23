package com.capstoneco2.rideguard.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import kotlinx.coroutines.launch

// Data class for emergency contact
data class EmergencyContact(
    val name: String,
    val role: String
)

// Enum for dialog states
enum class AddMemberDialogState {
    SEARCH_INPUT,
    PERSON_FOUND,
    PERSON_NOT_FOUND,
    MEMBER_ADDED
}

// Enum for blackbox pairing states
enum class BlackboxPairingState {
    PAIRING_INPUT,
    DEVICE_FOUND,
    DEVICE_NOT_FOUND,
    PAIRING_SUCCESS
}

@Composable
fun BlackboxScreen(
    onNavigateToPulsaBalance: () -> Unit = {}
) {
    var isDeviceOnline by remember { mutableStateOf(false) } // Changed to false by default
    var deletionRate by remember { mutableStateOf("3 Hours") }
    var showDeletionDropdown by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showPairingDialog by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("No Device Connected") }
    var isVisible by remember { mutableStateOf(false) }
    
    // Emergency contacts state
    var emergencyContacts by remember { 
        mutableStateOf(
            listOf(
                EmergencyContact("John Doe", "Family Leader"),
                EmergencyContact("Jonathan Joestar", "Family Member"),
                EmergencyContact("Mike Shinoda", "Family Member")
            )
        ) 
    }
    
    // Trigger visibility animation on composition
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                animationSpec = tween(600, easing = androidx.compose.animation.core.EaseOutQuart),
                initialOffsetY = { it / 3 }
            ),
            exit = fadeOut(animationSpec = tween(400))
        ) {
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
                    deviceName = deviceName,
                    isOnline = isDeviceOnline,
                    onToggleStatus = { isDeviceOnline = !isDeviceOnline },
                    onConnectDevice = { showPairingDialog = true }
                )
            }
            
            item {
                // Change RideGuard Device Button
                SecondaryButton(
                    text = if (isDeviceOnline && deviceName != "No Device Connected") 
                           "Change RideGuard Device" 
                           else "Connect to RideGuard Device",
                    onClick = { showPairingDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BodyText(
                    text = if (isDeviceOnline && deviceName != "No Device Connected") 
                           "Note: You can only pair to one device" 
                           else "Note: Connect to your RideGuard device to get started",
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
                    emergencyContacts = emergencyContacts,
                    onAddMoreUsers = { showAddMemberDialog = true },
                    isDeviceConnected = isDeviceOnline
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(40.dp)) // Space for bottom nav
            }
        }
        }
        
        // Add Member Dialog with Animation
        AnimatedVisibility(
            visible = showAddMemberDialog,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOutBack)
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                animationSpec = tween(200, easing = androidx.compose.animation.core.EaseInBack)
            )
        ) {
            AddFamilyMemberDialog(
                onDismiss = { showAddMemberDialog = false },
                onMemberAdded = { memberName ->
                    emergencyContacts = emergencyContacts + EmergencyContact(memberName, "Family Member")
                }
            )
        }
        
        // Blackbox Pairing Dialog with Animation
        AnimatedVisibility(
            visible = showPairingDialog,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOutBack)
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                animationSpec = tween(200, easing = androidx.compose.animation.core.EaseInBack)
            )
        ) {
            BlackboxPairingDialog(
                onDismiss = { showPairingDialog = false },
                onDeviceConnected = { serialNumber ->
                    deviceName = "RideGuard $serialNumber"
                    isDeviceOnline = true
                }
            )
        }
    }
}

@Composable
fun AddFamilyMemberDialog(
    onDismiss: () -> Unit,
    onMemberAdded: (String) -> Unit
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
                            onConfirm = {
                                onMemberAdded(foundPersonName)
                                onDismiss()
                            }
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
fun BlackboxPairingDialog(
    onDismiss: () -> Unit,
    onDeviceConnected: (String) -> Unit
) {
    var dialogState by remember { mutableStateOf(BlackboxPairingState.PAIRING_INPUT) }
    var serialNumber by remember { mutableStateOf("") }
    var foundSerialNumber by remember { mutableStateOf("") }
    
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
                    .padding(horizontal = 16.dp, vertical = 32.dp)
                    .clickable(enabled = false) { }, // Prevent click through
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                when (dialogState) {
                    BlackboxPairingState.PAIRING_INPUT -> {
                        PairingInputContent(
                            serialNumber = serialNumber,
                            onSerialNumberChange = { serialNumber = it },
                            onConfirm = {
                                // Mock search logic - check if serial number exists
                                if (serialNumber.uppercase().contains("A2DW12DF") || 
                                    serialNumber.length >= 6) {
                                    foundSerialNumber = serialNumber.uppercase()
                                    dialogState = BlackboxPairingState.DEVICE_FOUND
                                } else {
                                    dialogState = BlackboxPairingState.DEVICE_NOT_FOUND
                                }
                            }
                        )
                    }
                    BlackboxPairingState.DEVICE_FOUND -> {
                        DeviceFoundContent(
                            serialNumber = foundSerialNumber,
                            onConfirm = { 
                                dialogState = BlackboxPairingState.PAIRING_SUCCESS
                            }
                        )
                    }
                    BlackboxPairingState.DEVICE_NOT_FOUND -> {
                        DeviceNotFoundContent(
                            onTryAgain = { 
                                serialNumber = ""
                                dialogState = BlackboxPairingState.PAIRING_INPUT 
                            }
                        )
                    }
                    BlackboxPairingState.PAIRING_SUCCESS -> {
                        PairingSuccessContent(
                            onConfirm = {
                                onDeviceConnected(foundSerialNumber)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PairingInputContent(
    serialNumber: String,
    onSerialNumberChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { it / 4 }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Pairing Ongoing",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Input the serial number of the\nRideGuard Box",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Serial Number Input Field
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
            BasicTextField(
                value = serialNumber,
                onValueChange = onSerialNumberChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color.Black,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (serialNumber.isEmpty()) {
                        Text(
                            text = "Input Serial Number Here",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Make sure you connected to the same network as the RideGuard Device",
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
private fun DeviceFoundContent(
    serialNumber: String,
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + scaleIn(
            animationSpec = tween(400, easing = androidx.compose.animation.core.EaseOutBack)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Pairing Ongoing",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Input the serial number of the\nRideGuard Box",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Serial Number Display Field
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
            Text(
                text = serialNumber,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Animated success text
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(500, delayMillis = 200)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Text(
                text = "RideGuard Box Found!",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Make sure you connected to the same network as the RideGuard Device",
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
private fun DeviceNotFoundContent(
    onTryAgain: () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pairing Ongoing",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Input the serial number of the\nRideGuard Box",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Empty Serial Number Field
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
            Text(
                text = "Input Serial Number Here",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "RideGuard Box not found.",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Make sure you connected to the same network as the RideGuard Device",
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
private fun PairingSuccessContent(
    onConfirm: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var showSuccessText by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
        kotlinx.coroutines.delay(300)
        showSuccessText = true
    }
    
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Animated success title
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(tween(600)) + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        ) {
            Text(
                text = "Pairing Successful!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Animated success message
        AnimatedVisibility(
            visible = showSuccessText,
            enter = fadeIn(tween(600)) + slideInVertically(
                animationSpec = tween(600),
                initialOffsetY = { it / 4 }
            )
        ) {
            Text(
                text = "Congratulations! Now you are registered as the last user of this Rideguard",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
        
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
    onToggleStatus: () -> Unit,
    onConnectDevice: () -> Unit = {}
) {
    // Animate card scale based on connection status
    val scale by animateFloatAsState(
        targetValue = if (isOnline) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardScale"
    )
    
    // Animate card color transition
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isOnline) 1f else 0.8f,
        animationSpec = tween(300),
        label = "cardAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleStatus() }
            .padding(2.dp), // Extra padding for scale animation
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isOnline) 8.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device Icon with pulse animation
            val iconScale by animateFloatAsState(
                targetValue = if (isOnline) 1.1f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "iconScale"
            )
            
            // Custom device icon - no background rectangle
            Icon(
                painter = painterResource(id = R.drawable.box),
                contentDescription = "RideGuard Device", 
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300)),
                    exit = fadeOut(tween(300))
                ) {
                    Column {
                        Text(
                            text = if (deviceName == "No Device Connected") "No Device Connected" else deviceName,
                            color = Color.White,
                            fontWeight = if (deviceName == "No Device Connected") FontWeight.Bold else FontWeight.Normal,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (deviceName == "No Device Connected") FontWeight.Bold else FontWeight.Normal
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        BodyText(
                            text = "Status: ${if (isOnline) "Online" else "Not connected"}",
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RideGuardDetailsSection(
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
                text = "Check Here",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onPulsaBalanceClick() }
            )
        }
    }
}

@Composable
fun StorageSettingsSection(
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
fun EmergencyContactsSection(
    emergencyContacts: List<EmergencyContact>,
    onAddMoreUsers: () -> Unit = {},
    isDeviceConnected: Boolean = false
) {
    var isVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(200) // Small delay for staggered appearance
        isVisible = true
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(500)) + slideInVertically(
            animationSpec = tween(500),
            initialOffsetY = { it / 6 }
        )
    ) {
        Column {
        SectionHeader(
            text = "Emergency Contacts",
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact Items - Dynamic list
        emergencyContacts.forEach { contact ->
            EmergencyContactItem(
                name = contact.name,
                role = contact.role
            )
            
            if (contact != emergencyContacts.last()) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Add More Users Button
        PrimaryButton(
            text = "Add More Users",
            onClick = onAddMoreUsers,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Sync Contact Data Button
        SecondaryButton(
            text = if (isSyncing) "Syncing..." else "Sync Contact Data",
            onClick = {
                if (!isSyncing && isDeviceConnected) {
                    isSyncing = true
                    syncResult = null
                    // Simulate sync operation
                    coroutineScope.launch {
                        delay(2000) // Simulate network delay
                        // Random success/failure for demo
                        syncResult = if (kotlin.random.Random.nextBoolean()) {
                            "Refresh Success"
                        } else {
                            "Sync failed - Connection timeout"
                        }
                        isSyncing = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isDeviceConnected && !isSyncing
        )
        
        // Sync result message
        syncResult?.let { result ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result,
                style = MaterialTheme.typography.bodySmall,
                color = if (result.startsWith("Refresh Success")) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
}

@Composable
fun EmergencyContactItem(
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
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
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
        AddFamilyMemberDialog(
            onDismiss = {},
            onMemberAdded = {}
        )
    }
}
