package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
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
            StatCard("$readCount", "Read", Modifier.weight(1f))
            StatCard("$quizzes", "Quizzes", Modifier.weight(1f))
            StatCard("$average%", "Avg Score", Modifier.weight(1f))
        }

        // FEATURE 2: Current Level & Unlock Status
        val currLevel = AppState.currentLevel()
        val nextLevelId = when (currLevel.id) {
            "level-1" -> "level-2"
            "level-2" -> "level-3"
            else -> null
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            }
        }

        Text(
            "My Scores",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )

        if (AppState.scores.isEmpty()) {
            Text(
                "No quizzes taken yet. Finish a story and take its quiz — your score will appear here.",
                fontSize = 14.sp,
                color = Muted,
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
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                material.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = InkDark,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (best != null) "$best/5" else "—/5",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = BluePrimary,
                            trackColor = BlueLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Reading: $progress%  •  ${if (best != null) "Best quiz: $best/5" else "Quiz not taken yet"}",
                            fontSize = 12.sp,
                            color = Muted
                        )
                    }
                }
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
private fun StatCard(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
            Text(label, fontSize = 11.sp, color = Muted)
        }
    }
}
