package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.DictionaryWord
import com.mnhs.readventures.data.ProgressStore
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.components.SecondaryButton
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg
import coil.compose.AsyncImage
import java.util.regex.Pattern

/**
 * Reading screen: real-book experience with serif typography, night mode,
 * adjustable font size, and soft vocabulary highlights (not web-link underlines).
 *
 * Difficult words are marked with a soft background tint — tap to view definition.
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
    var isNightMode by remember { mutableStateOf(false) }
    var fontSizeIndex by remember { mutableIntStateOf(1) }

    val fontSizeOptions = listOf(15, 17, 19)
    val bodyFontSize = fontSizeOptions[fontSizeIndex]
    val bodyLineHeight = bodyFontSize + 12

    // Computed reading time
    val wordCount = material.body.split(" ").filter { it.isNotBlank() }.size
    val readMinutes = (wordCount / 200).coerceAtLeast(1)

    // Night mode colors
    val bgColor = if (isNightMode) Color(0xFF1A1A2E) else ScreenBg
    val surfaceColor = if (isNightMode) Color(0xFF16213E) else Color.White
    val titleColor = if (isNightMode) Color.White else InkDark
    val subtitleColor = if (isNightMode) Color.White.copy(alpha = 0.60f) else Muted
    val bodyColor = if (isNightMode) Color(0xFFE2E8F0) else InkDark

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
            .background(bgColor)
    ) {
        // ── Top control bar ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isNightMode) Color(0xFF0F0F1A) else Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BlueLight)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Font size toggle
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BlueLight)
                    .clickable {
                        fontSizeIndex = (fontSizeIndex + 1) % fontSizeOptions.size
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Night mode toggle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isNightMode) Color(0xFF334155) else BlueLight)
                    .clickable { isNightMode = !isNightMode },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isNightMode) Icons.Filled.WbSunny else Icons.Filled.Bedtime,
                    contentDescription = if (isNightMode) "Day mode" else "Night mode",
                    tint = if (isNightMode) Color(0xFFFDE047) else BluePrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // ── Scrollable content ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title — serif, centered
            Text(
                material.title,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = titleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Category / Level / Topic line
            Text(
                "${material.topic}  •  ${material.level}  •  ${material.category}",
                fontSize = 12.sp,
                color = subtitleColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Reading time estimate
            Text(
                "~$readMinutes min read",
                fontSize = 11.sp,
                color = subtitleColor.copy(alpha = 0.70f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (material.illustrationImageUrl.isNotBlank()) {
                AsyncImage(
                    model = material.illustrationImageUrl,
                    contentDescription = "Illustration for ${material.title}",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Thin divider under header
            HorizontalDivider(
                thickness = 1.dp,
                color = CardBorder.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { slider / 100f },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)),
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

            // Glossary hint — subtle, not a card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BlueLight.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    "Tap a highlighted word for its definition.",
                    fontSize = 12.sp,
                    color = BluePrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Story body
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isNightMode) 0.dp else 1.dp)
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
                                    // Soft highlight — no underline, just a tinted background
                                    addStyle(
                                        style = SpanStyle(
                                            color = Color(0xFF1D4ED8),
                                            fontWeight = FontWeight.SemiBold,
                                            background = Color(0xFF2563EB).copy(alpha = 0.08f)
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
                                fontSize = bodyFontSize.sp,
                                color = bodyColor,
                                lineHeight = bodyLineHeight.sp,
                                fontFamily = FontFamily.Serif
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
            Spacer(modifier = Modifier.height(20.dp))

            // Save-and-continue slider
            Text(
                "Where are you in the story?",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Slider(
                value = slider,
                onValueChange = { slider = it },
                valueRange = 0f..100f,
                steps = 9
            )
            Text(
                "Move the slider as you read, then save — the app remembers your place.",
                fontSize = 12.sp,
                color = subtitleColor
            )
            savedMessage?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it, fontSize = 13.sp, color = Green)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Bottom action bar ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .background(if (isNightMode) Color(0xFF0F0F1A) else Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
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

    // Vocabulary definition popup
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
