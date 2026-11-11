package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.Question
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.QuizSubmissionResult
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.GreenLight
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.RedLight
import com.mnhs.readventures.ui.theme.ScreenBg
import java.util.UUID

/**
 * Bundled practice quizzes keep local feedback. Firebase quizzes collect answers without
 * revealing correctness and submit them to the trusted grading function.
 */
@Composable
fun QuizScreen(
    material: ReadingMaterial,
    questions: List<Question>,
    serverGraded: Boolean,
    onBack: () -> Unit,
    onFinished: (materialId: String, score: Int, total: Int) -> Unit,
    onServerFinished: (QuizSubmissionResult) -> Unit,
    onSubmitAnswers: (
        answers: Map<String, Int>,
        attemptId: String,
        onResult: (Result<QuizSubmissionResult>) -> Unit
    ) -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    val selectedAnswers = remember(material.id) { mutableStateMapOf<String, Int>() }
    val attemptId = remember(material.id) { UUID.randomUUID().toString() }
    var submitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    if (questions.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
            SubHeader(title = "Quiz", onBack = onBack)
            Text("No quiz questions for this story yet.", modifier = Modifier.padding(20.dp))
        }
        return
    }

    val question = questions[index]
    val isCorrect = !serverGraded && checked && selected == question.correctIndex
    val currentQuestionId = question.id
    val answeredCount = selectedAnswers.size +
        if (serverGraded && selected >= 0 && currentQuestionId !in selectedAnswers) 1 else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Quiz — ${material.title}", onBack = { if (!submitting) onBack() })
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text("COMPREHENSION CHECK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary, letterSpacing = .7.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Question ${index + 1} of ${questions.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (index + 1).toFloat() / questions.size },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = BluePrimary,
                trackColor = BlueLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (serverGraded) "Answered: $answeredCount / ${questions.size}"
                else "Score so far: $score / ${questions.size}",
                fontSize = 13.sp,
                color = Muted
            )
            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(question.question, fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                    Spacer(modifier = Modifier.height(18.dp))
                    for ((choiceIndex, choice) in question.choices.withIndex()) {
                        val isSelected = selected == choiceIndex
                        val bg = when {
                            !serverGraded && checked && choiceIndex == question.correctIndex -> GreenLight
                            !serverGraded && checked && isSelected -> RedLight
                            isSelected -> BlueLight
                            else -> Color.White
                        }
                        val border = when {
                            !serverGraded && checked && choiceIndex == question.correctIndex -> Green
                            !serverGraded && checked && isSelected -> Red
                            isSelected -> BluePrimary
                            else -> com.mnhs.readventures.ui.theme.CardBorder
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .border(1.5.dp, border, RoundedCornerShape(14.dp))
                                .clickable(enabled = !checked && !submitting) { selected = choiceIndex },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = bg)
                        ) {
                            Text(
                                "${'A' + choiceIndex}.  $choice",
                                fontSize = 15.sp,
                                lineHeight = 21.sp,
                                color = InkDark,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 16.dp)
                            )
                        }
                    }
                }
            }

            if (checked && !serverGraded) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) GreenLight else RedLight
                    )
                ) {
                    Text(
                        if (isCorrect) "✓ Correct! Well done."
                        else "✕ Not quite. The correct answer is: ${question.choices[question.correctIndex]}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = InkDark,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            submissionError?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = RedLight)
                ) {
                    Text(
                        it,
                        fontSize = 14.sp,
                        color = InkDark,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (serverGraded) {
                val isLastQuestion = index + 1 == questions.size
                Button(
                    onClick = {
                        if (selected >= 0 && !submitting) {
                            submissionError = null
                            if (!isLastQuestion) {
                                selectedAnswers[currentQuestionId] = selected
                                index++
                                selected = selectedAnswers[questions[index].id] ?: -1
                            } else {
                                val answers = selectedAnswers.toMutableMap().apply {
                                    put(currentQuestionId, selected)
                                }
                                if (questions.any { it.id.isBlank() } || answers.size != questions.size) {
                                    submissionError = "This quiz is missing question data. Please reload it and try again."
                                } else {
                                    submitting = true
                                    onSubmitAnswers(answers, attemptId) { result ->
                                        submitting = false
                                        result.onSuccess { submission ->
                                            onServerFinished(submission)
                                        }.onFailure { error ->
                                            submissionError = error.localizedMessage
                                                ?: "Could not submit the quiz. Check your connection and try again."
                                        }
                                    }
                                }
                            }
                        }
                    },
                    enabled = selected >= 0 && !submitting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (isLastQuestion) "Submit quiz" else "Next question")
                    }
                }
            } else if (!checked) {
                PrimaryButton(
                    text = "Check answer",
                    onClick = {
                        if (selected >= 0) {
                            checked = true
                            if (selected == question.correctIndex) score++
                        }
                    },
                    enabled = selected >= 0,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PrimaryButton(
                    text = if (index + 1 < questions.size) "Next question" else "See my result",
                    onClick = {
                        if (index + 1 < questions.size) {
                            index++
                            selected = -1
                            checked = false
                        } else {
                            onFinished(material.id, score, questions.size)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
