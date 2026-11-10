package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.components.SecondaryButton
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

/**
 * Quiz Result screen: score per activity, a short message, and
 * buttons to retake, go back to the Library, or go Home.
 */
@Composable
fun QuizResultScreen(
    material: ReadingMaterial,
    score: Int,
    total: Int,
    onRetake: () -> Unit,
    onBackToLibrary: () -> Unit,
    onHome: () -> Unit
) {
    val percent = if (total == 0) 0 else (score * 100) / total
    val message = when {
        percent == 100 -> "Perfect score! 🎉"
        percent >= 80 -> "Great job! You understood the story well."
        percent >= 60 -> "Good work! Review the story and try again to improve."
        else -> "Keep practicing! Read the story again and retake the quiz."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Quiz Result", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = InkDark)
        Text(material.title, fontSize = 14.sp, color = Muted)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QUIZ COMPLETE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary, letterSpacing = .8.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { percent.coerceIn(0, 100) / 100f },
                        modifier = Modifier.size(112.dp),
                        color = BluePrimary,
                        trackColor = BluePrimary.copy(alpha = .12f),
                        strokeWidth = 8.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$percent%", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        Text("score", fontSize = 11.sp, color = Muted)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "$score correct  •  ${(total - score).coerceAtLeast(0)} to review",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InkDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(message, fontSize = 15.sp, color = InkDark, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton("Retake Quiz", onRetake, Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton("Back to Library", onBackToLibrary, Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton("Back to Home", onHome, Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your score is saved on this device and shown in My Progress.",
            fontSize = 12.sp,
            color = Muted,
            textAlign = TextAlign.Center
        )
    }
}
