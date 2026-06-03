package com.schemewise.app.ui.screens.scheme

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

/** Compare Schemes — rebuilt with God-Level Smart AI Verdict */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onBack: () -> Unit,
    viewModel: CompareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var searchA by remember { mutableStateOf("") }
    var searchB by remember { mutableStateOf("") }
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    val filteredA = uiState.schemes.filter { it.schemeName.contains(searchA, ignoreCase = true) }.take(5)
    val filteredB = uiState.schemes.filter { it.schemeName.contains(searchB, ignoreCase = true) }.take(5)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compare Schemes", fontWeight = FontWeight.Bold, color = Brand800) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = Brand800) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ── Search Selectors ─────────────────────────────────────────────
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                // Scheme A Selector
                ExposedDropdownMenuBox(expanded = expandedA, onExpandedChange = { expandedA = it }) {
                    OutlinedTextField(
                        value = uiState.schemeA?.schemeName ?: searchA,
                        onValueChange = { searchA = it; expandedA = true },
                        label = { Text("Select First Scheme") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedA) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Brand500)
                    )
                    ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                        filteredA.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.schemeName) },
                                onClick = {
                                    viewModel.selectSchemeA(s.schemeId.toString())
                                    searchA = s.schemeName
                                    expandedA = false
                                }
                            )
                        }
                    }
                }

                // Scheme B Selector
                ExposedDropdownMenuBox(expanded = expandedB, onExpandedChange = { expandedB = it }) {
                    OutlinedTextField(
                        value = uiState.schemeB?.schemeName ?: searchB,
                        onValueChange = { searchB = it; expandedB = true },
                        label = { Text("Select Second Scheme") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedB) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VioletAccent)
                    )
                    ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                        filteredB.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.schemeName) },
                                onClick = {
                                    viewModel.selectSchemeB(s.schemeId.toString())
                                    searchB = s.schemeName
                                    expandedB = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { viewModel.compare() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = uiState.schemeA != null && uiState.schemeB != null && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand600)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Analyze Stacking & Verdict", fontWeight = FontWeight.ExtraBold)
                }
            }

            if (uiState.error != null) {
                Text(uiState.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(16.dp))
            }

            // ── AI Smart Verdict Hero ─────────────────────────────────────────
            if (uiState.verdict != null) {
                AiVerdictBanner(verdict = uiState.verdict!!)
            }

            // ── Attribute comparison rows ─────────────────────────────────────
            if (uiState.schemeA != null && uiState.schemeB != null) {
                val sA = uiState.schemeA!!
                val sB = uiState.schemeB!!

                Spacer(Modifier.height(8.dp))
                CompareAttribute("Category", sA.schemeCategory ?: "All", sB.schemeCategory ?: "All", false)
                CompareAttribute("Type", sA.schemeType ?: "Central", sB.schemeType ?: "Central", false)
                CompareAttribute("State", sA.stateApplicable ?: "All", sB.stateApplicable ?: "All", false)
                CompareAttribute("Min Age", "${sA.targetAgeMin?.toInt() ?: 0} yrs", "${sB.targetAgeMin?.toInt() ?: 0} yrs", (sA.targetAgeMin ?: 0.0) <= (sB.targetAgeMin ?: 0.0))
                CompareAttribute("Income Limit", "₹${sA.incomeMax ?: "N/A"}", "₹${sB.incomeMax ?: "N/A"}", (sA.incomeMax ?: 0.0) >= (sB.incomeMax ?: 0.0))
                
                Spacer(Modifier.height(32.dp))
            } else if (!uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.CompareArrows, null, modifier = Modifier.size(64.dp), tint = Muted.copy(0.2f))
                    Text("Select two schemes to compare eligibility and stacking rules.", 
                        textAlign = TextAlign.Center, color = Muted, fontSize = 14.sp)
                }
            }
        }
    }
}

// ── AI Verdict Banner ─────────────────────────────────────────────────────────
@Composable
private fun AiVerdictBanner(verdict: String) {
    // Animated shimmer pulse on the icon
    val infiniteTransition = rememberInfiniteTransition(label = "verdict_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Card(
        modifier  = Modifier.fillMaxWidth().padding(16.dp),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(containerColor = Color(0xFF0D0D1A)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box {
            // Gradient glow canvas
            Box(
                modifier = Modifier.fillMaxWidth().height(4.dp)
                    .background(Brush.horizontalGradient(GradElectricSunset))
            )
            Column(modifier = Modifier.padding(top = 4.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color  = Brand500.copy(0.2f),
                            shape  = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.AutoAwesome, null,
                                    tint = Brand500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                "SchemeWise AI Verdict",
                                color = Brand400, fontWeight = FontWeight.Black,
                                fontSize = 11.sp, letterSpacing = 1.sp
                            )
                            Text(
                                "Personalized recommendation based on your profile",
                                color = Color.White.copy(0.5f), fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        verdict,
                        color      = Color.White,
                        fontSize   = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Scheme Header ──────────────────────────────────────────────────────────────
@Composable
private fun SchemeHeader(name: String, color: Color, isWinner: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(if (isWinner) 6.dp else 2.dp),
        border    = if (isWinner) androidx.compose.foundation.BorderStroke(2.dp, Brand500) else null
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isWinner) {
                Surface(
                    color = Brand500, shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Text(
                        "⭐ AI PICK", color = Color.White,
                        fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            // Icon Placeholder
            Box(
                modifier = Modifier.size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Brand800, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

// ── Attribute Comparison Row ───────────────────────────────────────────────────
@Composable
private fun CompareAttribute(label: String, valueA: String, valueB: String, aWins: Boolean) {
    Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column {
            // Label row
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(label.uppercase(), color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ValueCell(
                    value  = valueA,
                    isWin  = aWins,
                    color  = Brand500,
                    modifier = Modifier.weight(1f)
                )
                ValueCell(
                    value  = valueB,
                    isWin  = !aWins,
                    color  = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}

@Composable
private fun ValueCell(value: String, isWin: Boolean, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isWin) color.copy(0.08f) else Color.Transparent)
            .border(
                width = if (isWin) 1.dp else 0.dp,
                color = if (isWin) color.copy(0.3f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (isWin) {
                Icon(Icons.Filled.CheckCircle, null, tint = color, modifier = Modifier.size(12.dp))
            }
            Text(
                value,
                color      = if (isWin) color else Muted,
                fontSize   = 12.sp,
                fontWeight = if (isWin) FontWeight.Bold else FontWeight.Normal,
                lineHeight = 16.sp
            )
        }
    }
}

// ── Document List Card ─────────────────────────────────────────────────────────
@Composable
private fun DocList(schemeName: String, docs: List<String>, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(schemeName, color = color, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            HorizontalDivider(color = color.copy(0.2f))
            docs.forEach { doc ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                    Text(doc, fontSize = 11.sp, color = Brand800)
                }
            }
        }
    }
}

// ── Data class ────────────────────────────────────────────────────────────────
private data class CompareScheme(
    val name: String, val ministry: String, val benefit: String,
    val maxAge: String, val income: String, val gender: String,
    val documents: List<String>, val matchPct: Int,
    val speed: String, val color: Color
)
