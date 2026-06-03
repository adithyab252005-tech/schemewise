package com.schemewise.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.Profile
import com.schemewise.app.ui.components.*
import com.schemewise.app.ui.theme.*

@Composable
fun HomeScreen(
    onSchemeClick:     (String) -> Unit,
    onExploreClick:    () -> Unit,
    onSavedClick:      () -> Unit,
    onUpdatesClick:    () -> Unit,
    onSimulatorClick:  () -> Unit,
    onRecheckClick:    () -> Unit,
    onSettingsClick:   () -> Unit = {},
    onProfileClick:    () -> Unit = {},
    onCentersMapClick: () -> Unit = {},
    onCompareClick:    () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val firstName = viewModel.userName.split(" ").firstOrNull() ?: "Citizen"

    // Animated eligibility ring score
    val animatedScore by animateFloatAsState(
        targetValue   = if (state.isLoading) 0f else state.civicScore.toFloat(),
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label         = "score"
    )

    // Entry visibility trigger
    var contentVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { contentVisible = true }

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title           = "SchemeWise",
                subtitle        = "NIC PORTAL",
                onSettingsClick = onSettingsClick,
                onProfileClick  = onProfileClick,
            )
        },
        containerColor = Background,
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.padding(padding).fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Hero Bento Card ──────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible  = contentVisible,
                    enter    = fadeIn(tween(400)) + slideInVertically(tween(400)) { -20 },
                ) {
                    HeroBentoCard(
                        firstName      = firstName,
                        eligibleCount  = state.eligibleCount,
                        isLoading      = state.isLoading,
                        onCompareClick = onCompareClick,
                    )
                }
            }

            // ── Error Banner ─────────────────────────────────────────────────
            if (state.error != null) {
                item {
                    AnimatedVisibility(visible = true, enter = fadeIn()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BadgeRedBg),
                            shape  = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.ErrorOutline, "Error", tint = BadgeRedText, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(state.error!!, color = BadgeRedText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // ── Next Best Action (AI Engine) ──────────────────────────────────
            item {
                StaggeredEntry(index = 1, visible = contentVisible) {
                    NextBestActionCard(
                        score             = animatedScore.toInt(),
                        profileCompletion = state.profileCompletion,
                        isLoading         = state.isLoading,
                        onRecheckClick    = onRecheckClick,
                        onSimulatorClick  = onSimulatorClick,
                    )
                }
            }

            // ── Stats Row ─────────────────────────────────────────────────────
            item {
                StaggeredEntry(index = 2, visible = contentVisible) {
                    StatsRow(
                        eligibleCount = state.eligibleCount,
                        savedCount    = state.savedSchemes.size,
                        isLoading     = state.isLoading,
                        onEligibleClick = onExploreClick,
                        onSavedClick    = onSavedClick,
                        onUpdatesClick  = onUpdatesClick,
                    )
                }
            }

            // ── Verified High-Yield Matches ───────────────────────────────────
            item {
                StaggeredEntry(index = 3, visible = contentVisible) {
                    VerifiedMatchesCard(
                        picks       = state.topPicks,
                        isLoading   = state.isLoading,
                        onSchemeClick  = onSchemeClick,
                        onViewAllClick = onExploreClick,
                        onRecheckClick = onRecheckClick,
                    )
                }
            }

            // ── Physical Hub Routing ─────────────────────────────────────────
            item {
                StaggeredEntry(index = 4, visible = contentVisible) {
                    HubRoutingCard(onClick = onCentersMapClick)
                }
            }

            // ── Profile Snapshot ──────────────────────────────────────────────
            if (state.profile != null) {
                item {
                    StaggeredEntry(index = 6, visible = contentVisible) {
                        ProfileSnapshotCard(profile = state.profile!!)
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

// ── Staggered entrance helper ─────────────────────────────────────────────────
@Composable
private fun StaggeredEntry(
    index:   Int,
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(visible) {
        if (visible) {
            kotlinx.coroutines.delay((index * 80).toLong())
            show = true
        }
    }
    AnimatedVisibility(
        visible = show,
        enter   = fadeIn(tween(350)) + slideInVertically(tween(350, easing = FastOutSlowInEasing)) { 24 },
    ) {
        content()
    }
}

// ── Hero Bento Card ───────────────────────────────────────────────────────────
@Composable
private fun HeroBentoCard(
    firstName:       String,
    eligibleCount:   Int,
    isLoading:       Boolean,
    onCompareClick:  () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hero")
    val orbOffset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "orb"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "pulse"
    )

    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(28.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(GradNavyBlue),
                    RoundedCornerShape(28.dp)
                )
        ) {
            // Floating ambient orbs
            Canvas(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                drawCircle(
                    brush  = Brush.radialGradient(
                        listOf(Brand500.copy(0.25f), Color.Transparent),
                        radius = 180f,
                        center = Offset(size.width * 0.85f, size.height * 0.2f + orbOffset * 20f)
                    ),
                    radius = 180f,
                    center = Offset(size.width * 0.85f, size.height * 0.2f + orbOffset * 20f),
                )
                drawCircle(
                    brush  = Brush.radialGradient(
                        listOf(VioletAccent.copy(0.15f), Color.Transparent),
                        radius = 120f,
                        center = Offset(size.width * 0.1f, size.height * 0.75f - orbOffset * 15f)
                    ),
                    radius = 120f,
                    center = Offset(size.width * 0.1f, size.height * 0.75f - orbOffset * 15f),
                )
            }

            Column(modifier = Modifier.padding(22.dp)) {
                // God-Level Electric Sunset top strip
                Row(modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(1.dp))) {
                    Box(Modifier.fillMaxWidth().fillMaxHeight().background(Brush.horizontalGradient(GradElectricSunset)))
                }
                Spacer(Modifier.height(16.dp))

                // Live badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Brand500.copy(0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(6.dp).background(Brand500.copy(pulseAlpha), CircleShape))
                    Text("Intelligence Core · Live", color = Brand300, fontSize = 9.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    "Welcome back,\n$firstName.",
                    color      = Color.White,
                    fontSize   = 28.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 34.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "The engine has verified ${if (isLoading) "—" else eligibleCount} qualifying schemes matching your profile.",
                    color      = Color.White.copy(0.6f),
                    fontSize   = 13.sp,
                    lineHeight = 18.sp,
                )
                Spacer(Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GhostButton(
                        text     = "Compare",
                        onClick  = onCompareClick,
                        icon     = Icons.Filled.CompareArrows,
                        modifier = Modifier.weight(1f),
                        height   = 46.dp,
                        radius   = 13.dp,
                    )
                }
            }
        }
    }
}

// ── Next Best Action (AI Engine) ─────────────────────────────────────────────
@Composable
private fun NextBestActionCard(
    score:             Int,
    profileCompletion: Int,
    isLoading:         Boolean,
    onRecheckClick:    () -> Unit,
    onSimulatorClick:  () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onSimulatorClick() },
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.AutoFixHigh, null, tint = Brand500, modifier = Modifier.size(14.dp))
                    Text(
                        "AI ANALYSIS STATUS",
                        fontSize = 9.sp, fontWeight = FontWeight.Black, color = Brand600,
                        letterSpacing = 1.2.sp,
                    )
                }
                Spacer(Modifier.height(6.dp))
                if (isLoading) {
                    ShimmerBox(modifier = Modifier.width(100.dp), height = 18.dp)
                } else {
                    Text(
                        "Action Required: Run Simulator",
                        fontSize = 14.sp, fontWeight = FontWeight.Black, color = OnSurface, lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Simulate parameter changes to unlock ${score}% more schemes.",
                    fontSize = 11.sp, color = Muted, lineHeight = 15.sp,
                )
                Spacer(Modifier.height(14.dp))

                // Profile quality badge (compact)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BadgeGreenBg,
                ) {
                    Text(
                        "✓ Profile Verified",
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = BadgeGreenText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // Premium Animated Hexagon or Pulse (reusing ring structure for now but styled)
            PremiumScoreRing(score = score, isLoading = isLoading)
        }
    }
}

// ── Premium Score Ring ────────────────────────────────────────────────────────
@Composable
private fun PremiumScoreRing(score: Int, isLoading: Boolean) {
    Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
        val sweepAngle = (score / 100f) * 360f
        val ringColor = when {
            score >= 70 -> Success500
            score >= 40 -> WarningAmber
            else        -> Brand500
        }

        Canvas(modifier = Modifier.size(110.dp)) {
            val strokeW = 11.dp.toPx()
            val radius  = (size.minDimension - strokeW) / 2

            // Outer glow (soft shadow approximation)
            drawArc(
                color      = ringColor.copy(0.1f),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter  = false,
                style      = Stroke(strokeW + 8.dp.toPx(), cap = StrokeCap.Round),
            )
            // Track
            drawArc(
                color      = Border,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(strokeW, cap = StrokeCap.Round),
            )
            // Gradient fill
            if (!isLoading) {
                drawArc(
                    brush      = Brush.sweepGradient(GradElectricSunset + listOf(ringColor)),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter  = false,
                    style      = Stroke(strokeW, cap = StrokeCap.Round),
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isLoading) {
                ShimmerBox(modifier = Modifier.width(40.dp), height = 24.dp)
            } else {
                Text("$score", fontSize = 26.sp, fontWeight = FontWeight.Black, color = OnSurface)
                Text("%", fontSize = 12.sp, color = Brand500, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                when { score >= 70 -> "Excellent"; score >= 40 -> "Good"; else -> "Building" },
                fontSize = 9.sp, color = Muted, letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────
@Composable
private fun StatsRow(
    eligibleCount:   Int,
    savedCount:      Int,
    isLoading:       Boolean,
    onEligibleClick: () -> Unit,
    onSavedClick:    () -> Unit,
    onUpdatesClick:  () -> Unit,
) {
    val stats = listOf(
        StatItem("Eligible",  eligibleCount.toString(),  Icons.Filled.CheckCircle,   Success500,   Color(0xFFDCFCE7), onEligibleClick),
        StatItem("Saved",     savedCount.toString(),      Icons.Filled.Bookmark,      VioletAccent, Color(0xFFF5F3FF), onSavedClick),
        StatItem("Updates",   "3",                        Icons.Filled.Notifications, WarningAmber, Color(0xFFFFFBEB), onUpdatesClick),
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        stats.forEach { stat ->
            StatCard(stat = stat, isLoading = isLoading, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(stat: StatItem, isLoading: Boolean, modifier: Modifier = Modifier) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed) 0.95f else 1f,
        spring(Spring.DampingRatioMediumBouncy),
        label = "statScale"
    )

    Card(
        modifier  = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null) { stat.onClick() },
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(stat.bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(stat.icon, null, tint = stat.iconColor, modifier = Modifier.size(18.dp))
            }
            if (isLoading) ShimmerBox(modifier = Modifier.width(32.dp), height = 22.dp)
            else Text(stat.value, fontWeight = FontWeight.Black, fontSize = 24.sp, color = OnSurface)
            Text(stat.label, fontSize = 10.sp, color = Muted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}

// ── Verified High-Yield Matches (AI Feed) ───────────────────────────────────
@Composable
private fun VerifiedMatchesCard(
    picks:         List<com.schemewise.app.data.model.EligibilityResult>,
    isLoading:     Boolean,
    onSchemeClick:  (String) -> Unit,
    onViewAllClick: () -> Unit,
    onRecheckClick: () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column {
            // Section header with animated gradient shimmer border at top
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(Brush.horizontalGradient(GradElectricSunset))
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.DynamicFeed, null, tint = Brand500, modifier = Modifier.size(16.dp))
                    Text("VERIFIED HIGH-YIELD MATCHES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Muted, letterSpacing = 1.sp)
                }
                TextButton(onClick = onViewAllClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Open Feed →", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Brand600)
                }
            }
            HorizontalDivider(color = Border)

            if (isLoading) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(3) { ShimmerCard(height = 60.dp, radius = 12.dp) }
                }
            } else if (picks.isNotEmpty()) {
                picks.take(4).forEachIndexed { i, pick ->
                    RecommendationRow(
                        pick         = pick,
                        onSchemeClick = { onSchemeClick(pick.schemeId.toString()) }
                    )
                    if (i < picks.size.coerceAtMost(4) - 1) {
                        HorizontalDivider(color = Border.copy(0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
            } else {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, null, tint = Border, modifier = Modifier.size(40.dp))
                    Text("No recommendations yet", fontWeight = FontWeight.Bold, color = Muted)
                    Text("Complete your profile to get matched.", fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    GradientButton("Complete Profile", onRecheckClick, modifier = Modifier.fillMaxWidth(), height = 44.dp, radius = 12.dp)
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(
    pick: com.schemewise.app.data.model.EligibilityResult,
    onSchemeClick: () -> Unit,
) {
    val isEligible = pick.status == "Eligible"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSchemeClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Color accent bar
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(42.dp)
                .background(
                    Brush.verticalGradient(if (isEligible) GradGreen else GradAmber),
                    RoundedCornerShape(2.dp)
                )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                pick.schemeName,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                color      = OnSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                val (bg, fg) = if (isEligible) BadgeGreenBg to BadgeGreenText else BadgeAmberBg to BadgeAmberText
                Surface(shape = RoundedCornerShape(5.dp), color = bg) {
                    Text(pick.status, color = fg, fontSize = 8.sp, fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), letterSpacing = 0.5.sp)
                }
                // Mini score bar
                Box(
                    modifier = Modifier.width(50.dp).height(3.dp).clip(CircleShape).background(Border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(pick.scorePercentage / 100f)
                            .clip(CircleShape)
                            .background(if (isEligible) Success500 else WarningAmber)
                    )
                }
                Text("${pick.scorePercentage}%", fontSize = 10.sp, color = Muted, fontWeight = FontWeight.SemiBold)
            }
        }
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(Surface3),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.ChevronRight, null, tint = MutedLight, modifier = Modifier.size(16.dp))
        }
    }
}

// ── Physical Hub Routing ──────────────────────────────────────────────────────
@Composable
private fun HubRoutingCard(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner")
    val orbX by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Reverse),
        label = "bannerOrb"
    )

    Card(
        modifier  = Modifier.fillMaxWidth().height(130.dp).padding(horizontal = 16.dp).clickable { onClick() },
        shape     = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(NavyLight, NavyAccent, NavyMid)))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(listOf(Brand500.copy(0.3f), Color.Transparent), radius = 300f,
                        center = Offset(size.width * (0.8f + orbX * 0.1f), size.height * 0.2f)),
                    radius = 300f,
                    center = Offset(size.width * (0.8f + orbX * 0.1f), size.height * 0.2f),
                )
            }
            Row(
                modifier = Modifier.padding(22.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Map, null, tint = Brand300, modifier = Modifier.size(12.dp))
                        Text("PHYSICAL HUB ROUTING", fontSize = 9.sp, fontWeight = FontWeight.Black,
                            color = Brand300, letterSpacing = 1.2.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Assistance Centers", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Locate nearby processing nodes.", fontSize = 11.sp, color = Color.White.copy(0.85f),
                        modifier = Modifier.padding(top = 3.dp))
                }
                Box(
                    modifier = Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(Brand500.copy(0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.NearMe, null, tint = Brand500, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

// ── Profile Snapshot Card ─────────────────────────────────────────────────────
@Composable
private fun ProfileSnapshotCard(profile: com.schemewise.app.data.model.Profile) {
    Card(
        modifier  = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape     = RoundedCornerShape(22.dp),
        colors    = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Brush.horizontalGradient(GradNavyBlue)))
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Person, null, tint = VioletAccent, modifier = Modifier.size(14.dp))
                    Text("PROFILE SNAPSHOT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Muted, letterSpacing = 1.2.sp)
                }
            }
            HorizontalDivider(color = Border)
            listOf(
                "Sector"     to (profile.ruralUrban ?: "—"),
                "Income"     to if (profile.income != null && profile.income > 0) "₹${profile.income.toLong()}" else "—",
                "Occupation" to (profile.occupation ?: "—"),
                "State"      to (profile.state ?: "—"),
            ).forEachIndexed { i, (k, v) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(k, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 0.5.sp)
                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.Black, color = OnSurface)
                }
                if (i < 3) HorizontalDivider(color = Border.copy(0.4f), modifier = Modifier.padding(horizontal = 16.dp))
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private data class StatItem(
    val label:     String,
    val value:     String,
    val icon:      ImageVector,
    val iconColor: Color,
    val bgColor:   Color,
    val onClick:   () -> Unit,
)

