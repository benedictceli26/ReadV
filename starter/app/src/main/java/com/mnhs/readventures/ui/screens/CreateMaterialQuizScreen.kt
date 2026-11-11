package com.mnhs.readventures.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.CloudinaryImageUploadService
import com.mnhs.readventures.data.Question
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.ReadingRepository
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.ScreenBg
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.util.UUID

class QuestionDraft(
    var id: String? = null,
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
    readingRepository: ReadingRepository,
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
    var coverImageUrl by remember { mutableStateOf(existing?.coverImageUrl ?: "") }
    var selectedCoverUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var uploadingCover by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var loadingExistingQuestions by remember {
        mutableStateOf(existing != null)
    }
    var existingQuestionsLoaded by remember {
        mutableStateOf(existing == null)
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedCoverUri = uri
            uploadingCover = true
            uploadMessage = null
            scope.launch {
                when (val result = CloudinaryImageUploadService(context).upload(uri)) {
                    is CloudinaryImageUploadService.Result.Success -> {
                        coverImageUrl = result.secureUrl
                        uploadMessage = "Cover uploaded successfully."
                    }
                    is CloudinaryImageUploadService.Result.Failure -> uploadMessage = result.message
                }
                uploadingCover = false
            }
        }
    }

    val questionsList = remember {
        mutableStateListOf<QuestionDraft>().apply {
            add(QuestionDraft())
        }
    }

    LaunchedEffect(editingMaterialId) {
        if (editingMaterialId != null) {
            if (existing == null || !existing.isTeacherMade || existing.createdBy != teacher.uid) {
                loadingExistingQuestions = false
                existingQuestionsLoaded = true
                errorMessage = "Only materials owned by your Firebase teacher account can be edited."
                return@LaunchedEffect
            }
            loadingExistingQuestions = true
            readingRepository.loadTeacherQuestions(editingMaterialId) { result ->
                loadingExistingQuestions = false
                result.onSuccess { loaded ->
                    questionsList.clear()
                    loaded.forEach { question ->
                        val choices = question.choices.toMutableList()
                        while (choices.size < 4) choices.add("")
                        questionsList.add(
                            QuestionDraft(
                                id = question.id,
                                questionText = question.question,
                                choices = choices,
                                correctIndex = question.correctIndex
                            )
                        )
                    }
                    if (questionsList.isEmpty()) questionsList.add(QuestionDraft())
                    existingQuestionsLoaded = true
                }.onFailure {
                    errorMessage = it.message ?: "Could not load this quiz for editing."
                }
            }
        }
    }

    fun handleSave() {
        if (saving) return
        if (!existingQuestionsLoaded || loadingExistingQuestions) {
            errorMessage = "Wait for the existing quiz to finish loading."
            return
        }
        if (uploadingCover) { errorMessage = "Wait for the cover upload to finish."; return }
        if (teacher.uid.isBlank()) {
            errorMessage = "Sign in with a provisioned Firebase teacher account before saving materials."
            return
        }
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
            if (q.correctIndex !in 0..3) {
                errorMessage = "Select the correct answer for Question ${i + 1}."
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
            createdBy = teacher.uid,
            isTeacherMade = true,
            levelId = levelId,
            coverImageUrl = coverImageUrl,
            illustrationImageUrl = existing?.illustrationImageUrl ?: ""
        )

        val newQuestions = questionsList.mapIndexed { idx, q ->
            Question(
                id = q.id ?: "$materialId-q-${UUID.randomUUID()}",
                materialId = materialId,
                question = q.questionText.trim(),
                choices = q.choices.map { it.trim() },
                correctIndex = q.correctIndex,
                questionOrder = idx,
                createdBy = teacher.uid,
                isTeacherMade = true
            )
        }

        saving = true
        readingRepository.saveTeacherMaterial(newMaterial, newQuestions) { result ->
            saving = false
            result.onSuccess {
                val existingIndex = AppState.materials.indexOfFirst { it.id == newMaterial.id }
                if (existingIndex >= 0) AppState.materials[existingIndex] = newMaterial
                else AppState.materials.add(0, newMaterial)
                AppState.questions.removeAll { it.materialId == newMaterial.id }
                AppState.questions.addAll(newQuestions.map { it.copy(correctIndex = -1) })
                onSaved()
            }.onFailure { errorMessage = it.message ?: "Could not save material to Firestore." }
        }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "Reading Material Details",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = InkDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Book cover (optional)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedCoverUri != null || coverImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = selectedCoverUri ?: coverImageUrl,
                            contentDescription = "Selected book cover",
                            modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedButton(
                        onClick = { coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        enabled = !uploadingCover,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(if (uploadingCover) "Uploading cover…" else "Select book cover") }
                    uploadMessage?.let {
                        Text(it, fontSize = 12.sp, color = if (coverImageUrl.isNotBlank()) Green else Red, modifier = Modifier.padding(top = 6.dp))
                    }
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

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        placeholder = { Text("Fiction / Science") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = level,
                        onValueChange = { level = it },
                        label = { Text("Reading level") },
                        placeholder = { Text("Level 1 (Grade 7)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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

            if (loadingExistingQuestions) {
                Text(
                    "Loading the saved quiz and answer keys…",
                    fontSize = 13.sp,
                    color = Muted,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            questionsList.forEachIndexed { qIndex, qDraft ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
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
                enabled = !saving && existingQuestionsLoaded && !loadingExistingQuestions,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(
                    if (saving) "Saving…" else if (existing != null) "Update Story & Quiz" else "Save Story & Quiz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
