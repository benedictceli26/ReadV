package com.mnhs.readventures.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mnhs.readventures.data.AppState
import com.mnhs.readventures.ui.theme.BlueLight
import com.mnhs.readventures.ui.theme.BluePrimary
import com.mnhs.readventures.ui.theme.InkDark
import com.mnhs.readventures.ui.theme.Muted

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
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                .size(40.dp)
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
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
                tint = BluePrimary
            )
        }
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
        Spacer(modifier = Modifier.weight(1f))
        LogoMark(size = 32)
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
