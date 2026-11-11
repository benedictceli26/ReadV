package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

@Composable
fun TeacherClassScreen(onManageAssignments: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Class management is not connected to Firestore yet.",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            textAlign = TextAlign.Center
        )
        Text(
            "No class, roster, or assignment data is stored by this app. Secure class collections must be configured before this area can be used.",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 14.sp,
            color = Muted,
            textAlign = TextAlign.Center
        )
        TextButton(
            onClick = onManageAssignments,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Return to teacher dashboard")
        }
    }
}
