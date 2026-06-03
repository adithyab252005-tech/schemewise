package com.schemewise.app.ui.theme

import androidx.compose.ui.graphics.Color

// ── God Level Premium Orange Brand ─────────────────────────────────────────────
val Brand500  = Color(0xFFFF6B00) // Electric Orange
val Brand600  = Color(0xFFE65C00) // Crimson Edge
val Brand700  = Color(0xFFCC5200)
val Brand800  = Color(0xFF993D00)
val Brand900  = Color(0xFF7A3100)
val Brand950  = Color(0xFF3D1900)

val Brand50   = Color(0xFFFFF7ED)
val Brand100  = Color(0xFFFFEEDB)
val Brand200  = Color(0xFFFFDAB3)
val Brand300  = Color(0xFFFFB866)
val Brand400  = Color(0xFFFF8C1A) // Warm Amber-Orange midpoint

// ── Navy / Dark hero backgrounds ─────────────────────────────────────────────
val NavyDark  = Color(0xFF030712) // Near pitch black with blue tint
val NavyMid   = Color(0xFF091221)
val NavyLight = Color(0xFF13233F)
val NavyAccent= Color(0xFF1E355D)

// ── Pure dark mode surfaces ───────────────────────────────────────────────────
val DarkBg1   = Color(0xFF040608)
val DarkBg2   = Color(0xFF090D13)
val DarkBg3   = Color(0xFF101722)

// ── Semantic ─────────────────────────────────────────────────────────────────
val Accent500    = Color(0xFFFF6B00)
val Accent600    = Color(0xFFE65C00)
val Success500   = Color(0xFF10B981) // Emerald looks more premium than standard green
val Success600   = Color(0xFF059669)
val ErrorRed     = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val VioletAccent = Color(0xFF8B5CF6)
val CyanAccent   = Color(0xFF06B6D4)
val PinkAccent   = Color(0xFFF43F5E) // Premium Rose
val EmeraldAccent= Color(0xFF10B981)

// ── Light-mode surfaces ───────────────────────────────────────────────────────
val Background = Color(0xFFFAFAFB) // Very crisp light off-white
val Surface    = Color(0xFFFFFFFF)
val Surface2   = Color(0xFFF4F5F7)
val Surface3   = Color(0xFFE9EBEF)
val OnSurface  = Color(0xFF0F172A)
val Muted      = Color(0xFF64748B)
val MutedLight = Color(0xFF94A3B8)
val Border     = Color(0xFFE2E8F0)
val BorderLight= Color(0xFFF1F5F9)

// ── Glassmorphism overlays ────────────────────────────────────────────────────
val GlassWhite10  = Color(0x1AFFFFFF)   // 10% white
val GlassWhite20  = Color(0x33FFFFFF)   // 20% white
val GlassWhite30  = Color(0x4DFFFFFF)   // 30% white
val GlassBlack5   = Color(0x0D000000)   // 5% black overlay
val GlassBlack10  = Color(0x1A000000)

// ── Gradient pairs (start → end) ─────────────────────────────────────────────
val GradOrangeGold   = listOf(Color(0xFFFFB800), Color(0xFFFF6B00)) // Golden ratio to Electric Orange
val GradElectricSunset = listOf(Color(0xFFFF6B00), Color(0xFFE1005A)) // God-level premium sunset
val GradNavyBlue     = listOf(Color(0xFF030712), Color(0xFF13233F))
val GradViolet       = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
val GradGreen        = listOf(Color(0xFF10B981), Color(0xFF047857))
val GradRose         = listOf(Color(0xFFF43F5E), Color(0xFFBE123C))
val GradAmber        = listOf(Color(0xFFF59E0B), Color(0xFFB45309))
val GradSky          = listOf(Color(0xFF0EA5E9), Color(0xFF0369A1))

// ── Category gradient map ─────────────────────────────────────────────────────
fun categoryGradient(cat: String): List<Color> = when {
    cat.contains("Agriculture", true)  -> listOf(Color(0xFF16A34A), Color(0xFF22C55E))
    cat.contains("Business", true)     -> listOf(Color(0xFFD97706), Color(0xFFF97316))
    cat.contains("Education", true)    -> listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6))
    cat.contains("Health", true)       -> listOf(Color(0xFFDC2626), Color(0xFFEF4444))
    cat.contains("Housing", true)      -> listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
    cat.contains("Women", true)        -> listOf(Color(0xFFBE185D), Color(0xFFEC4899))
    cat.contains("Social", true)       -> listOf(Color(0xFF7C3AED), Color(0xFF5B21B6))
    cat.contains("Skills", true)       -> listOf(Color(0xFFD97706), Color(0xFFF59E0B))
    else                               -> listOf(Color(0xFFF97316), Color(0xFFEA580C))
}

// ── Badge colours ─────────────────────────────────────────────────────────────
val BadgeGreenBg    = Color(0xFFDCFCE7)
val BadgeGreenText  = Color(0xFF15803D)
val BadgeOrangeBg   = Color(0xFFFFF7ED)
val BadgeOrangeText = Color(0xFFC2410C)
val BadgeRedBg      = Color(0xFFFEF2F2)
val BadgeRedText    = Color(0xFFDC2626)
val BadgeBlueBg     = Color(0xFFEFF6FF)
val BadgeBlueText   = Color(0xFF1D4ED8)
val BadgeAmberBg    = Color(0xFFFFFBEB)
val BadgeAmberText  = Color(0xFFD97706)
val BadgeVioletBg   = Color(0xFFF5F3FF)
val BadgeVioletText = Color(0xFF7C3AED)
