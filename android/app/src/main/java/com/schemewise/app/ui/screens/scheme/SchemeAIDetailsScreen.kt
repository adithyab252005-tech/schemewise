package com.schemewise.app.ui.screens.scheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*

@Composable
fun SchemeAIDetailsScreen(
    schemeId: String,
    onBack: () -> Unit,
    viewModel: SchemeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(schemeId) {
        viewModel.loadDetails(schemeId)
    }

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title = "AI Analysis",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = OnSurface)
                    }
                },
                actions = {
                    if (!state.isLoading && state.scheme != null) {
                        IconButton(onClick = { viewModel.fetchAiDetails() }) {
                            Icon(Icons.Filled.AutoAwesome, "Re-Fetch", tint = Brand500)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator(color = Brand500, strokeWidth = 3.dp, modifier = Modifier.size(52.dp))
                            Text("Loading scheme details…", color = Muted, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                state.error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = Color(0xFF991B1B))
                            Spacer(Modifier.width(8.dp))
                            Text(state.error!!, color = Color(0xFF991B1B), fontSize = 13.sp)
                        }
                    }
                }

                state.scheme != null -> {
                    val scheme = state.scheme!!

                    // Scheme name header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(NavyDark, NavyLight)), RoundedCornerShape(20.dp))
                                .padding(20.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, null, tint = Brand500, modifier = Modifier.size(14.dp))
                                    Text("DETAILED AI ANALYSIS", fontSize = 10.sp, fontWeight = FontWeight.Black,
                                        color = Brand300, letterSpacing = 1.sp)
                                }
                                Text(scheme.schemeName, color = Color.White, fontSize = 20.sp,
                                    fontWeight = FontWeight.Black, lineHeight = 26.sp)
                                Text(
                                    "Deep extraction of requirements and parameters for this scheme.",
                                    color = Color.White.copy(0.65f), fontSize = 12.sp, lineHeight = 17.sp
                                )
                            }
                        }
                    }

                    // AI fetch status banner
                    if (state.isFetchingAiDetails) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Brand500.copy(0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Brand500.copy(0.3f))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(color = Brand500, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Fetching AI Details…", fontWeight = FontWeight.Bold, color = Brand600, fontSize = 14.sp)
                                    Text("Scraping official portal for latest content.", fontSize = 12.sp, color = Muted)
                                }
                            }
                        }
                    }

                    // AI fetch error banner
                    if (state.error != null && !state.isFetchingAiDetails) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Icon(Icons.Filled.Warning, null, tint = Color(0xFFDC2626))
                                Spacer(Modifier.width(8.dp))
                                Text(state.error!!, color = Color(0xFF991B1B), fontSize = 12.sp)
                            }
                        }
                    }

                    // AI Content card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Filled.AutoAwesome, null, tint = Brand500, modifier = Modifier.size(18.dp))
                                    Text("AI Extracted Description", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand600)
                                }
                                Button(
                                    onClick = { viewModel.fetchAiDetails() },
                                    enabled = !state.isFetchingAiDetails,
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(if (state.isFetchingAiDetails) "Fetching…" else "Re-Fetch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            HorizontalDivider(color = Border)

                            if (!scheme.description.isNullOrBlank()) {
                                Text(
                                    scheme.description,
                                    fontSize = 14.sp,
                                    color = OnSurface,
                                    lineHeight = 22.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("🤖", fontSize = 40.sp, textAlign = TextAlign.Center)
                                    Text("No detailed description available yet.",
                                        color = Muted, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                                    Text(
                                        "Tap \"Re-Fetch\" to extract the latest details from the official portal using AI.",
                                        fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center, lineHeight = 17.sp
                                    )
                                }
                            }
                        }
                    }

                    // Source link
                    if (!scheme.sourceUrl.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Filled.Link, null, tint = Brand500, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Official Source", fontSize = 11.sp, fontWeight = FontWeight.Black,
                                        color = Muted, letterSpacing = 0.5.sp)
                                    Text(scheme.sourceUrl, fontSize = 12.sp, color = Brand500,
                                        fontWeight = FontWeight.SemiBold, maxLines = 2)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
