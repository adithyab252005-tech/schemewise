package com.schemewise.app.ui.screens.civic

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.screens.profile.ProfileViewModel
import com.schemewise.app.ui.theme.*

private data class CivicDoc(
    val name: String,
    val reason: String,
    val required: Boolean,
    val vector: String,
)

private fun buildDocList(
    category: String?,
    isBpl: String?,
    isDifferentlyAbled: String?,
): List<CivicDoc> {
    val docs = mutableListOf(
        CivicDoc("Aadhaar Card",         "Primary Identity Verification",         true, "Identity"),
        CivicDoc("PAN Card",             "Financial & Tax Compliance",             true, "Identity"),
        CivicDoc("Domicile Certificate", "State-level Scheme Entitlement",         true, "Demographic"),
        CivicDoc("Income Certificate",   "Socio-Economic Verification",            true, "SocioEconomic"),
    )
    if (!category.isNullOrBlank() && category != "General") {
        docs.add(CivicDoc("Caste Certificate", "Verifies $category Category Entitlements", true, "Demographic"))
    }
    if (isBpl == "Yes") {
        docs.add(CivicDoc("Ration Card (BPL)", "Verifies Below Poverty Line Status", true, "SocioEconomic"))
    } else {
        docs.add(CivicDoc("Ration Card", "Optional Family Proof", false, "SocioEconomic"))
    }
    if (isDifferentlyAbled == "Yes") {
        docs.add(CivicDoc("Disability Certificate", "Special Exemptions & Healthcare", true, "Special"))
    }
    return docs
}

@Composable
fun CivicReadinessScreen(
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
) {
    val ctx = LocalContext.current
    val uiState by profileViewModel.uiState.collectAsState()
    val profile = uiState.profile

    // Persist checked docs to SharedPreferences
    val prefKey = "civic_docs_${profile?.userId ?: 0}"
    val prefs = remember { ctx.getSharedPreferences("civic_prefs", Context.MODE_PRIVATE) }
    var checkedDocs by remember {
        mutableStateOf(
            prefs.getStringSet(prefKey, emptySet())?.toSet() ?: emptySet()
        )
    }

    fun toggleDoc(docName: String) {
        checkedDocs = if (docName in checkedDocs) checkedDocs - docName else checkedDocs + docName
        prefs.edit().putStringSet(prefKey, checkedDocs).apply()
    }

    val docs = buildDocList(
        category          = profile?.category,
        isBpl             = profile?.isBPL,
        isDifferentlyAbled = profile?.isDifferentlyAbled,
    )

    val totalRequired = docs.count { it.required }
    val totalFilled   = docs.count { it.required && it.name in checkedDocs }
    val overallIndex  = if (totalRequired > 0) (totalFilled * 100 / totalRequired) else 100

    val indexColor = when {
        overallIndex >= 85 -> Color(0xFF22C55E)
        overallIndex >= 50 -> Color(0xFFF59E0B)
        else               -> Color(0xFFF43F5E)
    }

    // Group docs by vector for the summary panel
    fun vectorSummary(vector: String): Triple<Int, Int, Boolean> {
        val req = docs.filter { it.vector == vector && it.required }
        val checked = req.count { it.name in checkedDocs }
        return Triple(checked, req.size, checked == req.size && req.isNotEmpty())
    }

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title = "Civic Readiness",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = OnSurface)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Hero score card ──────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(NavyDark, NavyLight)),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFF22C55E).copy(0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Filled.Security, null,
                                            tint = Color(0xFF22C55E), modifier = Modifier.size(10.dp))
                                        Text("Security Core Verified", color = Color(0xFF22C55E),
                                            fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text("Civic\nReadiness", color = Color.White,
                                    fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Analyze your governmental profile strength. Check off actual documents you possess.",
                                    color = Color.White.copy(0.65f), fontSize = 12.sp, lineHeight = 17.sp
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            // Index score ring
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                                    .padding(20.dp)
                            ) {
                                Text(
                                    "$overallIndex",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    color = indexColor
                                )
                                Text("%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = indexColor)
                                Text("Overall\nIndex", fontSize = 10.sp, color = Color.White.copy(0.6f),
                                    textAlign = TextAlign.Center, letterSpacing = 0.5.sp, lineHeight = 14.sp)
                            }
                        }
                    }
                }
            }

            // ── Compliance summary panel ─────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.VerifiedUser, null, tint = Brand500, modifier = Modifier.size(16.dp))
                            Text("Document Compliance", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                            Spacer(Modifier.weight(1f))
                            Text("$totalFilled / $totalRequired Secured",
                                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                        }

                        val vectorList = mutableListOf(
                            Triple("Identity Documents", vectorSummary("Identity"), Color(0xFF22C55E)),
                            Triple("Socio-Economic Proof", vectorSummary("SocioEconomic"), Color(0xFFF59E0B)),
                            Triple("Demographic Certificates", vectorSummary("Demographic"), VioletAccent),
                        )
                        if (profile?.isDifferentlyAbled == "Yes") {
                            vectorList.add(Triple("Special Exemptions", vectorSummary("Special"), Color(0xFF3B82F6)))
                        }

                        vectorList.forEach { (label, summary, color) ->
                            val (checked, total, verified) = summary
                            if (total > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(100)))
                                        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("$checked/$total", fontSize = 12.sp, fontWeight = FontWeight.Black, color = OnSurface)
                                        Icon(
                                            if (verified) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                                            null,
                                            tint = if (verified) Color(0xFF22C55E) else Border,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Overall progress bar
                        HorizontalDivider(color = Border)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("OVERALL COMPLETENESS", fontSize = 10.sp, fontWeight = FontWeight.Black,
                                color = Muted, letterSpacing = 1.sp)
                            Text("$overallIndex%", fontSize = 13.sp, fontWeight = FontWeight.Black, color = indexColor)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100))
                                .background(Border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(overallIndex / 100f)
                                    .fillMaxHeight()
                                    .background(indexColor, RoundedCornerShape(100))
                            )
                        }
                    }
                }
            }

            // ── Document checklist ───────────────────────────────────────
            item {
                Text(
                    "REQUIRED DOCUMENTATION",
                    fontSize = 10.sp, fontWeight = FontWeight.Black,
                    color = Muted, letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            items(docs) { doc ->
                val isDone = doc.name in checkedDocs
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { toggleDoc(doc.name) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDone) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDone) Color(0xFF22C55E).copy(0.4f) else Border
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Icon(
                            if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null,
                            tint = if (isDone) Color(0xFF22C55E) else Border,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    doc.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isDone) Color(0xFF15803D) else OnSurface
                                )
                                if (!doc.required) {
                                    Surface(
                                        color = Background,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("Optional", fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                            color = Muted, letterSpacing = 0.5.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                            Text(
                                doc.reason,
                                fontSize = 12.sp,
                                color = if (isDone) Color(0xFF16A34A).copy(0.7f) else Muted,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // ── Info tip ─────────────────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.Info, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                        Text(
                            "Tick each document you physically possess. Your Civic Readiness Index updates instantly and is used to prioritize your scheme applications.",
                            fontSize = 12.sp, color = Color(0xFF1D4ED8), lineHeight = 18.sp, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
