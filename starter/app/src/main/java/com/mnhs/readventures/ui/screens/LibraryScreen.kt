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
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Library screen: search + filters (All / Stories / Informational),
 * level grouping with locked-state gating.
 */
@Composable
fun LibraryScreen(
    materials: List<ReadingMaterial>,
    initialCategory: String? = null,
    onOpenReading: (String) -> Unit,
    onOpenProfile: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }
    var category by remember { mutableStateOf(initialCategory) }
    var lockedDialogMaterial by remember { mutableStateOf<ReadingMaterial?>(null) }

    val filtered = materials.filter { material ->
        val matchesQuery = query.isBlank() ||
            material.title.contains(query, ignoreCase = true) ||
            material.topic.contains(query, ignoreCase = true)
        val matchesKind = filter == "All" || material.kind == filter
        val matchesCategory = category == null || material.category == category
        matchesQuery && matchesKind && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        AppTopBar(
            title = "My Library",
            subtitle = "Hi, ${com.mnhs.readventures.ui.components.greetingName()}! Keep reading today.",
            onProfileClick = onOpenProfile
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search stories...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(22.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (option in listOf("All", "Stories", "Informational")) {
                FilterChip(
                    selected = filter == option,
                    onClick = { filter = option },
                    label = { Text(option) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            if (category != null) {
                FilterChip(
                    selected = true,
                    onClick = { category = null },
                    label = { Text("$category ✕") }
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Text(
                "No stories found. Try a different search or filter.",
                color = Muted,
                modifier = Modifier.padding(20.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { material ->
                val isUnlocked = AppState.isLevelUnlocked(material.levelId)
                val progress = AppState.readingProgress[material.id] ?: 0
                val best = AppState.bestScores[material.id]

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isUnlocked) {
                                onOpenReading(material.id)
                            } else {
                                lockedDialogMaterial = material
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color.White else Color(0xFFF1F5F9)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 2.dp else 0.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isUnlocked) BlueLight else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Filled.Book else Icons.Filled.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) BluePrimary else Muted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    material.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isUnlocked) InkDark else Muted,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!isUnlocked) {
                                    Text(
                                        "LOCKED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Muted
                                    )
                                }
                            }
                            Text(
                                "${material.topic}  •  ${material.level}",
                                fontSize = 12.sp,
                                color = Muted
                            )

                            if (isUnlocked) {
                                if (progress > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = BluePrimary,
                                        trackColor = BlueLight
                                    )
                                    Text("$progress% read", fontSize = 11.sp, color = Muted)
                                }
                                if (best != null) {
                                    Text("Best quiz score: $best/5", fontSize = 12.sp, color = Green)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tap to view unlock requirements",
                                    fontSize = 11.sp,
                                    color = Muted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    lockedDialogMaterial?.let { material ->
        val levelObj = AppState.levels.firstOrNull { it.id == material.levelId }
        val priorLevelNumber = ((levelObj?.order ?: 2) - 1).coerceAtLeast(1)

        AlertDialog(
            onDismissRequest = { lockedDialogMaterial = null },
            icon = {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Story is Locked")
            },
            text = {
                Text(
                    "\"${material.title}\" is in ${material.level}.\n\n" +
                        "Finish Level $priorLevelNumber first to unlock.\n\n" +
                        AppState.levelUnlockRequirement(material.levelId)
                )
            },
            confirmButton = {
                TextButton(onClick = { lockedDialogMaterial = null }) {
                    Text("Got it", color = BluePrimary)
                }
            }
        )
    }
}
