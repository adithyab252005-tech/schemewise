package com.schemewise.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary             = Brand500,
    onPrimary           = Color.White,
    primaryContainer    = Brand100,
    onPrimaryContainer  = Brand800,
    secondary           = NavyMid,
    onSecondary         = Color.White,
    secondaryContainer  = Brand50,
    onSecondaryContainer= Brand700,
    background          = Background,
    onBackground        = OnSurface,
    surface             = Surface,
    onSurface           = OnSurface,
    surfaceVariant      = Surface2,
    onSurfaceVariant    = Muted,
    outline             = Border,
    error               = ErrorRed,
    onError             = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary             = Brand500,
    onPrimary           = Color.White,
    primaryContainer    = Brand900,
    onPrimaryContainer  = Brand200,
    secondary           = Brand300,
    onSecondary         = NavyDark,
    background          = DarkBg1,
    onBackground        = Color(0xFFF8FAFC),
    surface             = DarkBg2,
    onSurface           = Color(0xFFF8FAFC),
    surfaceVariant      = DarkBg3,
    onSurfaceVariant    = Color(0xFF94A3B8),
    outline             = Color(0xFF1E293B),
    error               = BadgeRedText,
    onError             = Color.White,
)

// Premium shape system
val SchemeWiseShapes = Shapes(
    extraSmall  = RoundedCornerShape(6.dp),
    small       = RoundedCornerShape(10.dp),
    medium      = RoundedCornerShape(16.dp),
    large       = RoundedCornerShape(24.dp),
    extraLarge  = RoundedCornerShape(32.dp),
)

@Composable
fun SchemeWiseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SchemeWiseTypography,
        shapes      = SchemeWiseShapes,
        content     = content
    )
}
