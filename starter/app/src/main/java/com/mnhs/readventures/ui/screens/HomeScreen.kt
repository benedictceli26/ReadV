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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.SampleData
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.greetingName
import com.mnhs.readventures.ui.theme.BlueDark
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.GreenLight
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Orange
import com.mnhs.readventures.ui.theme.OrangeLight
import com.mnhs.readventures.ui.theme.PurpleLight
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Home screen with Solo Mode vs Class Mode switch, Dictionary,
 * Reading Levels, and Teacher Dashboard access.
 */
@Composable
fun HomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenReading: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenDictionary: () -> Unit = {},
    onOpenLevels: () -> Unit = {},
    onOpenMyClass: () -> Unit = {},
    onJoinClass: () -> Unit = {},
    onTeacherDashboard: () -> Unit = {}
) {
    val continueMaterial: ReadingMaterial =
        AppState.continueReadingMaterial() ?: SampleData.materials.first()
    val continueProgress = AppState.readingProgress[continueMaterial.id] ?: 0
    val isTeacher = AppState.currentUser.value.role == "teacher"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
    ) {
        AppTopBar(onProfileClick = onOpenProfile)

        // Teacher banner if logged in as teacher
        if (isTeacher) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueDark)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Teacher Mode Active",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Manage classes, assign readings & create quizzes",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Button(
                        onClick = onTeacherDashboard,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Dashboard →", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // FEATURE 3: Solo Mode vs Class Mode Segmented Control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!AppState.isClassMode.value) BluePrimary else Color.Transparent)
                    .clickable { AppState.isClassMode.value = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Solo Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!AppState.isClassMode.value) Color.White else Muted
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (AppState.isClassMode.value) BluePrimary else Color.Transparent)
                    .clickable { AppState.isClassMode.value = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Class Mode",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (AppState.isClassMode.value) Color.White else Muted
                )
            }
        }

        if (AppState.isClassMode.value) {
            // CLASS MODE VIEW
            val enrolledCodes = AppState.enrolledClassCodes
            val joinedClasses = AppState.classes.filter { enrolledCodes.contains(it.classCode) }

            if (joinedClasses.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Not Enrolled in a Class",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Join your class using your teacher's 6-character code (demo code: READ-7A1) to see assignments.",
                            fontSize = 13.sp,
                            color = Muted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onJoinClass,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                        ) {
                            Text("Join Class with Code")
                        }
                    }
                }
            } else {
                for (sc in joinedClasses) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BluePrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    sc.className,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "${sc.subject}  •  Teacher: ${sc.teacherName}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(BlueDark)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    sc.classCode,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Assigned readings
                    val assignments = AppState.classAssignments.filter { it.classId == sc.classId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Assigned Readings",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = InkDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "View My Class →",
                            fontSize = 12.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onOpenMyClass() }
                        )
                    }

                    for (assign in assignments) {
                        val progress = AppState.readingProgress[assign.materialId] ?: 0
                        val best = AppState.bestScores[assign.materialId]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 5.dp)
                                .clickable { onOpenReading(assign.materialId) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        assign.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = InkDark
                                    )
                                    Text(
                                        "Due: ${assign.dueDate}  •  ${assign.level}",
                                        fontSize = 11.sp,
                                        color = Orange,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = BluePrimary,
                                        trackColor = BlueLight
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Read: $progress%  |  Quiz: ${if (best != null) "$best/5" else "Pending"}",
                                        fontSize = 11.sp,
                                        color = if (best != null && best >= 4) Green else Muted
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(BluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.PlayArrow,
                                        contentDescription = "Read",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // SOLO MODE VIEW (Default / Original Home)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BluePrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hello, ${greetingName()}!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Let's improve your reading skills today!",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onOpenLibrary,
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text("Start Reading →", color = BluePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BlueDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Reading Categories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reading Categories", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "See all →",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    modifier = Modifier.clickable { onOpenLibrary() }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryCard("Fiction", Icons.Filled.Book, BlueLight, Modifier.weight(1f)) { onOpenCategory("Fiction") }
                CategoryCard("Non-Fiction", Icons.Filled.Public, GreenLight, Modifier.weight(1f)) { onOpenCategory("Non-Fiction") }
                CategoryCard("Science", Icons.Filled.Science, OrangeLight, Modifier.weight(1f)) { onOpenCategory("Science") }
                CategoryCard("Classic", Icons.Filled.TheaterComedy, PurpleLight, Modifier.weight(1f)) { onOpenCategory("Classic") }
            }

            // Continue Reading
            Text(
                "Continue Reading",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clickable { onOpenReading(continueMaterial.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Book, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(continueMaterial.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                        Text("Category: ${continueMaterial.topic}  •  ${continueMaterial.level}", fontSize = 12.sp, color = Muted)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { continueProgress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = BluePrimary,
                            trackColor = BlueLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$continueProgress% read", fontSize = 12.sp, color = Muted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(BluePrimary)
                            .clickable { onOpenReading(continueMaterial.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Continue", tint = Color.White)
                    }
                }
            }
        }

        // Quick Actions
        Text(
            "Quick Actions & Learning Tools",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenDictionary() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BlueLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Spellcheck, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dictionary", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Text("Glossary & vocabulary", fontSize = 11.sp, color = Muted)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenLevels() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.LockOpen, contentDescription = null, tint = Orange)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Levels", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Text("${AppState.currentLevel().name.substringBefore(":")} open", fontSize = 11.sp, color = Muted)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenQuiz(continueMaterial.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PurpleLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.Quiz, contentDescription = null, tint = BluePrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Take a Quiz", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Text("Instant test & feedback", fontSize = 11.sp, color = Muted)
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenProgress() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Green)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("My Progress", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Text("Track scores & stats", fontSize = 11.sp, color = Muted)
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryCard(
    label: String,
    icon: ImageVector,
    bg: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bg)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = InkDark, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
        }
    }
}
