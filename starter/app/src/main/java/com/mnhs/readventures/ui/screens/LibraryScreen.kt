package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.mnhs.readventures.ui.components.AppTextField
import com.mnhs.readventures.ui.components.EmptyState
import com.mnhs.readventures.ui.components.ReadingMaterialCard
import com.mnhs.readventures.ui.components.SectionHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
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
    var levelFilter by remember { mutableStateOf<String?>(null) }
    var lockedDialogMaterial by remember { mutableStateOf<ReadingMaterial?>(null) }

    val categories = materials.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    val levels = materials.map { it.level }.filter { it.isNotBlank() }.distinct().sorted()
    val filtered = materials.filter { material ->
        val matchesQuery = query.isBlank() ||
            material.title.contains(query, ignoreCase = true) ||
            material.topic.contains(query, ignoreCase = true)
        val matchesKind = filter == "All" || material.kind == filter
        val matchesCategory = category == null || material.category == category
        val matchesLevel = levelFilter == null || material.level == levelFilter
        matchesQuery && matchesKind && matchesCategory && matchesLevel
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

        AppTextField(
            value = query,
            onValueChange = { query = it },
            label = "Search library",
            placeholder = "Find a title or topic",
            leadingIcon = Icons.Filled.Search,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
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
        }
        if (categories.isNotEmpty()) {
            Text(
                "TOPICS",
                modifier = Modifier.padding(start = 20.dp, top = 7.dp, bottom = 5.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = .7.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = category == null,
                    onClick = { category = null },
                    label = { Text("All topics") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                categories.forEach { topic ->
                    FilterChip(
                        selected = category == topic,
                        onClick = { category = if (category == topic) null else topic },
                        label = { Text(topic) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        if (levels.isNotEmpty()) {
            Text(
                "READING LEVEL",
                modifier = Modifier.padding(start = 20.dp, top = 7.dp, bottom = 5.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Muted,
                letterSpacing = .7.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = levelFilter == null,
                    onClick = { levelFilter = null },
                    label = { Text("All levels") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                levels.forEach { level ->
                    FilterChip(
                        selected = levelFilter == level,
                        onClick = { levelFilter = if (levelFilter == level) null else level },
                        label = { Text(level) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            title = if (category == null) "Browse reading materials" else "$category reads",
            detail = "${filtered.size} ${if (filtered.size == 1) "material" else "materials"} available",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            EmptyState(
                title = "No materials found",
                detail = "Try another search term or remove a filter.",
                modifier = Modifier.padding(horizontal = 20.dp)
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

                ReadingMaterialCard(
                    material = material,
                    isUnlocked = isUnlocked,
                    progress = progress,
                    bestScore = best,
                    onClick = {
                        if (isUnlocked) onOpenReading(material.id)
                        else lockedDialogMaterial = material
                    }
                )
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
