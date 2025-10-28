package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun TutorialDetailScreen(
    tutorialId: String,
    onBackClick: () -> Unit
) {
    val tutorial = getTutorialDetail(tutorialId)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        item {
            // Title
            MainHeader(
                text = tutorial.title,
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            // Hero Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.motorcycle_welcome_image),
                    contentDescription = "Tutorial Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        item {
            // Date and Author Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    BodyText(
                        text = tutorial.date,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Author
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Author",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    BodyText(
                        text = tutorial.author,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        item {
            // Content
            BodyText(
                text = tutorial.content,
                modifier = Modifier.padding(horizontal = 20.dp),
                textAlign = TextAlign.Justify
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            // Second Image (if available)
            if (tutorial.hasSecondImage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.motorcycle_welcome_image),
                        contentDescription = "Tutorial Image 2",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
        
        item {
            // Subtitle and additional content
            if (tutorial.subtitle.isNotEmpty()) {
                SectionHeader(
                    text = tutorial.subtitle,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                BodyText(
                    text = tutorial.additionalContent,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    textAlign = TextAlign.Justify
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

data class TutorialDetail(
    val id: String,
    val title: String,
    val date: String,
    val author: String,
    val content: String,
    val subtitle: String = "",
    val additionalContent: String = "",
    val hasSecondImage: Boolean = false
)

fun getTutorialDetail(id: String): TutorialDetail {
    return when (id) {
        "getting_started" -> TutorialDetail(
            id = "getting_started",
            title = "Getting Started with RideGuard",
            date = "Saturday, 17 July 2024",
            author = "RideGuard Team",
            content = "Lorem ipsum dolor sit amet consectetur. Id idetane faucibus adipiscing suspendisse. Ipsum interdum...\n\nMauris amet orci amet ultrices ornare eget nibh id. Rhoncus eget tempor massa donec. Diam ut tellus urna a malesuada convallis eget. In vitae ac risus sit eget lobortis metus. Enim ut aliquet proin quisque tellus faucibus placerat nunc. Arcu id semper urna et maecenas et vitae. Sit proin pharetra phasellus aliquet ac adipiscing. Sed consectetur orci elit quisque nulla odio accumsan blandit. Faucibus luctus eu eleifend in.",
            subtitle = "Subtitle Goes Here",
            additionalContent = "Mauris amet orci amet ultrices ornare eget nibh id. Rhoncus eget tempor massa donec. Diam ut tellus urna a malesuada convallis eget. In vitae ac risus sit eget lobortis metus. Enim ut aliquet proin quisque tellus faucibus placerat nunc. Arcu id semper urna et maecenas et vitae. Sit proin pharetra phasellus aliquet ac adipiscing. Sed consectetur orci elit quisque nulla odio accumsan blandit. Faucibus luctus eu eleifend in.",
            hasSecondImage = true
        )
        "track_trip" -> TutorialDetail(
            id = "track_trip",
            title = "How to Track Your Trip",
            date = "Saturday, 15 July 2024",
            author = "RideGuard Team",
            content = "Learn how to effectively track your motorcycle trips using RideGuard's advanced tracking system. Our CO2 monitoring technology helps you understand your environmental impact while ensuring your safety on the road.",
            hasSecondImage = false
        )
        "view_statistics" -> TutorialDetail(
            id = "view_statistics",
            title = "Understanding Your Statistics",
            date = "Friday, 14 July 2024",
            author = "Data Team",
            content = "Dive deep into your riding statistics and learn how to interpret the data to improve your riding habits and reduce your carbon footprint.",
            hasSecondImage = false
        )
        "blackbox_features" -> TutorialDetail(
            id = "blackbox_features",
            title = "Blackbox Device Features",
            date = "Thursday, 13 July 2024",
            author = "Hardware Team",
            content = "Explore all the features of your RideGuard blackbox device, from emergency detection to data logging and real-time monitoring.",
            hasSecondImage = true
        )
        "settings_guide" -> TutorialDetail(
            id = "settings_guide",
            title = "Settings and Customization",
            date = "Wednesday, 12 July 2024",
            author = "Support Team",
            content = "Customize your RideGuard experience by learning about all available settings and how to optimize them for your riding style.",
            hasSecondImage = false
        )
        "faq" -> TutorialDetail(
            id = "faq",
            title = "Frequently Asked Questions",
            date = "Tuesday, 11 July 2024",
            author = "Support Team",
            content = "Find answers to the most common questions about RideGuard, troubleshooting tips, and helpful information for new users.",
            hasSecondImage = false
        )
        else -> TutorialDetail(
            id = "default",
            title = "Tutorial Not Found",
            date = "Unknown",
            author = "Unknown",
            content = "Sorry, this tutorial could not be found.",
            hasSecondImage = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialDetailScreenPreview() {
    MyAppTheme {
        TutorialDetailScreen(
            tutorialId = "getting_started",
            onBackClick = { }
        )
    }
}