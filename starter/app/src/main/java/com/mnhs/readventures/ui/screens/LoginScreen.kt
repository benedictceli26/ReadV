package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.AuthRepository
import com.mnhs.readventures.ui.components.LogoMark
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Login / Register screen. Same design language as Home: blue hero panel
 * on top, white card with the form below it.
 */
@Composable
fun LoginScreen(
    authRepository: AuthRepository,
    onLoggedIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("student") } // "student" or "teacher"
    var isRegister by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    fun submit() {
        if (!email.contains("@") || email.isBlank()) {
            error = "Please enter a valid email address."
            return
        }
        if (password.length < 6) {
            error = "Password must be at least 6 characters."
            return
        }
        if (isRegister && name.isBlank()) {
            error = "Please enter your name."
            return
        }
        busy = true
        error = null
        val callback: (Result<com.mnhs.readventures.data.AppUser>) -> Unit = { result ->
            busy = false
            result.onSuccess { user ->
                AppState.resetForLogin(user, !authRepository.isFirebaseAvailable)
                onLoggedIn()
            }.onFailure { e ->
                error = e.message ?: "Login failed. Please try again."
            }
        }
        if (isRegister) authRepository.register(name.trim(), email.trim(), password, role, callback)
        else authRepository.signIn(email.trim(), password, callback)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        // Blue hero panel, like the Home hero.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BluePrimary)
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoMark(size = 72)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ReadVentures", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                "Read. Understand. Explore.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "A smart reading app for Muntinlupa National High School students.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    if (isRegister) "Create an account" else "Welcome back!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    if (isRegister) "Sign up to start reading."
                    else "Log in to continue your reading journey.",
                    fontSize = 13.sp,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (isRegister) {
                    // Role Selector: Student vs Teacher
                    Text(
                        "I am joining as a:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ScreenBg)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (role == "student") BluePrimary else Color.Transparent)
                                .clickable { role = "student" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Student",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (role == "student") Color.White else Muted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (role == "teacher") BluePrimary else Color.Transparent)
                                .clickable { role = "teacher" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Teacher",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (role == "teacher") Color.White else Muted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = BluePrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = Muted
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = BluePrimary,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = BluePrimary
                    )
                )

                error?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, fontSize = 13.sp, color = com.mnhs.readventures.ui.theme.Red)
                }

                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(
                    text = if (busy) "Please wait..." else if (isRegister) "Create account" else "Log In",
                    onClick = ::submit,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(
                    onClick = { isRegister = !isRegister; error = null },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        if (isRegister) "Already have an account? Log in"
                        else "No account yet? Create one",
                        color = BluePrimary
                    )
                }

                OutlinedButton(
                    onClick = {
                        authRepository.signInAsDemo { user ->
                            AppState.resetForLogin(user, true)
                            onLoggedIn()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("Continue as demo student (Juan Dela Cruz)", color = BluePrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        authRepository.signInAsDemoTeacher { user ->
                            AppState.resetForLogin(user, true)
                            onLoggedIn()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(23.dp)
                ) {
                    Text("Continue as demo teacher (Ms. Santos)", color = BluePrimary, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (authRepository.isFirebaseAvailable) "Signed in with Firebase Auth."
                    else "Sample mode: Firebase is not configured yet, so login is stored on this device only.",
                    fontSize = 11.sp,
                    color = Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
