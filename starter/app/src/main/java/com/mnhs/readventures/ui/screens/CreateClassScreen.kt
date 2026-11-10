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
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 3: Create Class Screen (for Teachers).
 */
@Composable
fun CreateClassScreen(
    onBack: () -> Unit,
    onClassCreated: () -> Unit
) {
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("English 7") }
    var schoolYear by remember { mutableStateOf("2026-2027") }
    var error by remember { mutableStateOf<String?>(null) }

    val teacher = AppState.currentUser.value

    fun handleCreate() {
        if (className.isBlank()) {
            error = "Please enter a class / section name."
            return
        }

        AppState.createClass(
            name = className.trim(),
            subject = subject.trim(),
            schoolYear = schoolYear.trim(),
            teacherId = teacher.uid.ifBlank { "teacher-santos" },
            teacherName = teacher.name.ifBlank { "Ms. Santos" }
        )

        onClassCreated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        SubHeader(title = "Create New Class", onBack = onBack)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Class Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Text(
                    "Enter the grade & section to generate a unique 6-character class code for your students.",
                    fontSize = 13.sp,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    label = { Text("Class / Section Name") },
                    placeholder = { Text("e.g. Grade 7 – Bonifacio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    placeholder = { Text("e.g. English 7") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = schoolYear,
                    onValueChange = { schoolYear = it },
                    label = { Text("School Year") },
                    placeholder = { Text("e.g. 2026-2027") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(it, fontSize = 13.sp, color = Red)
                }

                Spacer(modifier = Modifier.height(20.dp))

                PrimaryButton(
                    "Create class & generate code",
                    onClick = ::handleCreate,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
