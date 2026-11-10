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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.Question
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.GreenLight
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Red
import com.mnhs.readventures.ui.theme.RedLight
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Quiz screen: one question at a time, auto-check with instant feedback.
 * The student picks an answer, taps "Check Answer", sees immediately
 * whether it is correct (and the right answer when wrong), then moves on.
 * The score is tallied and passed to the Quiz Result screen.
 */
@Composable
fun QuizScreen(
    material: ReadingMaterial,
    questions: List<Question>,
    onBack: () -> Unit,
    onFinished: (materialId: String, score: Int, total: Int) -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var checked by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    if (questions.isEmpty()) {
        Column(modifier = Modifier.fillMaxSize().background(ScreenBg)) {
            SubHeader(title = "Quiz", onBack = onBack)
            Text("No quiz questions for this story yet.", modifier = Modifier.padding(20.dp))
        }
        return
    }

    val question = questions[index]
    val isCorrect = checked && selected == question.correctIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        SubHeader(title = "Quiz — ${material.title}", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                "Question ${index + 1} of ${questions.size}",
                fontSize = 13.sp,
                color = Muted
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (index + 1).toFloat() / questions.size },
                modifier = Modifier.fillMaxWidth(),
                color = BluePrimary,
                trackColor = BlueLight
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Score so far: $score / ${questions.size}", fontSize = 13.sp, color = Muted)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(question.question, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                    Spacer(modifier = Modifier.height(16.dp))
                    for ((choiceIndex, choice) in question.choices.withIndex()) {
                        val isSelected = selected == choiceIndex
                        val bg = when {
                            checked && choiceIndex == question.correctIndex -> GreenLight
                            checked && isSelected -> RedLight
                            isSelected -> BlueLight
                            else -> Color.White
                        }
                        val border = when {
                            checked && choiceIndex == question.correctIndex -> Green
                            checked && isSelected -> Red
                            isSelected -> BluePrimary
                            else -> com.mnhs.readventures.ui.theme.CardBorder
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.5.dp, border, RoundedCornerShape(12.dp))
                                .clickable(enabled = !checked) { selected = choiceIndex },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = bg)
                        ) {
                            Text(
                                "${'A' + choiceIndex}.  $choice",
                                fontSize = 15.sp,
                                color = InkDark,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                }
            }

            if (checked) {
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            if (!checked) {
                Button(
                    onClick = {
                        if (selected >= 0) {
                            checked = true
                            if (selected == question.correctIndex) score++
                        }
                    },
                    enabled = selected >= 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text("Check Answer", fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = {
                        if (index + 1 < questions.size) {
                            index++
                            selected = -1
                            checked = false
                        } else {
                            onFinished(material.id, score, questions.size)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                ) {
                    Text(
                        if (index + 1 < questions.size) "Next Question" else "See My Result",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
