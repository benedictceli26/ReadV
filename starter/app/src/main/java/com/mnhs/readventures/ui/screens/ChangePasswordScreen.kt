package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AuthRepository
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Change Password: validates the current password, new-password strength
 * (8+ chars, a letter, a number) and a matching confirmation, with
 * show/hide toggles and a strength meter. In Firebase mode the real
 * account password is changed; in sample mode the on-device password is.
 */
@Composable
fun ChangePasswordScreen(
    authRepository: AuthRepository,
    onBack: () -> Unit
) {
    var current by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    val issues = AuthRepository.passwordIssues(newPassword)
    val strength = AuthRepository.passwordStrength(newPassword) // 0..4
    val strengthLabel = when (strength) {
        0, 1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        else -> "Strong"
    }

    fun submit() {
        when {
            current.isBlank() -> error = "Please enter your current password."
            newPassword.length < 8 -> error = "New password must be at least 8 characters."
            issues.isNotEmpty() -> error = "New password needs: ${issues.joinToString(", ")}."
            newPassword != confirm -> error = "New password and confirmation do not match."
            newPassword == current -> error = "New password must be different from the current one."
            else -> {
                error = null
                busy = true
                authRepository.changePassword(current, newPassword) { result ->
                    busy = false
                    result.onSuccess { success = true }
                        .onFailure { e -> error = e.message ?: "Could not change the password." }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Change Password", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            if (success) {
                Text("✓ Password changed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Green)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Your password was updated successfully. Use the new password the next time you log in.",
                    fontSize = 14.sp,
                    color = InkDark
                )
            } else {
                PasswordField("Current password", current, { current = it }, showCurrent, { showCurrent = !showCurrent })
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField("New password", newPassword, { newPassword = it }, showNew, { showNew = !showNew })
                Spacer(modifier = Modifier.height(8.dp))
                if (newPassword.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { strength / 4f },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (strength >= 3) Green else if (strength == 2) Color(0xFFD97706) else Red,
                        trackColor = BlueLight
                    )
                    Text("Strength: $strengthLabel", fontSize = 12.sp, color = Muted)
                    Spacer(modifier = Modifier.height(8.dp))
                    for (rule in listOf("At least 8 characters" to (newPassword.length >= 8), "At least one letter" to newPassword.any { it.isLetter() }, "At least one number" to newPassword.any { it.isDigit() })) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (rule.second) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (rule.second) Green else Muted
                            )
                            Text(rule.first, fontSize = 13.sp, color = if (rule.second) Green else Muted)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                PasswordField("Confirm new password", confirm, { confirm = it }, showConfirm, { showConfirm = !showConfirm })
                error?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(it, fontSize = 13.sp, color = Red)
                }
            }
        }
        if (!success) {
            Button(
                onClick = ::submit,
                enabled = !busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(if (busy) "Please wait..." else "Change Password", fontSize = 16.sp)
            }
        } else {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Back to Profile", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisible) {
                Icon(
                    if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Hide password" else "Show password"
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
