package com.schemewise.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

/**
 * Status badge — mirrors the web StatusBadge.jsx component.
 * Eligible = green · Partial = orange · Ineligible = red
 */
@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bg, textColor) = when (status.lowercase()) {
        "eligible"   -> BadgeGreenBg   to BadgeGreenText
        "partial"    -> BadgeOrangeBg  to BadgeOrangeText
        "ineligible" -> BadgeRedBg     to BadgeRedText
        else         -> BadgeBlueBg    to BadgeBlueText
    }
    Surface(
        shape    = RoundedCornerShape(99.dp),
        color    = bg,
        modifier = modifier,
    ) {
        Text(
            text     = status.uppercase(),
            color    = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.06.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
