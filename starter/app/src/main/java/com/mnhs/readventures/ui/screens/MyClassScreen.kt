package com.mnhs.readventures.ui.screens

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueDark
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Orange
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 3: My Class View for Students.
 * Shows teacher-assigned readings first, with due date and completion status.
 */
@Composable
fun MyClassScreen(
    onBack: () -> Unit,
    onJoinClass: () -> Unit,
    onOpenReading: (String) -> Unit
) {
    val enrolledCodes = AppState.enrolledClassCodes
    val studentClasses = AppState.classes.filter { enrolledCodes.contains(it.classCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "My Class", onBack = onBack)

        if (studentClasses.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Class,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Class data is unavailable",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Class enrollment and assignment data are not connected to Firestore yet. No class information is being saved locally.",
                    fontSize = 13.sp,
                    color = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onJoinClass,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Check class availability")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (schoolClass in studentClasses) {
                    item(key = schoolClass.classId) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = InkDark),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            schoolClass.className,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "${schoolClass.subject}  •  ${schoolClass.schoolYear}",
                                            fontSize = 13.sp,
                                            color = Color.White.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            "Teacher: ${schoolClass.teacherName}",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BlueDark)
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            schoolClass.classCode,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val assignments = AppState.classAssignments.filter { it.classId == schoolClass.classId }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Assignment,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Assigned Readings (${assignments.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = InkDark
                            )
                        }
                    }

                    if (assignments.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Text(
                                    "No readings assigned yet for this class.",
                                    fontSize = 13.sp,
                                    color = Muted,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(assignments, key = { it.id }) { assignment ->
                            val progress = AppState.readingProgress[assignment.materialId] ?: 0
                            val best = AppState.bestScores[assignment.materialId]
                            val isCompleted = AppState.finishedReading.contains(assignment.materialId)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenReading(assignment.materialId) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                assignment.title,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = InkDark
                                            )
                                            Text(
                                                "Due: ${assignment.dueDate}  •  ${assignment.level}",
                                                fontSize = 12.sp,
                                                color = Orange,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(BluePrimary)
                                                .clickable { onOpenReading(assignment.materialId) },
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
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.fillMaxWidth().height(7.dp),
                                        color = BluePrimary,
                                        trackColor = BlueLight
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row {
                                        Text(
                                            if (isCompleted) "Reading completed (100%)" else "$progress% read",
                                            fontSize = 11.sp,
                                            color = if (isCompleted) Green else Muted,
                                            fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            if (best != null) "Quiz: $best/5" else "Quiz not taken",
                                            fontSize = 11.sp,
                                            color = if (best != null && best >= 4) Green else Muted,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onJoinClass,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("+ Join Another Class", color = BluePrimary)
                    }
                }
            }
        }
    }
}
