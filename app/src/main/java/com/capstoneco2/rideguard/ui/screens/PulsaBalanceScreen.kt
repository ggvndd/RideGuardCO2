package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.theme.Blue80
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun PulsaBalanceScreen(
    onBackClick: () -> Unit = {}
) {
    var showTopUpSuccessDialog by remember { mutableStateOf(false) }
    var currentBalance by remember { mutableStateOf("Rp5000,00") }
    var phoneNumber by remember { mutableStateOf("081245869242") }
    var expiryDate by remember { mutableStateOf("20/05/2025") }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Back Button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Blue80,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Header
                MainHeader(
                    text = "Pulsa Balance",
                    textAlign = TextAlign.Start,
                    color = Blue80
                )
                
                BodyText(
                    text = "This is your balance for your pulsa at your BlackBox.",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            item {
                // Balance Card
                PulsaBalanceCard(
                    phoneNumber = phoneNumber,
                    balance = currentBalance,
                    expiryDate = expiryDate
                )
            }
            
            item {
                // Check Pulsa Amount Button
                PrimaryButton(
                    text = "Check Pulsa Amount",
                    onClick = { 
                        // TODO: Add logic to check pulsa amount
                        // This could trigger an API call or show current balance
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            item {
                // Instructions Section
                HowToFillSection()
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp)) // Reduced space for bottom nav
            }
        }
        
        // Top-up Success Dialog
        if (showTopUpSuccessDialog) {
            TopUpSuccessDialog(
                newBalance = "Rp100.000",
                onDismiss = { showTopUpSuccessDialog = false }
            )
        }
    }
}

@Composable
private fun PulsaBalanceCard(
    phoneNumber: String,
    balance: String,
    expiryDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Phone Number
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HowToFillSection() {
    Column {
        Text(
            text = "How to Fill Your Pulsa Balance",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Step 1
        InstructionStep(
            stepNumber = "1.",
            instruction = "Find a counter or a place to buy a pulsa, maybe by mobile banking or minimarkets."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Step 2
        InstructionStep(
            stepNumber = "2.",
            instruction = "For mobile banking, check the top-up menu, and choose your card provider."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Step 3
        InstructionStep(
            stepNumber = "3.",
            instruction = "For minimarket, choose the top-up option for pulsa and choose your card provider."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Step 4
        InstructionStep(
            stepNumber = "4.",
            instruction = "Input the card number above and the desired amount of top-up."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Step 5
        InstructionStep(
            stepNumber = "5.",
            instruction = "We will refresh periodically if your pulsa balance has been increased due to the top-up!"
        )
    }
}

@Composable
private fun InstructionStep(
    stepNumber: String,
    instruction: String
) {
    BodyText(
        text = "$stepNumber $instruction",
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun TopUpSuccessDialog(
    newBalance: String,
    onDismiss: () -> Unit
) {
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
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Congratulations for Topping Up!",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Your pulsa balance is now $newBalance",
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
                            .clickable { onDismiss() }
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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PulsaBalanceScreenPreview() {
    MyAppTheme {
        PulsaBalanceScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun TopUpSuccessDialogPreview() {
    MyAppTheme {
        TopUpSuccessDialog(
            newBalance = "Rp100.000",
            onDismiss = {}
        )
    }
}