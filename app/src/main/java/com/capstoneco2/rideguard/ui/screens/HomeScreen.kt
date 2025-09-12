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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.components.BodyText
import androidx.compose.ui.graphics.Brush
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun HomeScreen(
    userName: String = "User"
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Welcome Header
            Column {
                BodyText(
                    text = "Welcome,",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Start
                )
                MainHeader(
                    text = "$userName!",
                    textAlign = TextAlign.Start
                )
            }
        }
        
        item {
            // Status Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Blackbox Status",
                    value = "Online",
                    valueColor = Color(0xFF06A759) // Green
                )
                StatusCard(
                    modifier = Modifier.weight(1f),
                    title = "Battery",
                    value = "100%",
                    valueColor = Color(0xFF06A759) // Green
                )
            }
        }
        
        item {
            // Blackbox Serial Number Card
            BlackboxSerialCard()
        }
        
        item {
            // Pulsa Balance
            PulsaBalanceSection()
        }
        
        item {
            // Emergency Contacts
            EmergencyContactsSection()
        }
        
        item {
            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            BodyText(
                text = title,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(4.dp))
            BodyText(
                text = value,
                color = valueColor,
                textAlign = TextAlign.Start,
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun BlackboxSerialCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                BodyText(
                    text = "Blackbox Serial Number",
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                MainHeader(
                    text = "Lorem Ipsum",
                    color = Color.White,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun PulsaBalanceSection() {
    Column {
        SectionHeader(
            text = "Pulsa Balance",
            textAlign = TextAlign.Start
        )
        Spacer(modifier = Modifier.height(8.dp))
        MainHeader(
            text = "Rp5000,00",
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Start
        )
    }
}

@Composable
private fun EmergencyContactsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionHeader(
                text = "Emergency Contacts",
                textAlign = TextAlign.Start
            )
            BodyText(
                text = "Add Another",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Emergency Contact Chips in Grid
        val contacts = listOf(
            "John Doe", "Lorem Ipsum", 
            "Jonathan Joestar", "John Doe"
        )
        
        // Split contacts into rows of 2
        val contactRows = contacts.chunked(2)
        
        contactRows.forEach { rowContacts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowContacts.forEach { contact ->
                    EmergencyContactChip(
                        name = contact,
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining space if odd number of contacts in row
                if (rowContacts.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EmergencyContactChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            BodyText(
                text = name,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyAppTheme {
        HomeScreen("John Doe")
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        HomeScreen("John Doe")
    }
}
