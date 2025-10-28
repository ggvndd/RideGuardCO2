package com.capstoneco2.rideguard.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColorScheme = lightColorScheme(
    primary = Blue40,
    onPrimary = White,
    secondary = Blue20,
    onSecondary = Black80,
    error = Red,
    onError = White,
    background = White,
    onBackground = Black80
)

val DarkColorScheme = darkColorScheme(
    primary = Blue40,
    onPrimary = White,
    secondary = Blue80,
    onSecondary = White,
    background = Black40,
    error = Red,
    onError = White,
    onBackground = White
)

@Composable
fun MyAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}