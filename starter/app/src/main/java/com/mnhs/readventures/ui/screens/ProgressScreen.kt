package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.SampleData
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.EmptyState
import com.mnhs.readventures.ui.components.ProgressCard
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * My Progress / Results screen: simple on-device progress view.
 * Read count, quizzes taken, average score, and a per-material list.
 */
@Composable
fun ProgressScreen(onOpenProfile: () -> Unit) {
    val readCount = AppState.readCount()
    val quizzes = AppState.quizzesTaken()
    val average = AppState.averageScorePercent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        AppTopBar(title = "My Progress", subtitle = "See how you're doing.", onProfileClick = onOpenProfile)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard("$readCount", "Stories read", Icons.AutoMirrored.Filled.MenuBook, Modifier.weight(1f))
            StatCard("$quizzes", "Quizzes", Icons.Filled.Quiz, Modifier.weight(1f))
            StatCard("$average", "Avg score", null, Modifier.weight(1f))
        }

        Text(
            "Reading completion is cached on this device. Quiz scores are loaded from Firebase.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            fontSize = 12.sp,
            color = Muted
        )

        // FEATURE 2: Current Level & Unlock Status
        val currLevel = AppState.currentLevel()
        val nextLevelId = when (currLevel.id) {
            "level-1" -> "level-2"
            "level-2" -> "level-3"
            else -> null
        }
        val currentLevelMaterials = AppState.materials.filter { it.levelId == currLevel.id }
        val completedInLevel = currentLevelMaterials.count { it.id in AppState.finishedReading }
        val levelProgress = if (currentLevelMaterials.isEmpty()) 0f
            else completedInLevel.toFloat() / currentLevelMaterials.size

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Current Reading Level:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        currLevel.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (nextLevelId != null) {
                    Text(
                        "To Unlock Next Level:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark
                    )
                    Text(
                        AppState.levelUnlockRequirement(nextLevelId),
                        fontSize = 12.sp,
                        color = Muted
                    )
                } else {
                    Text(
                        "All reading levels unlocked! Outstanding work!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = com.mnhs.readventures.ui.theme.Green
                    )
                }
                if (currentLevelMaterials.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$completedInLevel of ${currentLevelMaterials.size} stories completed",
                            fontSize = 12.sp,
                            color = Muted,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${(levelProgress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BluePrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { levelProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = BluePrimary,
                        trackColor = BlueLight
                    )
                }
            }
        }

        Text(
            "My Scores",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        AppState.scoreHistoryError.value?.let { message ->
            Text(
                message,
                fontSize = 13.sp,
                color = com.mnhs.readventures.ui.theme.Red,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        if (AppState.scores.isEmpty()) {
            EmptyState(
                title = "Your quiz history will appear here",
                detail = "Server-verified Firebase quiz results appear here after they load.",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // One row per material with its best score / reading progress.
            items(SampleData.materials, key = { it.id }) { material ->
                val best = AppState.bestScores[material.id]
                val progress = AppState.readingProgress[material.id] ?: 0
                ProgressCard(
                    title = material.title,
                    detail = "Reading: $progress%  •  ${if (best != null) "Best quiz: $best/5" else "Quiz not taken yet"}",
                    progress = progress / 100f
                )
            }
            // Latest quiz attempts, newest first (keyed by index: two identical
            // scores for the same story must still have unique keys).
            itemsIndexed(AppState.scores, key = { index, item -> "$index-${item.id}" }) { _, score ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            score.materialTitle.ifBlank { score.materialId },
                            fontSize = 14.sp,
                            color = InkDark,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${score.score}/${score.total}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
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
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon == null) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { (value.toIntOrNull() ?: 0).coerceIn(0, 100) / 100f },
                        modifier = Modifier.size(40.dp),
                        color = BluePrimary,
                        trackColor = BlueLight,
                        strokeWidth = 3.dp
                    )
                    Text("$value%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkDark)
                }
            } else {
                Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp), tint = BluePrimary)
                Spacer(modifier = Modifier.height(3.dp))
                Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
            }
            Text(label, fontSize = 10.sp, color = Muted, maxLines = 1)
        }
    }
}
