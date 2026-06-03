package com.schemewise.app.ui.screens.scheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.EmptyState
import com.schemewise.app.ui.components.SchemeCardSkeleton
import com.schemewise.app.ui.components.StatusBadge
import com.schemewise.app.ui.screens.simulator.SimulatorViewModel
import com.schemewise.app.ui.theme.*

/** Mirrors web SchemeResultsPage.jsx — shows results of simulator output */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeResultsScreen(
    onSchemeClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SimulatorViewModel = hiltViewModel(), // Shared viewmodel tied to nav graph
) {
    val state by viewModel.uiState.collectAsState()

    // Reset state when user navigates back so SimulatorScreen doesn't re-trigger navigation
    DisposableEffect(Unit) { onDispose { viewModel.reset() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simulation Results", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) { items(4) { SchemeCardSkeleton() } }

            state.results.isEmpty() && state.isDone -> Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                EmptyState(
                    icon = Icons.Filled.AssignmentTurnedIn,
                    title = "No schemes match",
                    subtitle = "Try adjusting the simulation parameters."
                )
            }

            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Found ${state.results.size} matches based on input", color = Muted)
                    Spacer(Modifier.height(8.dp))
                }
                items(state.results, key = { it.schemeId }) { result ->
                    Card(
                        modifier  = Modifier.fillMaxWidth().clickable { onSchemeClick(result.schemeId.toString()) },
                        colors    = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(1.dp),
                        shape     = RoundedCornerShape(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(result.schemeName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Brand800, maxLines = 2)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {

                                    if (result.maxFinancialValueInr > 0) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("• Max Benefit: ₹${result.maxFinancialValueInr}", color = Success600, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                                
                                if (result.status == "Partially Eligible" && !result.missingConditions.isNullOrEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Card(colors = CardDefaults.cardColors(containerColor = BadgeOrangeBg)) {
                                        Column(Modifier.padding(8.dp)) {
                                            Text("Almost Eligible! Missing:", color = BadgeOrangeText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            result.missingConditions.take(2).forEach { cond ->
                                                Text("• $cond", color = BadgeOrangeText, fontSize = 11.sp, lineHeight = 14.sp)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            StatusBadge(status = result.status)
                        }
                    }
                }
            }
        }
    }
}
