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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 3: Join Class Screen.
 * Student enters a 6-character class code to join their teacher's class.
 */
@Composable
fun JoinClassScreen(
    onBack: () -> Unit,
    onClassJoined: () -> Unit
) {
    var classCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    fun submitJoin() {
        errorMessage = null
        successMessage = null
        val code = classCode.trim().uppercase()
        if (code.isBlank()) {
            errorMessage = "Please enter a class code."
            return
        }

        val student = AppState.currentUser.value
        val result = AppState.joinClass(
            code = code,
            studentId = student.uid.ifBlank { "sample-juan" },
            studentName = student.name.ifBlank { "Juan Dela Cruz" }
        )

        result.onSuccess { joined ->
            successMessage = "Successfully joined ${joined.className}!"
            onClassJoined()
        }.onFailure { e ->
            errorMessage = e.message ?: "Invalid class code. Please check with your teacher."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        SubHeader(title = "Join a Class", onBack = onBack)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Enter Class Code",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Ask your teacher for your 6-character class code (e.g. READ-7A1).",
                    fontSize = 13.sp,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = classCode,
                    onValueChange = { classCode = it.uppercase() },
                    label = { Text("Class Code") },
                    placeholder = { Text("READ-7A1") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(it, fontSize = 13.sp, color = Red)
                }

                successMessage?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(it, fontSize = 13.sp, color = Green)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = ::submitJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Join Class", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Demo Class Code:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                        Text(
                            "READ-7A1 (Grade 7 – Rizal, Ms. Santos)",
                            fontSize = 13.sp,
                            color = InkDark
                        )
                    }
                }
            }
        }
    }
}
