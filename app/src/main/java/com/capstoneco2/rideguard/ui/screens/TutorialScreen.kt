package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.capstoneco2.rideguard.ui.components.SectionHeader
import com.capstoneco2.rideguard.ui.components.TutorialItemCard
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun TutorialScreen() {
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
                text = "Tutorial",
                textAlign = TextAlign.Start
            )
            
            BodyText(
                text = "Learn how to use RideGuard CO2 effectively",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Tutorial Items
        items(getTutorialItems()) { tutorial ->
            TutorialItemCard(
                title = tutorial.title,
                icon = tutorial.icon,
                onClick = { /* Handle tutorial click */ }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

data class SimpleTutorialItem(
    val title: String,
    val icon: String
)

fun getTutorialItems(): List<SimpleTutorialItem> {
    return listOf(
        SimpleTutorialItem("Getting Started", "🚀"),
        SimpleTutorialItem("Track Your Trip", "🛣️"),
        SimpleTutorialItem("View Statistics", "📊"),
        SimpleTutorialItem("Blackbox Features", "📦"),
        SimpleTutorialItem("Settings Guide", "⚙️"),
        SimpleTutorialItem("FAQ", "❓")
    )
}

@Preview(showBackground = true)
@Composable
fun TutorialScreenPreview() {
    MyAppTheme {
        TutorialScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TutorialScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        TutorialScreen()
    }
}
