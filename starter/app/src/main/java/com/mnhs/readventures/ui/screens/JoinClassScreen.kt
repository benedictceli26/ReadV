package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.ui.components.SubHeader
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.ScreenBg

@Composable
fun JoinClassScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(ScreenBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SubHeader(title = "Join a Class", onBack = onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Class joining is not connected to Firestore yet.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                textAlign = TextAlign.Center
            )
            Text(
                "Class codes are not validated or saved. Ask your administrator when secure class enrollment becomes available.",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp,
                color = Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}
