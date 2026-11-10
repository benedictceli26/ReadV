package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.DictionaryWord
import com.mnhs.readventures.data.ProgressStore
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.components.SecondaryButton
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg
import java.util.regex.Pattern

/**
 * Reading screen with save-and-continue and vocabulary tap-to-define.
 *
 * Difficult words in the story text are highlighted with blue underlines.
 * Tapping any highlighted word displays its dictionary definition,
 * part of speech, and example sentence in an instant popup.
 */
@Composable
fun ReadingScreen(
    material: ReadingMaterial,
    progressStore: ProgressStore,
    onBack: () -> Unit,
    onStartQuiz: (String) -> Unit
) {
    val saved = AppState.readingProgress[material.id] ?: progressStore.readingProgress(material.id)
    var slider by remember(material.id) { mutableFloatStateOf(saved.toFloat()) }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var selectedWord by remember { mutableStateOf<DictionaryWord?>(null) }

    val storyWords = AppState.dictionaryWords.filter {
        it.materialId == material.id || material.body.contains(it.word, ignoreCase = true)
    }

    fun persist(percent: Int, message: String) {
        progressStore.saveReadingProgress(material.id, percent)
        AppState.recordReadingProgress(material.id, percent)
        savedMessage = message
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Reading", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                material.title,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark
            )
            Text(
                "${material.topic}  •  ${material.level}  •  ${material.category}",
                fontSize = 13.sp,
                color = Muted
            )
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { slider / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = BluePrimary,
                trackColor = BlueLight
            )
            Text(
                "${slider.toInt()}% read",
                modifier = Modifier.padding(top = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Muted
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Glossary hint banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Tap any blue underlined word to view its vocabulary definition.",
                        fontSize = 12.sp,
                        color = BluePrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    for (paragraph in material.body.split("\n")) {
                        if (paragraph.isBlank()) continue

                        val annotatedText = buildAnnotatedString {
                            append(paragraph)
                            for (dictWord in storyWords) {
                                val pattern = Pattern.compile(
                                    "\\b" + Pattern.quote(dictWord.word) + "\\b",
                                    Pattern.CASE_INSENSITIVE
                                )
                                val matcher = pattern.matcher(paragraph)
                                while (matcher.find()) {
                                    val start = matcher.start()
                                    val end = matcher.end()
                                    addStyle(
                                        style = SpanStyle(
                                            color = BluePrimary,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = TextDecoration.Underline
                                        ),
                                        start = start,
                                        end = end
                                    )
                                    addStringAnnotation(
                                        tag = "DICT_WORD",
                                        annotation = dictWord.word,
                                        start = start,
                                        end = end
                                    )
                                }
                            }
                        }

                        ClickableText(
                            text = annotatedText,
                            style = TextStyle(
                                fontSize = 17.sp,
                                color = InkDark,
                                lineHeight = 29.sp
                            ),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations("DICT_WORD", offset, offset)
                                    .firstOrNull()?.let { annotation ->
                                        selectedWord = storyWords.firstOrNull {
                                            it.word.equals(annotation.item, ignoreCase = true)
                                        }
                                    }
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Where are you in the story?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
            Slider(
                value = slider,
                onValueChange = { slider = it },
                valueRange = 0f..100f,
                steps = 9
            )
            Text(
                "Move the slider as you read, then save — the app remembers your place.",
                fontSize = 12.sp,
                color = Muted
            )
            savedMessage?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, fontSize = 13.sp, color = Green)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            PrimaryButton(
                text = "Save progress",
                onClick = { persist(slider.toInt(), "Progress saved — you can continue later.") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecondaryButton(
                text = "Finish reading & start quiz",
                onClick = {
                    persist(100, "Finished! Great reading.")
                    onStartQuiz(material.id)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    selectedWord?.let { word ->
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
                        "Meaning:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Muted
                    )
                    Text(word.definition, fontSize = 15.sp, color = InkDark)
                    if (word.example.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Example:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Muted
                        )
                        Text(
                            "\"${word.example}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = InkDark
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedWord = null }) {
                    Text("Got it", color = BluePrimary)
                }
            }
        )
    }
}
