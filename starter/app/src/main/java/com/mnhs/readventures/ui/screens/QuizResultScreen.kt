package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mnhs.readventures.ui.theme.BluePrimary
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
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Quiz Result", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = InkDark)
        Text(material.title, fontSize = 14.sp, color = Muted)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Your Score", fontSize = 14.sp, color = Muted)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$score / $total",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Text("$percent%", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(modifier = Modifier.height(12.dp))
                Text(message, fontSize = 15.sp, color = InkDark, textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetake,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("Retake Quiz", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBackToLibrary,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Back to Library", color = BluePrimary, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Back to Home", color = BluePrimary, fontSize = 15.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your score is saved on this device and shown in My Progress.",
            fontSize = 12.sp,
            color = Muted,
            textAlign = TextAlign.Center
        )
    }
}
