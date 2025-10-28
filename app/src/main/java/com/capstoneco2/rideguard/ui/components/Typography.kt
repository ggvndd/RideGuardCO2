package com.capstoneco2.rideguard.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Main header component using headlineLarge typography
 */
@Composable
fun MainHeader(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Section header component using titleLarge typography
 */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Subheader component using bodyMedium typography (bold)
 */
@Composable
fun SubHeader(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Body text component using bodyLarge typography
 */
@Composable
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Caption text component - smaller text for hints and descriptions
 */
@Composable
fun CaptionText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.875f // Slightly smaller
        ),
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Error text component for displaying error messages
 */
@Composable
fun ErrorText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.error,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Success text component for displaying success messages
 */
@Composable
fun SuccessText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = Color(0xFF06A759) // Using your Green color
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        textAlign = textAlign,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Centered header for screens like login, welcome, etc.
 */
@Composable
fun CenteredHeader(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    MainHeader(
        text = text,
        textAlign = TextAlign.Center,
        color = color,
        modifier = modifier.padding(horizontal = 16.dp)
    )
}
