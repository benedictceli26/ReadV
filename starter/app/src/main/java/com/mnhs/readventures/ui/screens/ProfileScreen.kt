package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Profile screen: profile icon, student details, stats, Account Settings
 * (Edit Profile, Change Password, Notifications, My Progress), plus
 * Help & Support, About ReadVentures, and Log Out.
 */
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onOpenProgress: () -> Unit,
    onLogOut: () -> Unit,
    onOpenDictionary: () -> Unit = {},
    onOpenLevels: () -> Unit = {},
    onOpenMyClass: () -> Unit = {},
    onTeacherDashboard: () -> Unit = {}
) {
    val user = AppState.currentUser.value
    val isTeacher = user.role == "teacher"
    var showAbout by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showLogOut by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        AppTopBar(title = "My Profile", subtitle = "Manage your account.", onProfileClick = {})

        // Profile card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Profile icon", tint = InkDark, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        user.name.ifBlank { if (isTeacher) "Teacher profile" else "Student profile" },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark,
                        maxLines = 1
                    )
                    Text(
                        if (isTeacher) "Teacher account" else "Student account",
                        fontSize = 13.sp,
                        color = BluePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(user.email.ifBlank { "No email added" }, fontSize = 12.sp, color = Muted, maxLines = 1)
                }
            }
        }

        // Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatBox("${AppState.readCount()}", "Stories", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f))
            StatBox("${AppState.quizzesTaken()}", "Quizzes", Icons.Filled.Quiz, Modifier.weight(1f))
            StatBox("${AppState.averageScorePercent()}", "Average", null, Modifier.weight(1f))
        }

        Text(
            "Learning & Activities",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column {
                SettingsRow(androidx.compose.material.icons.Icons.Filled.Spellcheck, "Dictionary & Glossary", onClick = onOpenDictionary)
                SettingsRow(androidx.compose.material.icons.Icons.Filled.LockOpen, "Reading Levels (${AppState.currentLevel().name.substringBefore(":")})", onClick = onOpenLevels)
                SettingsRow(androidx.compose.material.icons.Icons.Filled.Class, "My Class & Assignments", onClick = onOpenMyClass)
                if (isTeacher) {
                    SettingsRow(androidx.compose.material.icons.Icons.Filled.Dashboard, "Teacher Dashboard", onClick = onTeacherDashboard)
                }
            }
        }

        Text(
            "Account Settings",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column {
                SettingsRow(Icons.Filled.Edit, "Edit Profile", onClick = onEditProfile)
                SettingsRow(Icons.Filled.Lock, "Change Password", onClick = onChangePassword)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = null, tint = InkDark)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Notifications", fontSize = 14.sp, color = InkDark, modifier = Modifier.weight(1f))
                    Switch(
                        checked = AppState.notificationsEnabled.value,
                        onCheckedChange = { AppState.notificationsEnabled.value = it }
                    )
                }
                SettingsRow(Icons.Filled.TrendingUp, "My Progress", onClick = onOpenProgress)
            }
        }

        Text(
            "More",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column {
                SettingsRow(Icons.Filled.Help, "Help & Support", onClick = { showHelp = true })
                SettingsRow(Icons.Filled.Info, "About ReadVentures", onClick = { showAbout = true })
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogOut = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, tint = Red)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Red, modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Muted)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About ReadVentures") },
            text = {
                Text(
                    "ReadVentures (Read + Adventures) — \"Read. Understand. Explore.\"\n\n" +
                        "A smart mobile application for improving reading and " +
                        "comprehension skills, for Muntinlupa National High School.\n\n" +
                        "Version 1.0 — Student app (Android, Kotlin + Firebase)."
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close") } }
        )
    }
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Help & Support") },
            text = {
                Text(
                    "How to use ReadVentures:\n\n" +
                        "1. Open a story in the Library and read it.\n" +
                        "2. Move the reading slider and tap Save Progress to continue later.\n" +
                        "3. Take the quiz after reading — you get instant feedback.\n" +
                        "4. Check My Progress to see your scores.\n\n" +
                        "For help, ask your teacher or the app proponents."
                )
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Close") } }
        )
    }
    if (showLogOut) {
        AlertDialog(
            onDismissRequest = { showLogOut = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = { showLogOut = false; onLogOut() }) {
                    Text("Log Out", color = Red)
                }
            },
            dismissButton = { TextButton(onClick = { showLogOut = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun StatBox(
    value: String,
    label: String,
    icon: ImageVector?,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon == null) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { (value.toIntOrNull() ?: 0).coerceIn(0, 100) / 100f },
                        modifier = Modifier.size(36.dp),
                        color = BluePrimary,
                        trackColor = BlueLight,
                        strokeWidth = 3.dp
                    )
                    Text("$value%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = InkDark)
                }
            } else {
                Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.height(3.dp))
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
            }
            Text(label, fontSize = 10.sp, color = Muted, maxLines = 1)
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(BlueLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, color = InkDark, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Muted)
    }
}
