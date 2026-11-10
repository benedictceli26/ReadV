package com.mnhs.readventures.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted
import com.mnhs.readventures.ui.theme.CardBorder
import com.mnhs.readventures.ui.theme.ScreenBg
import com.mnhs.readventures.ui.theme.Green

/**
 * Shared app chrome, matching the approved Home / Profile design:
 * ReadVentures header (logo + tagline + profile icon) and a bottom nav
 * with the icon on top and the label below it.
 */

/** Logo mark: blue rounded square with a white book. */
@Composable
fun LogoMark(size: Int = 40) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BluePrimary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.MenuBook,
            contentDescription = "ReadVentures logo",
            tint = Color.White,
            modifier = Modifier.size((size * 0.6).dp)
        )
    }
}

/** Main header used on Home, Library, Progress and Profile. */
@Composable
fun AppTopBar(
    title: String = "ReadVentures",
    subtitle: String = "Read. Understand. Explore.",
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LogoMark()
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Text(subtitle, fontSize = 11.sp, color = Muted)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(BlueLight)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile",
                tint = InkDark,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/** Sub-screen header: white card with a blue back button, title, mini logo. */
@Composable
fun SubHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
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
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = InkDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        LogoMark(size = 32)
    }
}

/** Shared controls keep forms and calls-to-action visually consistent. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary),
        border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { icon ->
            { Icon(icon, contentDescription = null) }
        },
        singleLine = singleLine,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = BluePrimary,
            unfocusedBorderColor = CardBorder,
            focusedLabelColor = BluePrimary
        )
    )
}

@Composable
fun SectionHeader(title: String, detail: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
        detail?.let { Text(it, fontSize = 12.sp, color = Muted) }
    }
}

@Composable
fun ProgressCard(
    title: String,
    detail: String,
    progress: Float,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = InkDark)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = BluePrimary,
                trackColor = BlueLight
            )
            Spacer(Modifier.height(6.dp))
            Text(detail, fontSize = 12.sp, color = Muted)
        }
    }
}

/** Consistent material surface for Library, recommendations, and assignments. */
@Composable
fun ReadingMaterialCard(
    material: ReadingMaterial,
    isUnlocked: Boolean,
    progress: Int,
    bestScore: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isUnlocked) CardBorder else Color(0xFFE2E8F0), shape)
            .clickable(onClick = onClick),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color.White else ScreenBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 1.dp else 0.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isUnlocked) BlueLight else CardBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Filled.MenuBook else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) BluePrimary else Muted
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        material.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isUnlocked) InkDark else Muted,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isUnlocked) {
                        Text("LOCKED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted)
                    }
                }
                Text("${material.topic}  •  ${material.level}", fontSize = 12.sp, color = Muted)
                if (isUnlocked) {
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = BluePrimary,
                        trackColor = BlueLight
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        if (bestScore == null) "$progress% read  •  Quiz not taken" else "$progress% read  •  Best quiz: $bestScore/5",
                        fontSize = 11.sp,
                        color = if (bestScore != null) Green else Muted
                    )
                } else {
                    Spacer(Modifier.height(6.dp))
                    Text("Tap to view unlock requirements", fontSize = 11.sp, color = Muted)
                }
            }
        }
    }
}

@Composable
fun EmptyState(title: String, detail: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Spacer(Modifier.height(4.dp))
            Text(detail, fontSize = 13.sp, lineHeight = 19.sp, color = Muted)
        }
    }
}

data class BottomNavItem(val route: String, val label: String, val icon: ImageVector)

val BottomNavItems = listOf(
    BottomNavItem("home", "Home", Icons.Filled.Home),
    BottomNavItem("library", "Library", Icons.Filled.Book),
    BottomNavItem("progress", "Results", Icons.Filled.TrendingUp),
    BottomNavItem("profile", "Profile", Icons.Filled.Person)
)

@Composable
fun ReadVenturesBottomNav(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        for (item in BottomNavItems) {
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { if (!selected) onNavigate(item.route) },
                icon = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (selected) BluePrimary else Muted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            item.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (selected) BluePrimary else Muted
                        )
                    }
                },
                label = null,
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BlueLight,
                    selectedIconColor = BluePrimary,
                    unselectedIconColor = Muted
                )
            )
        }
    }
}

/** Small helper: first name of the signed-in student, for "Hello, Juan!". */
fun greetingName(): String {
    val full = AppState.currentUser.value.name.ifBlank { "Student" }
    return full.split(" ").firstOrNull()?.ifBlank { "Student" } ?: "Student"
}
