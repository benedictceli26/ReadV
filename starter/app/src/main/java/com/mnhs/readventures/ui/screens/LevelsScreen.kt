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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.Level
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.GreenLight
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 2: Level Progression & Gating Screen.
 * Lists levels with lock/unlocked status and story breakdown.
 */
@Composable
fun LevelsScreen(
    onBack: () -> Unit,
    onOpenReading: (String) -> Unit
) {
    var lockedDialogLevel by remember { mutableStateOf<Level?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Reading Levels", onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(AppState.levels, key = { it.id }) { level ->
                val isUnlocked = AppState.isLevelUnlocked(level.id)
                val levelMaterials = AppState.materials.filter { it.levelId == level.id }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isUnlocked) {
                                lockedDialogLevel = level
                            }
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.White else Color(0xFFF1F5F9)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isUnlocked) BlueLight else Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUnlocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = if (isUnlocked) BluePrimary else Muted,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    level.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUnlocked) InkDark else Muted
                                )
                                Text(
                                    level.description,
                                    fontSize = 12.sp,
                                    color = Muted
                                )
                            }
                            if (isUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GreenLight)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("OPEN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Green)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("LOCKED", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (isUnlocked) {
                            Text(
                                "Stories in this level:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            for (material in levelMaterials) {
                                val isRead = AppState.finishedReading.contains(material.id)
                                val best = AppState.bestScores[material.id]
                                val progress = AppState.readingProgress[material.id] ?: 0

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onOpenReading(material.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isRead && (best ?: 0) >= 4) Icons.Filled.CheckCircle else Icons.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = if (isRead && (best ?: 0) >= 4) Green else BluePrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                material.title,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = InkDark
                                            )
                                            Text(
                                                "Read: $progress%  •  Quiz: ${if (best != null) "$best/5" else "Not taken"}",
                                                fontSize = 11.sp,
                                                color = Muted
                                            )
                                        }
                                        Icon(
                                            Icons.Filled.ChevronRight,
                                            contentDescription = null,
                                            tint = Muted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                "Unlock Requirement:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Muted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                AppState.levelUnlockRequirement(level.id),
                                fontSize = 13.sp,
                                color = Muted
                            )
                        }
                    }
                }
            }
        }
    }

    lockedDialogLevel?.let { level ->
        AlertDialog(
            onDismissRequest = { lockedDialogLevel = null },
            icon = {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text("${level.name} is Locked")
            },
            text = {
                val priorLevelNumber = (level.order - 1).coerceAtLeast(1)
                Text("Finish Level $priorLevelNumber first to unlock.\n\n${AppState.levelUnlockRequirement(level.id)}")
            },
            confirmButton = {
                TextButton(onClick = { lockedDialogLevel = null }) {
                    Text("Got it", color = BluePrimary)
                }
            }
        )
    }
}
