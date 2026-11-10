package com.mnhs.readventures.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.data.ReadingMaterial
import com.mnhs.readventures.data.SampleData
import com.mnhs.readventures.ui.components.AppTopBar
import com.mnhs.readventures.ui.components.PrimaryButton
import com.mnhs.readventures.ui.components.ReadingMaterialCard
import com.mnhs.readventures.ui.components.SectionHeader
import com.mnhs.readventures.ui.components.greetingName
import com.mnhs.readventures.ui.theme.BluePale
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.Green
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.Orange

/** Editorial dashboard: one current reading, quiet discovery, and concise tools. */
@Composable
fun HomeScreen(
    onOpenLibrary: () -> Unit,
    onOpenReading: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenProgress: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenDictionary: () -> Unit = {},
    onOpenLevels: () -> Unit = {},
    onOpenMyClass: () -> Unit = {},
    onJoinClass: () -> Unit = {},
    onTeacherDashboard: () -> Unit = {}
) {
    val continueMaterial = AppState.continueReadingMaterial() ?: SampleData.materials.first()
    val continueProgress = AppState.readingProgress[continueMaterial.id] ?: 0

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF3F6FB)).verticalScroll(rememberScrollState())
    ) {
        AppTopBar(onProfileClick = onOpenProfile)
        HomeModeSelector(
            isClassMode = AppState.isClassMode.value,
            onSolo = { AppState.isClassMode.value = false },
            onClass = { AppState.isClassMode.value = true }
        )
        if (AppState.isClassMode.value) {
            ClassDashboard(onOpenReading, onOpenMyClass, onJoinClass)
        } else {
            StudentDashboard(
                continueMaterial, continueProgress, onOpenLibrary, onOpenReading,
                onOpenQuiz, onOpenProgress, onOpenCategory, onOpenDictionary, onOpenLevels
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun StudentDashboard(
    continueMaterial: ReadingMaterial,
    continueProgress: Int,
    onOpenLibrary: () -> Unit,
    onOpenReading: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenProgress: () -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenDictionary: () -> Unit,
    onOpenLevels: () -> Unit
) {
    Column(Modifier.padding(top = 18.dp)) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Text("WELCOME BACK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
            Spacer(Modifier.height(5.dp))
            Text("Hi, ${greetingName()}.", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text("Your next good read is waiting. Keep your streak moving.", fontSize = 14.sp, color = Muted, maxLines = 2)
        }
        Spacer(Modifier.height(24.dp))
        ReadingOverview()
        Spacer(Modifier.height(28.dp))

        SectionHeader("Continue your reading", "A small step today builds a stronger reader.", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        FeaturedReading(continueMaterial, continueProgress) { onOpenReading(continueMaterial.id) }

        Spacer(Modifier.height(30.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            SectionHeader("Explore by topic", "Find a story that interests you")
            Spacer(Modifier.weight(1f))
            Text("Library", color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onOpenLibrary))
        }
        Spacer(Modifier.height(12.dp))
        CategoryRail(onOpenCategory)

        Spacer(Modifier.height(30.dp))
        SectionHeader("Your learning space", "Helpful tools for every reading session", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(10.dp))
        LearningLink(Icons.Filled.Spellcheck, BluePrimary, "Dictionary", "Look up words while you read", onOpenDictionary)
        LearningLink(Icons.Filled.CollectionsBookmark, Orange, "Reading levels", "${AppState.currentLevel().name.substringBefore(":")} is available", onOpenLevels)
        LearningLink(Icons.AutoMirrored.Filled.TrendingUp, Green, "My progress", "See your reading and quiz results", onOpenProgress)
        LearningLink(Icons.Filled.Quiz, BluePrimary, "Quick quiz", "Check what you remember from this story") { onOpenQuiz(continueMaterial.id) }
    }
}

@Composable
private fun HomeModeSelector(isClassMode: Boolean, onSolo: () -> Unit, onClass: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ModeOption("Personal", !isClassMode, onSolo, Modifier.weight(1f))
        ModeOption("My class", isClassMode, onClass, Modifier.weight(1f))
    }
}

@Composable
private fun ModeOption(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) InkDark else Color.Transparent)
            .clickable(onClick = onClick).padding(vertical = 10.dp), contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (selected) Color.White else Muted)
    }
}

@Composable
private fun ReadingOverview() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OverviewMetric(
                "${AppState.readCount()}", "Stories", Icons.AutoMirrored.Filled.MenuBook,
                BluePrimary, Modifier.weight(1f)
            )
            OverviewMetric(
                "${AppState.quizzesTaken()}", "Quizzes", Icons.Filled.Quiz,
                Color(0xFF0F766E), Modifier.weight(1f), true
            )
            OverviewMetric(
                "${AppState.averageScorePercent()}", "Average", Icons.AutoMirrored.Filled.TrendingUp,
                Color(0xFFB45309), Modifier.weight(1f), true, showProgress = true
            )
        }
    }
}

@Composable
private fun OverviewMetric(
    value: String,
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier,
    showDivider: Boolean = false,
    showProgress: Boolean = false
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (showDivider) {
            Box(Modifier.width(1.dp).height(48.dp).background(CardBorder))
        }
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (showProgress) {
                val score = value.toIntOrNull() ?: 0
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { score.coerceIn(0, 100) / 100f },
                        modifier = Modifier.size(40.dp),
                        color = iconTint,
                        trackColor = iconTint.copy(alpha = .14f),
                        strokeWidth = 3.dp
                    )
                    Text("$value%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = InkDark)
                }
            } else {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(11.dp))
                        .background(iconTint.copy(alpha = .10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(19.dp))
                }
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Muted)
        }
    }
}

@Composable
private fun FeaturedReading(material: ReadingMaterial, progress: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = InkDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                BookCoverArtwork()
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f).padding(top = 2.dp)) {
                    Text(
                        "CURRENTLY READING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = .62f),
                        letterSpacing = .8.sp
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        material.title,
                        fontSize = 19.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReadingTag(material.topic)
                        ReadingTag(material.level)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("YOUR PROGRESS", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = .65f), letterSpacing = .6.sp)
                Spacer(Modifier.weight(1f))
                Text("$progress% complete", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0, 100) / 100f },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = Color(0xFF60A5FA),
                trackColor = Color.White.copy(alpha = .16f)
            )
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = .10f)).padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Continue reading", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Continue reading", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun BookCoverArtwork() {
    Box(
        Modifier.size(width = 64.dp, height = 88.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF3B82F6))
            .border(1.dp, Color.White.copy(alpha = .25f), RoundedCornerShape(8.dp))
    ) {
        Box(
            Modifier.fillMaxWidth(.32f).height(5.dp)
                .background(Color(0xFFFBBF24))
        )
        Box(
            Modifier.fillMaxWidth().height(1.dp).align(Alignment.Center)
                .background(Color.White.copy(alpha = .55f))
        )
        Icon(
            Icons.AutoMirrored.Filled.MenuBook,
            contentDescription = null,
            tint = Color.White.copy(alpha = .88f),
            modifier = Modifier.size(31.dp).align(Alignment.Center)
        )
        Box(
            Modifier.fillMaxWidth().height(20.dp).align(Alignment.BottomCenter)
                .background(Color(0xFF1D4ED8).copy(alpha = .55f))
        )
    }
}

@Composable
private fun ReadingTag(label: String) {
    Text(
        label,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = .10f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = Color.White.copy(alpha = .86f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CategoryRail(onOpenCategory: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        listOf(
            Triple("Fiction", Icons.AutoMirrored.Filled.MenuBook, BluePrimary),
            Triple("Non-Fiction", Icons.Filled.CollectionsBookmark, Color(0xFF0F766E)),
            Triple("Science", Icons.Filled.Science, Color(0xFFB45309)),
            Triple("Classic", Icons.Filled.AutoStories, Color(0xFF7C3AED))
        ).forEach { (category, icon, tint) ->
            Row(
                Modifier.clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .border(1.dp, CardBorder, RoundedCornerShape(50))
                    .clickable { onOpenCategory(category) }
                    .padding(start = 7.dp, end = 13.dp, top = 7.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    Modifier.size(27.dp).clip(CircleShape).background(tint.copy(alpha = .11f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
                }
                Text(category, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkDark)
            }
        }
    }
}

@Composable
private fun LearningLink(icon: ImageVector, iconTint: Color, title: String, detail: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(iconTint.copy(alpha = .11f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(Modifier.height(2.dp))
                Text(detail, fontSize = 12.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Muted, modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun ClassDashboard(onOpenReading: (String) -> Unit, onOpenMyClass: () -> Unit, onJoinClass: () -> Unit) {
    val joinedClasses = AppState.classes.filter { AppState.enrolledClassCodes.contains(it.classCode) }
    Column(Modifier.padding(top = 22.dp)) {
        SectionHeader("Classroom", "Read what your teacher has assigned.", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(14.dp))
        if (joinedClasses.isEmpty()) {
            Card(Modifier.padding(horizontal = 20.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(20.dp)) {
                    Icon(Icons.Filled.Class, contentDescription = null, tint = BluePrimary)
                    Spacer(Modifier.height(12.dp))
                    Text("Join your classroom", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Spacer(Modifier.height(4.dp))
                    Text("Use your teacher's class code to see assignments and due dates.", fontSize = 13.sp, color = Muted)
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("Join with class code", onJoinClass, Modifier.fillMaxWidth())
                }
            }
        } else {
            joinedClasses.forEach { schoolClass ->
                val assignments = AppState.classAssignments.filter { it.classId == schoolClass.classId }
                Card(Modifier.padding(horizontal = 20.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = BluePale)) {
                    Column(Modifier.padding(18.dp)) {
                        Text(schoolClass.className, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        Text("${schoolClass.subject}  •  ${schoolClass.teacherName}", fontSize = 12.sp, color = Muted)
                        Spacer(Modifier.height(8.dp))
                        Text(schoolClass.classCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Assignments", "${assignments.size} active")
                    Spacer(Modifier.weight(1f))
                    Text("View class", color = BluePrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable(onClick = onOpenMyClass))
                }
                Spacer(Modifier.height(12.dp))
                assignments.forEach { assignment ->
                    AppState.materials.firstOrNull { it.id == assignment.materialId }?.let { material ->
                        ReadingMaterialCard(
                            material, true, AppState.readingProgress[material.id] ?: 0,
                            AppState.bestScores[material.id], { onOpenReading(material.id) },
                            Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}
