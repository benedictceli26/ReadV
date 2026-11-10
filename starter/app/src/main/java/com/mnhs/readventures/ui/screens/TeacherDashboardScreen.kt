package com.mnhs.readventures.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.SchoolClass
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueDark
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 3 & 4: Teacher Dashboard Screen.
 * Class management, class codes, student rosters, assigning readings,
 * and teacher-created reading materials & quizzes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCreateClass: () -> Unit,
    onCreateMaterial: () -> Unit,
    onEditMaterial: (String) -> Unit
) {
    val context = LocalContext.current
    val teacher = AppState.currentUser.value
    var assigningClass by remember { mutableStateOf<SchoolClass?>(null) }
    var selectedMaterialId by remember { mutableStateOf(AppState.materials.firstOrNull()?.id ?: "") }
    var dueDateText by remember { mutableStateOf("Friday, 5:00 PM") }
    var expandedDropdown by remember { mutableStateOf(false) }

    fun copyCode(code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ReadVentures Class Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Class code $code copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        if (showBack) {
            SubHeader(title = "Teacher Dashboard", onBack = onBack)
        } else {
            AppTopBar(
                title = "ReadVentures",
                subtitle = "Teacher Dashboard",
                onProfileClick = onProfileClick
            )
            Text(
                "Teacher Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Teacher Profile banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BluePrimary)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                teacher.name.ifBlank { "Ms. Santos" },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "English Department  •  Teacher Account",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                teacher.email.ifBlank { "teacher.santos@mnhs.edu.ph" },
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Quick Actions: Create Class / Upload Reading
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCreateClass,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Class", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onCreateMaterial,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueDark)
                    ) {
                        Icon(Icons.Filled.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Story", fontSize = 13.sp)
                    }
                }
            }

            // Section: My Classes
            item {
                Text(
                    "My Classes (${AppState.classes.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
            }

            items(AppState.classes, key = { it.classId }) { schoolClass ->
                val members = AppState.classMembers.filter { it.classId == schoolClass.classId }
                val classAssignments = AppState.classAssignments.filter { it.classId == schoolClass.classId }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    schoolClass.className,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Text(
                                    "${schoolClass.subject}  •  ${schoolClass.schoolYear}",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlueLight)
                                    .clickable { copyCode(schoolClass.classCode) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        schoolClass.classCode,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BluePrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Filled.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = BluePrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Student Roster
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Group,
                                contentDescription = null,
                                tint = Muted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Enrolled Students (${members.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark
                            )
                        }

                        if (members.isEmpty()) {
                            Text(
                                "No students have joined yet. Share code ${schoolClass.classCode}.",
                                fontSize = 12.sp,
                                color = Muted,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        } else {
                            for (member in members) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "• ${member.studentName}",
                                        fontSize = 13.sp,
                                        color = InkDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "Read: ${AppState.readCount()}  |  Avg: ${AppState.averageScorePercent()}%",
                                        fontSize = 11.sp,
                                        color = Green,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current Assignments
                        Text(
                            "Active Assignments (${classAssignments.size}):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = InkDark
                        )
                        for (assign in classAssignments) {
                            Text(
                                "• ${assign.title} (Due: ${assign.dueDate})",
                                fontSize = 12.sp,
                                color = Muted,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                assigningClass = schoolClass
                                selectedMaterialId = AppState.materials.firstOrNull()?.id ?: ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("+ Assign Reading to Class", color = BluePrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Section: Teacher-Created Content
            item {
                Text(
                    "Teacher-Made Materials & Quizzes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
            }

            val teacherMaterials = AppState.materials.filter { it.isTeacherMade }
            if (teacherMaterials.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Text(
                            "No teacher-made materials created yet. Tap \"Upload Story\" above to create one.",
                            fontSize = 13.sp,
                            color = Muted,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
                items(teacherMaterials, key = { it.id }) { material ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.MenuBook, contentDescription = null, tint = BluePrimary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    material.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InkDark
                                )
                                Text(
                                    "${material.topic}  •  ${material.level}  •  ${material.category}",
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                                val questionsCount = AppState.questions.count { it.materialId == material.id }
                                Text(
                                    "Quiz: $questionsCount questions",
                                    fontSize = 11.sp,
                                    color = Green,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { onEditMaterial(material.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = BluePrimary)
                            }
                            IconButton(onClick = { AppState.deleteMaterial(material.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // Assign Reading Dialog
    assigningClass?.let { targetClass ->
        AlertDialog(
            onDismissRequest = { assigningClass = null },
            title = {
                Text("Assign Reading to ${targetClass.className}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Select Material:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkDark)
                    Spacer(modifier = Modifier.height(6.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        val selectedTitle = AppState.materials.firstOrNull { it.id == selectedMaterialId }?.title ?: "Select"
                        OutlinedTextField(
                            value = selectedTitle,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            for (mat in AppState.materials) {
                                DropdownMenuItem(
                                    text = { Text(mat.title) },
                                    onClick = {
                                        selectedMaterialId = mat.id
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Due Date:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkDark)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = dueDateText,
                        onValueChange = { dueDateText = it },
                        placeholder = { Text("e.g. Next Friday, 5:00 PM") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mat = AppState.materials.firstOrNull { it.id == selectedMaterialId }
                        if (mat != null) {
                            AppState.assignReading(
                                classId = targetClass.classId,
                                materialId = mat.id,
                                title = mat.title,
                                dueDate = dueDateText,
                                level = mat.level,
                                assignedBy = teacher.name.ifBlank { "Ms. Santos" }
                            )
                            Toast.makeText(context, "Reading assigned to ${targetClass.className}!", Toast.LENGTH_SHORT).show()
                        }
                        assigningClass = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Assign")
                }
            },
            dismissButton = {
                TextButton(onClick = { assigningClass = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
