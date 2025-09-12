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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun BlackboxScreen() {
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
                text = "CO₂ Blackbox",
                textAlign = TextAlign.Start
            )
            
            BodyText(
                text = "Track and analyze your carbon footprint in real-time",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        item {
            // Current Trip Tracking
            CurrentTripCard()
        }
        
        item {
            // CO2 Metrics
            CO2MetricsCard()
        }
        
        item {
            // Transportation Mode Selector
            SectionHeader(text = "Transportation Mode")
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TransportModeCard(
                    modifier = Modifier.weight(1f),
                    mode = "Car",
                    icon = "🚗",
                    isSelected = true,
                    onClick = { /* Handle car selection */ }
                )
                TransportModeCard(
                    modifier = Modifier.weight(1f),
                    mode = "Bus",
                    icon = "🚌",
                    isSelected = false,
                    onClick = { /* Handle bus selection */ }
                )
                TransportModeCard(
                    modifier = Modifier.weight(1f),
                    mode = "Train",
                    icon = "🚆",
                    isSelected = false,
                    onClick = { /* Handle train selection */ }
                )
                TransportModeCard(
                    modifier = Modifier.weight(1f),
                    mode = "Walk",
                    icon = "🚶",
                    isSelected = false,
                    onClick = { /* Handle walk selection */ }
                )
            }
        }
        
        item {
            // Historical Data
            SectionHeader(text = "Recent Trips")
            Spacer(modifier = Modifier.height(8.dp))
            
            TripHistoryCard(
                destination = "Office",
                distance = "8.5 km",
                co2 = "2.1 kg CO₂",
                time = "15 min",
                timestamp = "Today, 8:30 AM"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TripHistoryCard(
                destination = "Shopping Mall",
                distance = "12.3 km",
                co2 = "3.2 kg CO₂",
                time = "22 min",
                timestamp = "Yesterday, 2:15 PM"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TripHistoryCard(
                destination = "Airport",
                distance = "25.7 km",
                co2 = "6.8 kg CO₂",
                time = "45 min",
                timestamp = "2 days ago, 6:00 AM"
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

@Composable
private fun CurrentTripCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    BodyText(
                        text = "Current Trip",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    SectionHeader(
                        text = "In Progress",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(25.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    BodyText(text = "🚗")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TripStatItem("Distance", "3.2 km")
                TripStatItem("Time", "8 min")
                TripStatItem("CO₂", "0.8 kg")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryButton(
                    text = "Pause",
                    onClick = { /* Handle pause */ },
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    text = "End Trip",
                    onClick = { /* Handle end trip */ },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CO2MetricsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            SectionHeader(text = "CO₂ Analytics")
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricItem(
                    label = "Today",
                    value = "2.1 kg",
                    icon = "📊",
                    color = MaterialTheme.colorScheme.primary
                )
                MetricItem(
                    label = "This Week",
                    value = "15.8 kg",
                    icon = "📈",
                    color = MaterialTheme.colorScheme.secondary
                )
                MetricItem(
                    label = "This Month",
                    value = "42.3 kg",
                    icon = "🌍",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(4.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            BodyText(
                text = "60% of monthly goal achieved",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TripStatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SectionHeader(
            text = value,
            textAlign = TextAlign.Center
        )
        BodyText(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    icon: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BodyText(text = icon)
        Spacer(modifier = Modifier.height(4.dp))
        SectionHeader(
            text = value,
            textAlign = TextAlign.Center,
            color = color
        )
        BodyText(
            text = label,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TransportModeCard(
    mode: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BodyText(text = icon)
            Spacer(modifier = Modifier.height(4.dp))
            BodyText(
                text = mode,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TripHistoryCard(
    destination: String,
    distance: String,
    co2: String,
    time: String,
    timestamp: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
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
                BodyText(text = "📍")
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                BodyText(
                    text = destination,
                    color = MaterialTheme.colorScheme.onBackground
                )
                BodyText(
                    text = "$distance • $time • $co2",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                BodyText(
                    text = timestamp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlackboxScreenPreview() {
    MyAppTheme {
        BlackboxScreen()
    }
}
