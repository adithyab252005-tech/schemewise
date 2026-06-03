package com.schemewise.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

/** Global drawer trigger so we don't need to pass a callback to every screen. */
val LocalDrawerAction = compositionLocalOf<() -> Unit> { {} }

/**
 * Premium glassmorphic top bar.
 * Uses a barely-translucent white tint + subtle bottom shadow for the frosted-glass look.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeWiseTopBar(
    title:           String  = "SchemeWise",
    subtitle:        String? = null,
    navigationIcon:  (@Composable () -> Unit)? = null,
    onProfileClick:  (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    onNotifClick:    (() -> Unit)? = null,
    hasNotif:        Boolean = false,
    actions:         @Composable RowScope.() -> Unit = {},
) {
    val openDrawer = LocalDrawerAction.current

    TopAppBar(
        modifier = Modifier
            .drawBehind {
                // Subtle bottom shadow line
                val shadowColor = Color(0x12000000)
                drawLine(
                    color       = shadowColor,
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            },
        navigationIcon = {
            if (navigationIcon == null) {
                IconButton(onClick = openDrawer) {
                    Icon(
                        imageVector        = Icons.Filled.Menu,
                        contentDescription = "Menu",
                        tint               = Brand500,
                        modifier           = Modifier.size(22.dp),
                    )
                }
            } else {
                navigationIcon()
            }
        },
        title = {
            Column {
                // Gradient text effect — simulated via a Box with gradient background + blendMode
                // Using a simple approach: logo text with orange weight contrast
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Accent dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                Brush.radialGradient(listOf(Brand500, Brand600)),
                                CircleShape
                            )
                    )
                    Text(
                        text       = title,
                        fontWeight = FontWeight.Black,
                        fontSize   = 18.sp,
                        color      = OnSurface,
                        letterSpacing = (-0.3).sp,
                    )
                }
                subtitle?.let {
                    Text(
                        text          = it,
                        fontSize      = 10.sp,
                        color         = Muted,
                        letterSpacing = 1.sp,
                        fontWeight    = FontWeight.SemiBold,
                    )
                }
            }
        },
        actions = {
            actions()
            // Settings icon
            if (onSettingsClick != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Surface3),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector        = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint               = Muted,
                            modifier           = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
            }
            // Profile avatar — gradient circle
            if (onProfileClick != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Brush.linearGradient(listOf(Brand500, Brand700)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onProfileClick, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector        = Icons.Filled.AccountCircle,
                            contentDescription = "Profile",
                            tint               = Color.White,
                            modifier           = Modifier.size(22.dp),
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White.copy(0.97f),
        ),
    )
}
