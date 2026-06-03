package com.schemewise.app.ui.screens.household

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.schemewise.app.ui.theme.*

data class ComboBenefit(
    val id: Int,
    val title: String,
    val tag: String,
    val matchScore: String,
    val membersNeeded: Int,
    val amountStr: String,
    val glowColor: Color,
    val borderColor: Color
)

val mockCombos = listOf(
    ComboBenefit(1, "Family Healthcare Alliance (PM-JAY)", "Medical", "98%", 3, "₹5,00,000", Color(0xFFF43F5E), Color(0xFFF43F5E).copy(alpha = 0.2f)),
    ComboBenefit(2, "Agricultural Household Subsidy", "Farming", "85%", 2, "₹12,000 / yr", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.2f)),
    ComboBenefit(3, "First-Generation Scholar Sync", "Education", "70%", 2, "Tuition Waiver", Color(0xFF6366F1), Color(0xFF6366F1).copy(alpha = 0.2f))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboBenefitsScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val extractingIds = remember { mutableStateListOf<Int>() }
    val extractedIds = remember { mutableStateListOf<Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Household Benefits", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B0E14)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Fake gradient overlay for premium look
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0B0E14).copy(alpha = 0.8f))
                                )
                            )
                        )

                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(Color(0xFFF59E0B).copy(alpha=0.1f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFFF59E0B).copy(alpha=0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PeopleAlt, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("FAMILY LINK ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFFF59E0B), letterSpacing = 1.sp)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text("Household", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Text("Benefits", color = Color(0xFFF59E0B), fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Link your family members to unlock combined household schemes.",
                                color = Color.White.copy(alpha=0.6f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Map list of combos
            items(mockCombos) { combo ->
                val isExtracting = extractingIds.contains(combo.id)
                val isExtracted = extractedIds.contains(combo.id)

                val containerColor = if (isExtracted) Color(0xFF10B981).copy(alpha = 0.1f) else Color.White
                val borderColor = if (isExtracted) Color(0xFF10B981).copy(alpha = 0.5f) else combo.borderColor

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                combo.tag.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isExtracted) Color(0xFF10B981) else combo.glowColor,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    "${combo.matchScore} Match",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(combo.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, color = OnSurface)
                        Spacer(Modifier.height(6.dp))
                        Text("Requires data synchronization from ${combo.membersNeeded} household members.", fontSize = 13.sp, color = Muted)

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        Spacer(Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("YIELD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
                                Text(combo.amountStr, fontSize = 16.sp, fontWeight = FontWeight.Black, color = OnSurface)
                            }
                            
                            val btnColor = if (isExtracted) Color(0xFF10B981) else if (isExtracting) Color(0xFFE2E8F0) else Color(0xFFF59E0B).copy(alpha=0.1f)
                            val btnTextColor = if (isExtracted) Color.White else if (isExtracting) Muted else Color(0xFFD97706)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = btnColor,
                                modifier = Modifier.clickable(enabled = !isExtracting && !isExtracted) {
                                    extractingIds.add(combo.id)
                                    coroutineScope.launch {
                                        delay(2000)
                                        extractingIds.remove(combo.id)
                                        extractedIds.add(combo.id)
                                    }
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    if (isExtracted) {
                                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                        Spacer(Modifier.width(6.dp))
                                    } else if (isExtracting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Muted, strokeWidth = 2.dp)
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(
                                        when {
                                            isExtracted -> "Extracted"
                                            isExtracting -> "Syncing..."
                                            else -> "Extract"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = btnTextColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Footer Guide
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.HelpOutline, null, tint = Muted)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("How do household benefits work?", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurface)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Certain government schemas strictly require linked household profiles. By ensuring all family members are actively added to your Profile Network, the engine can find special packages meant for your entire family.",
                                fontSize = 13.sp, color = Muted, lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
