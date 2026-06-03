package com.schemewise.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.schemewise.app.R

// Outfit font bundled from res/font/ — matches web app font exactly
// Fallback: FontFamily.SansSerif if font files are missing
val OutfitFontFamily = try {
    FontFamily(
        Font(R.font.outfit_regular,   FontWeight.Normal),
        Font(R.font.outfit_medium,    FontWeight.Medium),
        Font(R.font.outfit_semibold,  FontWeight.SemiBold),
        Font(R.font.outfit_bold,      FontWeight.Bold),
        Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
        Font(R.font.outfit_black,     FontWeight.Black),
    )
} catch (e: Exception) {
    FontFamily.SansSerif
}

val SchemeWiseTypography = androidx.compose.material3.Typography(
    displayLarge  = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Black,    fontSize = 36.sp, lineHeight = 42.sp),
    displayMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold,     fontSize = 30.sp, lineHeight = 36.sp),
    headlineLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold,     fontSize = 26.sp, lineHeight = 32.sp),
    headlineMedium= TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold,     fontSize = 22.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleLarge    = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleMedium   = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall    = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.06.sp),
)
