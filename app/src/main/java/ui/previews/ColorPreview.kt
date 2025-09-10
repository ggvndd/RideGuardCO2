import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.theme.Black80
import com.capstoneco2.rideguard.ui.theme.Black40
import com.capstoneco2.rideguard.ui.theme.Blue20
import com.capstoneco2.rideguard.ui.theme.Blue40
import com.capstoneco2.rideguard.ui.theme.Blue80
import com.capstoneco2.rideguard.ui.theme.Gray40
import com.capstoneco2.rideguard.ui.theme.Green
import com.capstoneco2.rideguard.ui.theme.Red
import com.capstoneco2.rideguard.ui.theme.White
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun ColorPreview() {
    MyAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorSwatch(name = "Black80", color = Black80)
            ColorSwatch(name = "Black40", color = Black40)
            ColorSwatch(name = "Gray40", color = Gray40)
            ColorSwatch(name = "Blue20", color = Blue20)
            ColorSwatch(name = "Blue40", color = Blue40)
            ColorSwatch(name = "Blue80", color = Blue80)
            ColorSwatch(name = "White", color = White)
            ColorSwatch(name = "Green", color = Green)
            ColorSwatch(name = "Red", color = Red)
        }
    }
}

@Composable
fun ColorSwatch(name: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun LightColorPreview() {
    MyAppTheme(darkTheme = false) {
        ColorPreview()
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DarkColorPreview() {
    MyAppTheme(darkTheme = true) {
        ColorPreview()
    }
}