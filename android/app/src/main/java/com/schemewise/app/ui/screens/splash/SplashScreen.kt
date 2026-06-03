package com.schemewise.app.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(onNavigateToLogin: () -> Unit) {

    // ── Animation state machines ─────────────────────────────────────────────
    val logoScale  = remember { Animatable(0.4f) }
    val logoAlpha  = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val titleSlide = remember { Animatable(30f) }
    val subAlpha   = remember { Animatable(0f) }
    val tagAlpha   = remember { Animatable(0f) }
    val barAlpha   = remember { Animatable(0f) }

    // Infinite orbit ring rotation
    val infiniteTransition = rememberInfiniteTransition(label = "orbit")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "orbitAngle"
    )
    // Ambient orb pulse
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "orbPulse"
    )

    LaunchedEffect(Unit) {
        // Step 1 — Logo springs in
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
        logoAlpha.animateTo(1f, tween(300))
        delay(100)
        // Step 2 — Title slides up
        titleAlpha.animateTo(1f, tween(400))
        titleSlide.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        delay(80)
        // Step 3 — Subtitle fades
        subAlpha.animateTo(1f, tween(350))
        delay(80)
        // Step 4 — Tagline + bar
        tagAlpha.animateTo(1f, tween(350))
        barAlpha.animateTo(1f, tween(400))
        delay(1400)
        onNavigateToLogin()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060F1E), Color(0xFF0D1F3C), Color(0xFF142F58)))),
        contentAlignment = Alignment.Center,
    ) {

        // ── Ambient background orbs (Canvas) ─────────────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Top-left large orb
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(Brand500.copy(0.15f), Color.Transparent),
                    radius = 300f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.15f)
                ),
                radius = 300f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.1f, size.height * 0.15f),
            )
            // Bottom-right orb
            drawCircle(
                brush  = Brush.radialGradient(
                    colors = listOf(VioletAccent.copy(0.12f * orbScale), Color.Transparent),
                    radius = 260f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.85f)
                ),
                radius = 260f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.9f, size.height * 0.85f),
            )
        }

        // ── Tricolor top accent strip ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .alpha(barAlpha.value)
                .align(Alignment.TopCenter)
        ) {
            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF97316)))
            Box(Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(0.4f)))
            Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF22C55E)))
        }

        // ── Main content column ───────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── Logo with orbit ring ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center,
            ) {
                // Rotating dashed orbit ring (Canvas)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    val radius = size.minDimension / 2 - 4.dp.toPx()

                    rotate(orbitAngle, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
                        // Gradient orbit ring
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Brand500.copy(0f), Brand500.copy(0.8f), Brand500.copy(0f))
                            ),
                            startAngle = 0f,
                            sweepAngle = 300f,
                            useCenter  = false,
                            style      = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                        )
                    }

                    // 3 orbit dots
                    val dotRadius = 4.dp.toPx()
                    listOf(0f, 120f, 240f).forEachIndexed { i, baseAngle ->
                        val angle = Math.toRadians((orbitAngle + baseAngle).toDouble())
                        val x = cx + radius * cos(angle).toFloat()
                        val y = cy + radius * sin(angle).toFloat()
                        drawCircle(
                            color  = Brand500.copy(if (i == 0) 1f else 0.5f),
                            radius = dotRadius,
                            center = androidx.compose.ui.geometry.Offset(x, y),
                        )
                    }
                }

                // Shield icon container
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.linearGradient(listOf(Brand500, Brand700)),
                            RoundedCornerShape(22.dp)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector        = Icons.Filled.Shield,
                        contentDescription = "SchemeWise Logo",
                        tint               = Color.White,
                        modifier           = Modifier.size(44.dp),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Title ──────────────────────────────────────────────────────────
            Text(
                text      = "SchemeWise",
                color     = Color.White,
                fontSize  = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                modifier  = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleSlide.value.dp),
            )

            Spacer(Modifier.height(6.dp))

            // Subtitle
            Text(
                text          = "NIC · GOVERNMENT WELFARE PORTAL",
                color         = Color.White.copy(0.45f),
                fontSize      = 10.sp,
                fontWeight    = FontWeight.Bold,
                letterSpacing = 2.5.sp,
                modifier      = Modifier.alpha(subAlpha.value),
            )

            Spacer(Modifier.height(24.dp))

            // ── Tagline pill ───────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .alpha(tagAlpha.value)
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color.White.copy(0.07f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Orange pulsing dot
                    val dotAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
                        label = "dotPulse"
                    )
                    Box(
                        Modifier.size(6.dp).background(Brand500.copy(dotAlpha), CircleShape)
                    )
                    Text(
                        "Connecting citizens with government welfare",
                        color      = Color.White.copy(0.75f),
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // ── Loading bar at the bottom ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(2.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(0.1f))
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(barAlpha.value)
        ) {
            val loadProgress by infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
                label = "loadBar"
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(loadProgress)
                    .background(Brush.horizontalGradient(GradOrangeGold))
            )
        }
    }
}
