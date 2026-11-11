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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.AppTextField
import com.mnhs.readventures.ui.components.EmptyState
import com.mnhs.readventures.ui.components.SectionHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg
import com.mnhs.readventures.ui.components.greetingName

/**
 * Library screen: search + filters, 2-column book-cover grid.
 * Locked covers show a LOCKED overlay. Progress bar + quiz status below each cover.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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
    var showFilters by remember { mutableStateOf(false) }
    var draftFilter by remember { mutableStateOf(filter) }
    var draftLevelFilter by remember { mutableStateOf(levelFilter) }
    var lockedDialogMaterial by remember { mutableStateOf<ReadingMaterial?>(null) }

    val materialCategories = materials.map { it.category }.filter { it.isNotBlank() }.distinct()
    val categories = listOf("Classic", "Fiction", "Non-Fiction")
        .filter { it in materialCategories } + materialCategories.filterNot {
            it in listOf("Classic", "Fiction", "Non-Fiction")
        }.sorted()
    val levels = materials.map { it.level }.filter { it.isNotBlank() }.distinct().sorted()
    val activeFilterCount = (if (filter == "All") 0 else 1) + if (levelFilter == null) 0 else 1
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
            subtitle = "Hi, ${greetingName()}! Keep reading today.",
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

        // Filter chip row
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = category == null,
                    onClick = { category = null },
                    label = { Text("All topics", maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BluePrimary,
                        selectedLabelColor = Color.White
                    )
                )
                categories.forEach { topic ->
                    FilterChip(
                        selected = category == topic,
                        onClick = { category = if (category == topic) null else topic },
                        label = { Text(topic, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilterChip(
                selected = activeFilterCount > 0,
                onClick = {
                    draftFilter = filter
                    draftLevelFilter = levelFilter
                    showFilters = true
                },
                label = { Text(if (activeFilterCount > 0) "Filter $activeFilterCount" else "Filter", maxLines = 1) },
                leadingIcon = {
                    Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = BluePrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
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
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filtered, key = { it.id }) { material ->
                    val isUnlocked = AppState.isLevelUnlocked(material.levelId)
                    val progress = AppState.readingProgress[material.id] ?: 0
                    val best = AppState.bestScores[material.id]

                    BookCoverGridItem(
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
    }

    // Filter bottom sheet
    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ScreenBg
        ) {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 620.dp).verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Filter library", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        Text("Narrow by content type and reading level.", fontSize = 13.sp, color = Muted)
                    }
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close filters",
                        tint = Muted,
                        modifier = Modifier.size(24.dp).clickable { showFilters = false }
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Content type", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Stories", "Informational").forEach { option ->
                        FilterChip(
                            selected = draftFilter == option,
                            onClick = { draftFilter = option },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Reading level", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = draftLevelFilter == null,
                        onClick = { draftLevelFilter = null },
                        label = { Text("All Levels") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BluePrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                    levels.forEach { level ->
                        val shortLevel = level.substringBefore(" (")
                        FilterChip(
                            selected = draftLevelFilter == level,
                            onClick = { draftLevelFilter = if (draftLevelFilter == level) null else level },
                            label = { Text(shortLevel, maxLines = 1) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            draftFilter = "All"
                            draftLevelFilter = null
                        }
                    ) {
                        Text("Reset", color = BluePrimary)
                    }
                    com.mnhs.readventures.ui.components.PrimaryButton(
                        "Apply filters",
                        onClick = {
                            filter = draftFilter
                            levelFilter = draftLevelFilter
                            showFilters = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Locked dialog
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

/** Cover color palette per category — no copyright, all geometric art. */
private fun coverGradient(category: String): Brush {
    return when (category.lowercase()) {
        "fiction" -> Brush.verticalGradient(
            listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
        )
        "non-fiction" -> Brush.verticalGradient(
            listOf(Color(0xFF0F766E), Color(0xFF0D6161))
        )
        "science" -> Brush.verticalGradient(
            listOf(Color(0xFFB45309), Color(0xFF92400E))
        )
        "classic" -> Brush.verticalGradient(
            listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))
        )
        else -> Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E293B))
        )
    }
}

@Composable
private fun BookCoverGridItem(
    material: ReadingMaterial,
    isUnlocked: Boolean,
    progress: Int,
    bestScore: Int?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        // Cover art
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(14.dp))
                .background(coverGradient(material.category))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp)
                )
        ) {
            // Decorative spine line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.08f))
                    .align(Alignment.CenterStart)
            )

            // Cover content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top: category tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        material.category.ifBlank { material.topic }.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.90f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Middle: book icon
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.30f),
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Bottom: title
                Column {
                    Text(
                        material.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        material.level.substringBefore(" ("),
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.65f),
                        maxLines = 1
                    )
                }
            }

            // Locked overlay
            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000000).copy(alpha = 0.60f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "LOCKED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        // Progress + quiz status below cover
        Spacer(modifier = Modifier.height(8.dp))
        if (isUnlocked) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
                color = BluePrimary,
                trackColor = BlueLight
            )
            Spacer(modifier = Modifier.height(5.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$progress%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress > 0) BluePrimary else Muted
                )
                Text(
                    if (bestScore != null) "Quiz ✓ $bestScore/5" else "No quiz",
                    fontSize = 11.sp,
                    color = if (bestScore != null) Green else Muted
                )
            }
        } else {
            Text(
                "Tap to unlock",
                fontSize = 11.sp,
                color = Muted
            )
        }
    }
}
