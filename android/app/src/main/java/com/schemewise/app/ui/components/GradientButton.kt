package com.schemewise.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon

/**
 * Premium gradient CTA button with press-scale micro-interaction.
 * Replaces plain Material3 buttons in key CTA positions.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Color(0xFFF97316), Color(0xFFEA580C)),
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    radius: Dp = 14.dp,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label        = "btnScale"
    )
    val shadowElev by animateFloatAsState(
        targetValue  = if (isPressed) 2f else 8f,
        animationSpec = tween(100),
        label        = "btnShadow"
    )

    val alpha = if (enabled) 1f else 0.5f

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
            .shadow(shadowElev.dp, RoundedCornerShape(radius), ambientColor = gradientColors.first().copy(0.4f))
            .clip(RoundedCornerShape(radius))
            .background(Brush.linearGradient(gradientColors))
            .height(height)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                enabled           = enabled,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            icon?.let {
                Icon(it, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Text(
                text       = text,
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                letterSpacing = 0.3.sp,
            )
        }
    }
}

/**
 * Outlined ghost button — pairs with GradientButton.
 */
@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(0.35f),
    textColor: Color = Color.White,
    icon: ImageVector? = null,
    height: Dp = 52.dp,
    radius: Dp = 14.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue  = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label        = "ghostScale"
    )

    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(radius))
            .background(Color.White.copy(0.08f))
            .height(height)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .then(
                Modifier.padding(horizontal = 1.dp) // simulate border via inner padding
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            icon?.let { Icon(it, null, tint = textColor, modifier = Modifier.size(18.dp)) }
            Text(text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}
