package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ProgressStore
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.ScreenBg

/** Edit Profile: name, grade/section and email. Saved on-device. */
@Composable
fun EditProfileScreen(
    progressStore: ProgressStore,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val user = AppState.currentUser.value
    var name by remember { mutableStateOf(user.name) }
    var grade by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(user.email) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Edit Profile", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Profile information", fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = InkDark)
                    Text("Update the account details available in your profile.", fontSize = 13.sp, color = Muted)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Full name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = grade, onValueChange = { grade = it },
                        label = { Text("Grade & Section") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    message?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it, fontSize = 13.sp, color = Green)
                    }
                }
            }
        }
        Button(
            onClick = {
                if (name.isBlank() || email.isBlank()) {
                    message = "Name and email cannot be empty."
                    return@Button
                }
                AppState.currentUser.value = user.copy(name = name.trim(), email = email.trim())
                progressStore.saveDisplayName(name.trim())
                message = "Profile updated."
                onSaved()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Save Changes", fontSize = 16.sp)
        }
    }
}
