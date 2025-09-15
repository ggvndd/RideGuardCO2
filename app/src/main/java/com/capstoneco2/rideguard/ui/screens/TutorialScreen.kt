package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.TutorialItemCard
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun TutorialScreen(
    onTutorialClick: (String) -> Unit = { }
) {
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
                text = "Tutorials",
                textAlign = TextAlign.Start
            )
            
            BodyText(
                text = "Learn how to use RideGuard optimally by this curated guides",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // Tutorial Items
        items(getTutorialItems()) { tutorial ->
            TutorialItemCard(
                title = tutorial.title,
                icon = tutorial.icon,
                onClick = { onTutorialClick(tutorial.id) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for bottom nav
        }
    }
}

data class SimpleTutorialItem(
    val id: String,
    val title: String,
    val icon: String
)

fun getTutorialItems(): List<SimpleTutorialItem> {
    return listOf(
        SimpleTutorialItem("getting_started", "Getting Started", "🚀"),
        SimpleTutorialItem("track_trip", "Track Your Trip", "🛣️"),
        SimpleTutorialItem("view_statistics", "View Statistics", "📊"),
        SimpleTutorialItem("blackbox_features", "Blackbox Features", "📦"),
        SimpleTutorialItem("settings_guide", "Settings Guide", "⚙️"),
        SimpleTutorialItem("faq", "FAQ", "❓")
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
