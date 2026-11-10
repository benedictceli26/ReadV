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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.DictionaryWord
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * FEATURE 1: Dictionary / Glossary Screen.
 * Searchable list of words with tap-to-view definition, example, and source story.
 */
@Composable
fun DictionaryScreen(
    onBack: () -> Unit,
    onOpenReading: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedWord by remember { mutableStateOf<DictionaryWord?>(null) }

    val allWords = AppState.dictionaryWords
    val filteredWords = allWords.filter { item ->
        searchQuery.isBlank() ||
            item.word.contains(searchQuery, ignoreCase = true) ||
            item.definition.contains(searchQuery, ignoreCase = true) ||
            item.partOfSpeech.contains(searchQuery, ignoreCase = true)
    }.sortedBy { it.word.lowercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Dictionary & Glossary", onBack = onBack)

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search words, definitions...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(22.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${filteredWords.size} vocabulary words",
                fontSize = 13.sp,
                color = Muted,
                fontWeight = FontWeight.Medium
            )
        }

        if (filteredWords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No matching words found. Try a different search.",
                    color = Muted,
                    fontSize = 14.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredWords, key = { "${it.word}-${it.materialId}" }) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedWord = item },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BlueLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.MenuBook,
                                contentDescription = null,
                                tint = BluePrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    item.word,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BluePrimary
                                )
                                if (item.partOfSpeech.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "(${item.partOfSpeech})",
                                        fontSize = 12.sp,
                                        fontStyle = FontStyle.Italic,
                                        color = Muted
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                item.definition,
                                fontSize = 13.sp,
                                color = InkDark,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${item.level}  •  Tap to view details",
                                fontSize = 11.sp,
                                color = Muted
                            )
                        }
                    }
                }
            }
        }
    }

    selectedWord?.let { word ->
        val sourceStory = AppState.materials.firstOrNull { it.id == word.materialId }
        AlertDialog(
            onDismissRequest = { selectedWord = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        word.word,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                    if (word.partOfSpeech.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "(${word.partOfSpeech})",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = Muted
                        )
                    }
                }
            },
            text = {
                Column {
                    Text(
                        "Definition:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted
                    )
                    Text(word.definition, fontSize = 15.sp, color = InkDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (word.example.isNotBlank()) {
                        Text(
                            "Example in sentence:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Muted
                        )
                        Text(
                            "\"${word.example}\"",
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            color = InkDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (sourceStory != null) {
                        Text(
                            "Appears in:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Muted
                        )
                        Text(
                            "${sourceStory.title} (${word.level})",
                            fontSize = 13.sp,
                            color = BluePrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                if (sourceStory != null) {
                    TextButton(onClick = {
                        val matId = word.materialId
                        selectedWord = null
                        onOpenReading(matId)
                    }) {
                        Text("Open Story", color = BluePrimary)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedWord = null }) {
                    Text("Close")
                }
            }
        )
    }
}
