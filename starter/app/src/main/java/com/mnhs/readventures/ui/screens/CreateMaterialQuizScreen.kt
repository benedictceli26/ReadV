package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.mnhs.readventures.data.Question
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg

class QuestionDraft(
    var questionText: String = "",
    val choices: MutableList<String> = mutableListOf("", "", "", ""),
    var correctIndex: Int = 0
)

/**
 * FEATURE 4: Teacher Upload / Create Own Quiz + Reading.
 */
@Composable
fun CreateMaterialQuizScreen(
    editingMaterialId: String? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val teacher = AppState.currentUser.value
    val existing = AppState.materials.firstOrNull { it.id == editingMaterialId }

    var title by remember { mutableStateOf(existing?.title ?: "") }
    var topic by remember { mutableStateOf(existing?.topic ?: "Short Story") }
    var category by remember { mutableStateOf(existing?.category ?: "Fiction") }
    var level by remember { mutableStateOf(existing?.level ?: "Level 1 (Grade 7)") }
    var body by remember { mutableStateOf(existing?.body ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val questionsList = remember {
        mutableStateListOf<QuestionDraft>().apply {
            if (existing != null) {
                val found = AppState.questions.filter { it.materialId == existing.id }
                if (found.isNotEmpty()) {
                    for (q in found) {
                        val cList = q.choices.toMutableList()
                        while (cList.size < 4) cList.add("")
                        add(QuestionDraft(q.question, cList, q.correctIndex))
                    }
                } else {
                    add(QuestionDraft())
                }
            } else {
                add(QuestionDraft())
            }
        }
    }

    fun handleSave() {
        if (title.isBlank()) {
            errorMessage = "Please enter a story title."
            return
        }
        if (body.isBlank()) {
            errorMessage = "Please enter the story content / body."
            return
        }
        for (i in questionsList.indices) {
            val q = questionsList[i]
            if (q.questionText.isBlank()) {
                errorMessage = "Question ${i + 1} text cannot be empty."
                return
            }
            if (q.choices.any { it.isBlank() }) {
                errorMessage = "Please fill in all 4 choices for Question ${i + 1}."
                return
            }
        }

        val materialId = existing?.id ?: "teacher-mat-${System.currentTimeMillis()}"
        val levelId = when {
            level.contains("Level 2", ignoreCase = true) -> "level-2"
            level.contains("Level 3", ignoreCase = true) -> "level-3"
            else -> "level-1"
        }

        val newMaterial = ReadingMaterial(
            id = materialId,
            title = title.trim(),
            level = level.trim(),
            topic = topic.trim(),
            category = category.trim(),
            kind = if (category == "Science" || category == "Non-Fiction") "Informational" else "Stories",
            body = body.trim(),
            createdBy = teacher.uid.ifBlank { "teacher-santos" },
            isTeacherMade = true,
            levelId = levelId
        )

        val newQuestions = questionsList.mapIndexed { idx, q ->
            Question(
                id = "$materialId-q${idx + 1}",
                materialId = materialId,
                question = q.questionText.trim(),
                choices = q.choices.map { it.trim() },
                correctIndex = q.correctIndex,
                createdBy = teacher.uid.ifBlank { "teacher-santos" },
                isTeacherMade = true
            )
        }

        AppState.addOrUpdateMaterial(newMaterial, newQuestions)
        onSaved()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(
            title = if (existing != null) "Edit Story & Quiz" else "Create Story & Quiz",
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Story Details Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Reading Material Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Story / Passage Title") },
                        placeholder = { Text("e.g. The Brave Carabao") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            placeholder = { Text("Fiction / Science") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = level,
                            onValueChange = { level = it },
                            label = { Text("Level") },
                            placeholder = { Text("Level 1 (Grade 7)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic / Theme") },
                        placeholder = { Text("e.g. Life Lessons") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = body,
                        onValueChange = { body = it },
                        label = { Text("Story Body / Passage Text") },
                        placeholder = { Text("Write or paste the story paragraphs here...") },
                        minLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quiz Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Comprehension Quiz (${questionsList.size}/5)",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.weight(1f))
                if (questionsList.size < 5) {
                    OutlinedButton(
                        onClick = { questionsList.add(QuestionDraft()) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Question", fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            questionsList.forEachIndexed { qIndex, qDraft ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Question ${qIndex + 1}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (questionsList.size > 1) {
                                IconButton(onClick = { questionsList.removeAt(qIndex) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Remove question",
                                        tint = Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = qDraft.questionText,
                            onValueChange = {
                                qDraft.questionText = it
                                questionsList[qIndex] = qDraft
                            },
                            label = { Text("Question text") },
                            placeholder = { Text("What happened when...?") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Choices (select radio button for correct answer):",
                            fontSize = 12.sp,
                            color = Muted,
                            fontWeight = FontWeight.Medium
                        )

                        val choiceLabels = listOf("A", "B", "C", "D")
                        for (cIndex in 0..3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = qDraft.correctIndex == cIndex,
                                    onClick = {
                                        qDraft.correctIndex = cIndex
                                        questionsList[qIndex] = qDraft
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Green)
                                )
                                OutlinedTextField(
                                    value = qDraft.choices[cIndex],
                                    onValueChange = { newChoice ->
                                        qDraft.choices[cIndex] = newChoice
                                        questionsList[qIndex] = qDraft
                                    },
                                    label = { Text("Option ${choiceLabels[cIndex]}") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(10.dp))
                Text(it, fontSize = 13.sp, color = Red, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = ::handleSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(
                    if (existing != null) "Update Story & Quiz" else "Save Story & Quiz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
