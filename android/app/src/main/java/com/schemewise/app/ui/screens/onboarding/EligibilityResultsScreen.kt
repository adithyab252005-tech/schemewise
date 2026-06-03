package com.schemewise.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.ui.components.ShimmerCard
import com.schemewise.app.ui.components.StatusBadge
import com.schemewise.app.ui.components.GradientButton
import com.schemewise.app.ui.theme.*

/** Mirrors web EligibilityResultsPage.jsx with working search + status filters */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EligibilityResultsScreen(
    onContinue: () -> Unit,
    showContinueButton: Boolean = true,
    onSchemeClick: (String) -> Unit = {},
    viewModel: EligibilityResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Local filter state — default to Eligible only, no Partial
    var searchQuery    by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Eligible") }
    var selectedRegion by remember { mutableStateOf("All Regions") }

    // Apply local filters — only Eligible is shown, no Partial
    val filteredResults = remember(uiState.results, searchQuery, selectedRegion, uiState.profile) {
        uiState.results.filter { result ->
            val matchesSearch = searchQuery.isBlank() ||
                result.schemeName.contains(searchQuery, ignoreCase = true)
            val isEligible    = result.status == "Eligible"
            val matchesRegion = selectedRegion == "All Regions" || result.stateApplicable == uiState.profile?.state
            matchesSearch && isEligible && matchesRegion
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            if (showContinueButton) "Eligibility Results" else "My Eligible Schemes",
                            fontWeight = FontWeight.Black, fontSize = 17.sp, color = OnSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(0.97f))
            )
        },
        bottomBar = {
            if (showContinueButton) {
                Surface(shadowElevation = 12.dp, color = Surface) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        GradientButton(
                            text     = "Continue to Dashboard →",
                            onClick  = onContinue,
                            modifier = Modifier.fillMaxWidth(),
                            height   = 52.dp,
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Brand500)
                            Spacer(Modifier.height(12.dp))
                            Text("Evaluating your eligibility...", color = Muted, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // ── Premium Summary Header ────────────────────────────────
                item {
                    Card(
                        colors    = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(22.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF060F1E), Color(0xFF0D2040), Color(0xFF142F58))),
                                    RoundedCornerShape(22.dp)
                                )
                        ) {
                            // Ambient orb
                            Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                                drawCircle(
                                    brush  = Brush.radialGradient(
                                        listOf(Brand500.copy(0.2f), Color.Transparent),
                                        radius = 160f,
                                        center = Offset(size.width * 0.88f, size.height * 0.25f)
                                    ),
                                    radius = 160f,
                                    center = Offset(size.width * 0.88f, size.height * 0.25f),
                                )
                            }
                            Column(modifier = Modifier.padding(20.dp)) {
                                // Tricolor strip
                                Row(modifier = Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp))) {
                                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF97316)))
                                    Box(Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(0.25f)))
                                    Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF22C55E)))
                                }
                                Spacer(Modifier.height(16.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Success500, modifier = Modifier.size(16.dp))
                                    Text(
                                        if (showContinueButton) "Eligibility Results" else "My Eligible Schemes",
                                        fontSize = 10.sp, fontWeight = FontWeight.Black,
                                        color = Color.White.copy(0.6f), letterSpacing = 1.2.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${filteredResults.size} Schemes Matched",
                                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Based on your profile · Tap any scheme to explore",
                                    color = Color.White.copy(0.58f), fontSize = 12.sp,
                                )
                                Spacer(Modifier.height(16.dp))
                                // Stat pill — Eligible only
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    val eligibleCount = filteredResults.size
                                    StatPill("✓ $eligibleCount Eligible", BadgeGreenBg, BadgeGreenText)
                                }
                            }
                        }
                    }
                }

                // --- FILTER BAR ---
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Search field
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFF1F5F9))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Search, null, tint = Muted, modifier = Modifier.size(18.dp))
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        color = Brand800,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = SolidColor(Brand500),
                                    decorationBox = { inner ->
                                        if (searchQuery.isEmpty()) {
                                            Text("Search schemes...", color = Muted, fontSize = 14.sp)
                                        }
                                        inner()
                                    }
                                )
                            }


                            // Region filter chips
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFF0F172A).copy(0.05f)).padding(4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val stateName = uiState.profile?.state ?: "My State"
                                val region1 = "All Regions"
                                val region2 = "$stateName Specific"
                                listOf(region1, region2).forEach { rStr ->
                                    val isSelected = (rStr == region1 && selectedRegion == "All Regions") || (rStr == region2 && selectedRegion == "My State Only")
                                    Surface(
                                        onClick = { selectedRegion = if (rStr == region1) "All Regions" else "My State Only" },
                                        shape = RoundedCornerShape(50),
                                        color = if (isSelected) Color(0xFF0F172A) else Color.Transparent,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            rStr,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Targeted matches section
                if (uiState.interestedMatches.isNotEmpty()) {
                    item {
                        Text(
                            "🎯 Targeted Matches — \"${uiState.interestedScheme}\"",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Brand600,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(uiState.interestedMatches) { result ->
                        EligibilityResultCard(result = result, isTargeted = true, onClick = { onSchemeClick(result.schemeId.toString()) })
                    }
                    item { Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 4.dp)) }
                }

                if (filteredResults.isNotEmpty()) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(Color(0xFF22C55E)))
                            Text("✓ Fully Eligible (${filteredResults.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF15803D))
                        }
                    }
                    items(filteredResults) { result ->
                        EligibilityResultCard(result = result, onClick = { onSchemeClick(result.schemeId.toString()) })
                    }
                }

                if (filteredResults.isEmpty() && !uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 40.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    if (searchQuery.isNotBlank() || selectedStatus != "All")
                                        "No results match your filters."
                                    else
                                        "No eligible schemes found for your current profile.",
                                    color = Muted,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EligibilityResultCard(result: EligibilityResult, isTargeted: Boolean = false, onClick: () -> Unit) {
    val isEligible = result.status == "Eligible"
    val borderColor = when {
        isTargeted -> Brand500
        isEligible -> Color(0xFF22C55E)
        else -> Color(0xFFF59E0B)
    }
    val bgHint = when {
        isTargeted -> Brand50
        isEligible -> Color(0xFFF0FDF4)
        else -> Color(0xFFFFFBEB)
    }

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        border = BorderStroke(1.5.dp, borderColor.copy(0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgHint.copy(0.4f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isTargeted) {
                    Text(
                        "🎯 TARGETED MATCH",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand600,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    result.schemeName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Brand800,
                    lineHeight = 18.sp
                )

                if (!result.improvementSuggestion.isNullOrEmpty() && !isEligible) {
                    Text(
                        "💡 ${result.improvementSuggestion}",
                        color = Color(0xFF92400E),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            StatusBadge(status = result.status)
        }
    }
}

// ── Stat Pill helper ──────────────────────────────────────────────────────────
@Composable
private fun StatPill(label: String, bgColor: Color, textColor: Color) {
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = bgColor,
    ) {
        Text(
            label,
            fontSize   = 11.sp,
            fontWeight = FontWeight.Bold,
            color      = textColor,
            modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
