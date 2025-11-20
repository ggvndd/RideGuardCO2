package com.capstoneco2.rideguard.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.capstoneco2.rideguard.viewmodel.AuthViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capstoneco2.rideguard.R
import com.capstoneco2.rideguard.ui.components.BodyText
import com.capstoneco2.rideguard.ui.components.ErrorText
import com.capstoneco2.rideguard.ui.components.MainHeader
import com.capstoneco2.rideguard.ui.components.PrimaryButton
import com.capstoneco2.rideguard.ui.theme.MyAppTheme

@Composable
fun SignUpPage(
    onSignUpSuccess: () -> Unit,
    onSignInClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    fcmToken: String? = null
) {
    var username by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    val performSignUp = {
        when {
            username.isEmpty() -> errorMessage = "Username is required"
            phoneNumber.isEmpty() -> errorMessage = "Phone number is required"  
            email.isEmpty() -> errorMessage = "Email is required"
            password.isEmpty() -> errorMessage = "Password is required"
            confirmPassword.isEmpty() -> errorMessage = "Please confirm your password"
            password != confirmPassword -> errorMessage = "Passwords do not match"
            password.length < 6 -> errorMessage = "Password must be at least 6 characters"
            !email.contains("@") -> errorMessage = "Please enter a valid email"
            else -> {
                errorMessage = ""
                authViewModel.signUp(email, password, username, phoneNumber, fcmToken)
            }
        }
    }
    
    // Navigate to main app when sign up is successful
    LaunchedEffect(authState.isSignedIn) {
        if (authState.isSignedIn) {
            onSignUpSuccess()
        }
    }
    
    // Show Firebase errors
    LaunchedEffect(authState.error) {
        if (authState.error != null) {
            errorMessage = authState.error!!
            authViewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Image(
            painter = painterResource(id = R.drawable.rideguardlogo),
            contentDescription = "RideGuard Logo",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(80.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Header - Larger and more prominent like in mockup
        MainHeader(
            text = "Sign Up",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        BodyText(
            text = "Sign up now to use Rideguard!",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Username Field
        IconTextField(
            value = username,
            onValueChange = { 
                username = it
                errorMessage = ""
            },
            label = "Username",
            placeholder = "Enter your username",
            icon = Icons.Default.Person,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone Number Field
        IconTextField(
            value = phoneNumber,
            onValueChange = { 
                phoneNumber = it
                errorMessage = ""
            },
            label = "Phone Number",
            placeholder = "Enter your phone number",
            icon = Icons.Default.Phone,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next,
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email Field
        IconTextField(
            value = email,
            onValueChange = { 
                email = it
                errorMessage = ""
            },
            label = "Email",
            placeholder = "Enter your email",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Field
        IconPasswordTextField(
            value = password,
            onValueChange = { 
                password = it
                errorMessage = ""
            },
            label = "Password",
            placeholder = "Enter your password",
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
            imeAction = ImeAction.Next,
            onKeyboardAction = { focusManager.moveFocus(FocusDirection.Down) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Confirm Password Field
        IconPasswordTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                errorMessage = ""
            },
            label = "Confirm Password",
            placeholder = "Confirm your password",
            passwordVisible = confirmPasswordVisible,
            onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
            imeAction = ImeAction.Done,
            onKeyboardAction = { 
                focusManager.clearFocus()
                performSignUp()
            }
        )
        
        // Error Message
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ErrorText(text = errorMessage)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Loading indicator
        if (authState.isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        // Sign Up Button
        PrimaryButton(
            text = "Sign Up",
            onClick = performSignUp,
            modifier = Modifier.fillMaxWidth(),
            enabled = !authState.isLoading
        )
                // Sign In Link - Like in mockup with blue text and interactive states
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            TextButton(
                onClick = onSignInClick,
                interactionSource = interactionSource
            ) {
                Text(
                    text = "Already have an account? Sign In Now",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isPressed) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun IconTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onKeyboardAction: () -> Unit = {}
) {
    Column {
        BodyText(
            text = label,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onKeyboardAction() },
                onDone = { onKeyboardAction() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun IconPasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    onKeyboardAction: () -> Unit = {}
) {
    Column {
        BodyText(
            text = label,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityChange) {
                    Text(
                        text = if (passwordVisible) "🙈" else "👁️",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onKeyboardAction() },
                onDone = { onKeyboardAction() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpPagePreview() {
    MyAppTheme {
        SignUpPage(
            onSignUpSuccess = { },
            onSignInClick = { }
        )
    }
}
