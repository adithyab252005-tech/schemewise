package com.schemewise.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ShimmerColors = listOf(
    Color(0xFFE2E8F0),
    Color(0xFFF1F5F9),
    Color(0xFFFFFFFF),
    Color(0xFFF1F5F9),
    Color(0xFFE2E8F0),
)

/**
 * Premium shimmer placeholder — diagonal sweep animation using infiniteTransition.
 * Replaces the old solid-grey rectangle placeholder.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    radius: Dp = 10.dp,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -600f,
        targetValue  = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerX"
    )

    val brush = Brush.linearGradient(
        colors = ShimmerColors,
        start  = Offset(translateX, translateX * 0.4f),
        end    = Offset(translateX + 600f, translateX * 0.4f + 300f),
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(radius))
            .background(brush)
    )
}

/** Shimmer for a full-width card-shaped placeholder */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp,
    radius: Dp = 16.dp,
) {
    ShimmerBox(modifier = modifier.fillMaxWidth(), height = height, radius = radius)
}

/** Shimmer for a circle (avatar placeholder) */
@Composable
fun ShimmerCircle(size: Dp = 40.dp) {
    ShimmerBox(modifier = Modifier.size(size), radius = size / 2)
}
