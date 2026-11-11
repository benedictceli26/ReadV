package com.mnhs.readventures.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.ReadingRepository
import com.mnhs.readventures.data.SchoolClass
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.SectionHeader
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.OrangeLight
import com.mnhs.readventures.ui.theme.Orange
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Teacher Dashboard — mirrors the Student Home design language:
 * compact top nav, greeting header, stats row, dark featured card, list sections.
 *
 * FEATURE 3 & 4: Class management, class codes, student rosters, assignments,
 * and teacher-created reading materials & quizzes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    showBack: Boolean = true,
    onBack: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    readingRepository: ReadingRepository,
    onCreateClass: () -> Unit,
    onCreateMaterial: () -> Unit,
    onEditMaterial: (String) -> Unit
) {
    val context = LocalContext.current
    val teacher = AppState.currentUser.value
    var assigningClass by remember { mutableStateOf<SchoolClass?>(null) }
    var pendingDelete by remember { mutableStateOf<ReadingMaterial?>(null) }
    var deletingMaterial by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

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
        }
        AppState.contentLoadError.value?.let { message ->
            Text(
                "Firestore reading materials could not load. $message",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                fontSize = 12.sp,
                color = Red
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── SECTION A: Greeting ──────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "TEACHER WORKSPACE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Welcome, ${teacher.name.ifBlank { "Teacher" }}.",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Manage classes, track students, and assign readings.",
                    fontSize = 13.sp,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Stats overview card ──────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, CardBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TeacherMetric(
                            value = "${AppState.classes.size}",
                            label = "Classes",
                            icon = Icons.Filled.School,
                            iconTint = BluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(CardBorder)
                        )
                        TeacherMetric(
                            value = "${AppState.classMembers.size}",
                            label = "Enrollments",
                            icon = Icons.Filled.Group,
                            iconTint = Color(0xFF0F766E),
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier
                                .width(1.dp)
                                .height(48.dp)
                                .background(CardBorder)
                        )
                        TeacherMetric(
                            value = "${AppState.classAssignments.size}",
                            label = "Assignments",
                            icon = Icons.Filled.Assignment,
                            iconTint = Color(0xFFB45309),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // ── Quick action chips ───────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionChip(
                        icon = Icons.Filled.Add,
                        label = "Create class",
                        containerColor = BluePrimary,
                        contentColor = Color.White,
                        onClick = onCreateClass,
                        modifier = Modifier.weight(1f)
                    )
                    ActionChip(
                        icon = Icons.Filled.PostAdd,
                        label = "Create reading",
                        containerColor = InkDark,
                        contentColor = Color.White,
                        onClick = onCreateMaterial,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── SECTION B: Featured Class dark card ──────────────────────
            item {
                val featuredClass = AppState.classes.firstOrNull()
                val featuredMembers = if (featuredClass != null)
                    AppState.classMembers.filter { it.classId == featuredClass.classId }
                else emptyList()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = InkDark),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        if (featuredClass == null) {
                            // Empty state inside dark card
                            Text(
                                "MY CLASS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.60f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Class management unavailable",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Class creation and assignment storage are not connected to Firestore yet.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .clickable(onClick = onCreateClass)
                                    .padding(horizontal = 13.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Create class",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Class header
                            Text(
                                "MY CLASS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.60f),
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                featuredClass.className,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${featuredClass.subject}  •  ${featuredClass.schoolYear}",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.70f)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Class code chip
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { copyCode(featuredClass.classCode) }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    featuredClass.classCode,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy code",
                                    tint = Color.White.copy(alpha = 0.80f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            // Enrolled students sub-section
                            Text(
                                "ENROLLED STUDENTS (${featuredMembers.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.60f),
                                letterSpacing = 0.6.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (featuredMembers.isEmpty()) {
                                Text(
                                    "No students enrolled. Share your class code.",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.60f)
                                )
                            } else {
                                for (member in featuredMembers) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            member.studentName,
                                            fontSize = 13.sp,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            "Enrolled",
                                            fontSize = 11.sp,
                                            color = Color(0xFF4ADE80),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Additional classes — assign reading button
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.10f))
                                    .clickable {
                                        assigningClass = featuredClass
                                    }
                                    .padding(horizontal = 13.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Assign reading to class",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── SECTION C: Active Assignments list ───────────────────────
            item {
                SectionHeader(
                    title = "Active Assignments",
                    detail = "${AppState.classAssignments.size} due"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (AppState.classAssignments.isEmpty()) {
                item {
                    Text(
                        "Assignments are not connected to Firestore yet; no assignment has been created.",
                        fontSize = 13.sp,
                        color = Muted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(AppState.classAssignments, key = { it.id }) { assignment ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                assignment.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                assignment.level,
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Due date chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(OrangeLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                assignment.dueDate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Orange,
                                maxLines = 1
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = CardBorder)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // ── SECTION D: More classes (if > 1) ─────────────────────────
            val extraClasses = AppState.classes.drop(1)
            if (extraClasses.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Other Classes",
                        detail = "${extraClasses.size} more"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(extraClasses, key = { it.classId }) { schoolClass ->
                    val classAssignments = AppState.classAssignments.filter { it.classId == schoolClass.classId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.School,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                schoolClass.className,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${schoolClass.subject}  •  ${classAssignments.size} assignments",
                                fontSize = 12.sp,
                                color = Muted
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(BlueLight)
                                .clickable {
                                    assigningClass = schoolClass
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    schoolClass.classCode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = BluePrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = CardBorder)
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            // ── SECTION E: Teacher-Created Content ───────────────────────
            item {
                SectionHeader(
                    title = "Created reading materials",
                    detail = "Readings and quizzes you have published"
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val teacherMaterials = AppState.materials.filter {
                it.isTeacherMade && it.createdBy == teacher.uid
            }
            if (teacherMaterials.isEmpty()) {
                item {
                    Text(
                        "No Firestore materials are available for this account yet. Create a reading or check your connection.",
                        fontSize = 13.sp,
                        color = Muted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(teacherMaterials, key = { it.id }) { material ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    material.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = InkDark,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
                            if (material.createdBy == teacher.uid && material.isTeacherMade) {
                                IconButton(onClick = { pendingDelete = material }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Red)
                                }
                            }
                        }
                        HorizontalDivider(thickness = 1.dp, color = CardBorder)
                    }
                }
            }
        }
    }

    // ── Assignment feature is unavailable until class storage is configured. ──
    assigningClass?.let { targetClass ->
        AlertDialog(
            onDismissRequest = { assigningClass = null },
            title = { Text("Assignments unavailable") },
            text = {
                Text("Assigning a reading to ${targetClass.className} is not connected to Firestore. No assignment has been saved.")
            },
            confirmButton = {
                TextButton(onClick = { assigningClass = null }) {
                    Text("Close")
                }
            }
        )
    }

    pendingDelete?.let { material ->
        AlertDialog(
            onDismissRequest = { if (!deletingMaterial) pendingDelete = null },
            title = { Text("Delete material?") },
            text = {
                Column {
                    Text("Delete \"${material.title}\" and its quiz questions? This cannot be undone.")
                    deleteError?.let { Text(it, color = Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = {
                Button(
                    enabled = !deletingMaterial,
                    onClick = {
                        deletingMaterial = true
                        deleteError = null
                        readingRepository.deleteTeacherMaterial(material, teacher.uid) { result ->
                            deletingMaterial = false
                            result.onSuccess { AppState.deleteMaterial(material.id); pendingDelete = null }
                                .onFailure { deleteError = it.message ?: "Could not delete this material." }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text(if (deletingMaterial) "Deleting…" else "Delete") }
            },
            dismissButton = { TextButton(enabled = !deletingMaterial, onClick = { pendingDelete = null }) { Text("Cancel") } }
        )
    }
}

/** Metric cell for the stats overview card — matches HomeScreen OverviewMetric. */
@Composable
private fun TeacherMetric(
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(iconTint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
        }
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Muted)
    }
}

/** Compact pill-style action chip for quick actions row. */
@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            maxLines = 1
        )
    }
}
