package com.schemewise.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

private data class BottomNavItem(
    val screen: Screen,
    val label:  String,
    val icon:   ImageVector,
)

private val navItems = listOf(
    BottomNavItem(Screen.Home,      "Home",      Icons.Filled.Home),
    BottomNavItem(Screen.Explore,   "Schemes",   Icons.Filled.Search),
    BottomNavItem(Screen.Simulator, "Simulator", Icons.Filled.FlashOn),
    BottomNavItem(Screen.Bot,       "AI Chat",   Icons.Filled.SmartToy),
    BottomNavItem(Screen.Saved,     "Saved",     Icons.Filled.Bookmark),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate:   (Screen) -> Unit,
) {
    // Elegant, seamless Light Frosted Dock
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 20.dp,
                shape     = RoundedCornerShape(32.dp),
                spotColor = Color(0x33000000),
                ambientColor = Color(0x1F000000)
            )
    ) {
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color.White, // Very crisp light surface
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                navItems.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    ShiftingNavItem(
                        item     = item,
                        selected = selected,
                        onClick  = { onNavigate(item.screen) },
                        modifier = Modifier.weight(if (selected) 1.5f else 1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShiftingNavItem(
    item:     BottomNavItem,
    selected: Boolean,
    onClick:  () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    // Smooth width animation through weight (handled by parent modifier, but we animate internal background)
    val bgColor by animateColorAsState(
        targetValue = if (selected) Brand500.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navBgColor"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Brand600 else MutedLight,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "navIconColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
            .background(bgColor)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = item.icon,
                contentDescription = item.label,
                tint               = iconColor,
                modifier           = Modifier.size(22.dp)
            )
            
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(300)),
                exit = shrinkHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing)
                ) + fadeOut(tween(300))
            ) {
                Text(
                    text       = item.label,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Brand600,
                    modifier   = Modifier.padding(start = 6.dp),
                    maxLines   = 1
                )
            }
        }
    }
}
