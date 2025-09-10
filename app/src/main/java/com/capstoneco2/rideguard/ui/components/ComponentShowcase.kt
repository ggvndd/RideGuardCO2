package com.capstoneco2.rideguard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

/**
 * Example screen showing how to use all the components together
 */
@Composable
fun ComponentShowcaseScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Typography Examples
        CenteredHeader(text = "Welcome to RideGuard")
        
        Spacer(modifier = Modifier.height(8.dp))
        
        SectionHeader(text = "Sign In")
        
        BodyText(text = "Please enter your credentials to continue")
        
        CaptionText(text = "We'll never share your information with anyone else.")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Text Field Examples
        EmailTextField(
            value = email,
            onValueChange = { 
                email = it
                isEmailError = false
            },
            isError = isEmailError,
            errorMessage = if (isEmailError) "Please enter a valid email" else ""
        )
        
        PasswordTextField(
            value = password,
            onValueChange = { password = it }
        )
        
        SearchTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = "Search destinations..."
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Button Examples
        PrimaryButton(
            text = "Sign In",
            onClick = {
                if (email.isEmpty() || !email.contains("@")) {
                    isEmailError = true
                } else {
                    // Handle sign in
                }
            }
        )
        
        SecondaryButton(
            text = "Create Account",
            onClick = { /* Handle create account */ }
        )
        
        SmallPrimaryButton(
            text = "Forgot Password?",
            onClick = { /* Handle forgot password */ }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status text examples
        if (isEmailError) {
            ErrorText(text = "Please check your email format")
        } else {
            SuccessText(text = "All fields look good!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComponentShowcaseScreenPreview() {
    MyAppTheme {
        ComponentShowcaseScreen()
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ComponentShowcaseScreenDarkPreview() {
    MyAppTheme(darkTheme = true) {
        ComponentShowcaseScreen()
    }
}
