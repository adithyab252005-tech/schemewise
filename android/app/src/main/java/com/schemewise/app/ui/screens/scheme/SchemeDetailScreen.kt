package com.schemewise.app.ui.screens.scheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import com.schemewise.app.ui.components.StatusBadge
import com.schemewise.app.util.PrintUtils
import com.schemewise.app.ui.theme.*

/** Mirrors web SchemeDetailPage.jsx */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchemeDetailScreen(
    schemeId: String,
    onBack: () -> Unit,
    onAskAi: (String) -> Unit = {},
    viewModel: SchemeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsManager = remember { com.schemewise.app.util.TtsManager(context) }
    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    LaunchedEffect(schemeId) {
        viewModel.loadDetails(schemeId)
    }

    LaunchedEffect(state.saveMessage) {
        if (state.saveMessage != null) {
            android.widget.Toast.makeText(context, state.saveMessage, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.release() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scheme Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnSurface) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    if (state.scheme != null) {
                        IconButton(onClick = {
                            val s = state.scheme!!
                            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                            val safeCategory = s.schemeCategory?.replace("\"", "") ?: "General"
                            val aiSafe = state.aiDetails?.replace("\n", "<br>") ?: "No personalized explanation available. Tap 'Ask AI' to generate a guide."
                            val descSafe = s.description?.replace("\n", "<br>") ?: "Official scheme targeted at improving socio-economic outcomes."
                            
                            val htmlContent = """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta charset="UTF-8">
                                    <script src="https://cdn.tailwindcss.com"></script>
                                    <style>
                                        body { font-family: Arial, sans-serif; }
                                        @media print {
                                            @page { margin: 20mm; }
                                            .print-break-avoid { page-break-inside: avoid; }
                                        }
                                    </style>
                                </head>
                                <body class="bg-white text-black p-8">
                                    <div class="max-w-4xl mx-auto">
                                        <!-- Header -->
                                        <div class="border-b-4 border-slate-900 pb-8 mb-8 flex justify-between items-start">
                                            <div>
                                                <div class="flex items-center gap-2 mb-2">
                                                    <div class="w-10 h-10 bg-slate-900 text-white font-black flex items-center justify-center rounded text-xl">S</div>
                                                    <span class="text-2xl font-black tracking-tighter">SchemeWise</span>
                                                </div>
                                                <p class="text-xs font-bold text-slate-500 tracking-widest uppercase">Verified Official Document</p>
                                            </div>
                                            <div class="text-right">
                                                <p class="text-sm font-bold text-slate-600">Generated: ${"$"}{dateStr}</p>
                                                <p class="text-xs font-medium text-slate-500 mt-1">Ref ID: SW-${"$"}{s.schemeId}-PR</p>
                                            </div>
                                        </div>
                            
                                        <!-- Metadata -->
                                        <div class="mb-10">
                                            <div class="inline-block px-3 py-1 bg-slate-100 border border-slate-300 text-slate-800 text-[10px] font-black uppercase tracking-widest mb-4">
                                                ${"$"}{safeCategory} | ${"$"}{s.schemeType ?: "Central"}
                                            </div>
                                            <h1 class="text-4xl font-black tracking-tight leading-tight mb-4 text-slate-900">
                                                ${"$"}{s.schemeName}
                                            </h1>
                                            <p class="text-base font-bold text-slate-600 flex items-center gap-2">
                                                ${"$"}{s.ministry ?: "Official Government Scheme"}
                                            </p>
                                        </div>
                            
                                        <!-- Demographics Matrix -->
                                        <h3 class="text-lg font-black uppercase tracking-widest border-b-2 border-slate-200 pb-2 mb-4">Eligibility Parameters</h3>
                                        <div class="grid grid-cols-4 gap-4 mb-10 text-sm">
                                            <div class="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                                                <span class="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Age Limit</span>
                                                <span class="font-bold underline decoration-slate-300">${"$"}{s.targetAgeMin?.toInt() ?: "Any"} - ${"$"}{s.targetAgeMax?.toInt() ?: "Any"}</span>
                                            </div>
                                            <div class="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                                                <span class="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Income Limit</span>
                                                <span class="font-bold underline decoration-slate-300">₹${"$"}{s.incomeMin?.toLong() ?: "0"} - ${"$"}{s.incomeMax?.toLong() ?: "No Limit"}</span>
                                            </div>
                                            <div class="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                                                <span class="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Sector Focus</span>
                                                <span class="font-bold underline decoration-slate-300">${"$"}{s.ruralUrban ?: "Both"}</span>
                                            </div>
                                            <div class="bg-slate-50 border border-slate-200 p-4 rounded-lg">
                                                <span class="block text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Target Demo</span>
                                                <span class="font-bold underline decoration-slate-300">${"$"}{s.eligibleCategories?.toString()?.replace("[", "")?.replace("]", "") ?: "Any"}</span>
                                            </div>
                                        </div>
                            
                                        <!-- Core Guidelines -->
                                        <h3 class="text-lg font-black uppercase tracking-widest border-b-2 border-slate-200 pb-2 mb-4">Core Guidelines</h3>
                                        <div class="bg-white border-l-4 border-slate-900 pl-6 py-2 mb-10">
                                            <p class="text-sm font-medium leading-relaxed text-slate-700">
                                                ${"$"}{descSafe}
                                            </p>
                                        </div>
                            
                                        <!-- AI Guide -->
                                        <div class="bg-slate-50 border border-slate-200 rounded-2xl p-8 mb-10 print-break-avoid">
                                            <h3 class="text-lg font-black uppercase tracking-widest border-b border-slate-300 pb-3 mb-6">
                                                Authorized Application Method (AI Guide)
                                            </h3>
                                            <div class="text-sm font-medium leading-loose text-slate-800">
                                                ${"$"}{aiSafe}
                                            </div>
                                        </div>
                            
                                        <!-- Verfication Box -->
                                        <div class="mt-8 border-t-2 border-slate-200 pt-8 flex items-center justify-between print-break-avoid">
                                            <div>
                                                <p class="text-[10px] font-bold text-slate-500 uppercase tracking-widest mb-1">Official Portal Gateway</p>
                                                <a href="${"$"}{s.sourceUrl ?: "https://myscheme.gov.in"}" class="text-sm font-black underline text-slate-900">
                                                    ${"$"}{s.sourceUrl ?: "https://myscheme.gov.in"}
                                                </a>
                                            </div>
                                            <div class="text-right text-xs font-bold uppercase tracking-widest text-slate-400">
                                                End of Record
                                            </div>
                                        </div>
                                    </div>
                                </body>
                                </html>
                            """.trimIndent()
                            
                            PrintUtils.printHtmlToPdf(context, "SchemeWise_Report_${"$"}{s.schemeId}", htmlContent)
                        }) {
                            Icon(Icons.Filled.Print, "Print/Share Full Details", tint = Brand500)
                        }
                    }
                    // WhatsApp Share Button
                    if (state.scheme != null) {
                        IconButton(onClick = {
                            val s = state.scheme!!
                            val msg = "\uD83D\uDEA8 Found a government scheme on SchemeWise!\n\n*${s.schemeName}*\n" +
                                "Type: ${s.schemeType ?: "Central"} | State: ${s.stateApplicable ?: "All India"}\n" +
                                "Category: ${s.schemeCategory ?: "General"}\n\n" +
                                "Check if you are eligible: ${s.sourceUrl ?: "https://myscheme.gov.in"}"
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, msg)
                                setPackage("com.whatsapp")
                            }
                            try {
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                // WhatsApp not installed, fallback to generic share
                                val fallback = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, msg)
                                }
                                context.startActivity(Intent.createChooser(fallback, "Share via"))
                            }
                        }) {
                            Icon(Icons.Filled.Share, "Share on WhatsApp", tint = Color(0xFF25D366))
                        }
                    }
                    IconButton(onClick = { viewModel.saveToHub(context) }, enabled = !state.isSaving) { 
                        Icon(if (state.saveMessage == "Saved to Hub") Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder, "Save", tint = Brand500) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            if (state.scheme != null) {
                FloatingActionButton(
                    onClick = {
                        val scheme = state.scheme ?: return@FloatingActionButton
                        val prompt = "Please provide a detailed explanation of the scheme \"${scheme.schemeName}\" and explain exactly how and why I am eligible for it based on my profile."
                        onAskAi(prompt)
                    },
                    containerColor = Brand500,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SmartToy, "Ask AI")
                        Spacer(Modifier.width(8.dp))
                        Text("Ask AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        bottomBar = {
            if (state.scheme != null) {
                val sourceUrl = state.scheme!!.sourceUrl ?: "https://myscheme.gov.in"
                Surface(shadowElevation = 8.dp) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.OpenInNew, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC) // Light surface background
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand500)
            }
        } else if (state.error != null && state.scheme == null) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Failed to load scheme", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand800)
                    Text(state.error ?: "", color = Muted, fontSize = 13.sp)
                }
            }
        } else if (state.scheme != null) {
            val s = state.scheme!!
            var selectedTabIndex by remember { mutableStateOf(0) }
            val tabs = listOf("Overview", "Eligibility")

            Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
                // Header (Always Visible)
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        if (!s.schemeCategory.isNullOrEmpty() && !s.schemeCategory.equals("General", ignoreCase = true)) {
                            Surface(color = Brand500.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp), border = BorderStroke(1.dp, Brand500.copy(alpha = 0.2f))) {
                                Text(s.schemeCategory!!, color = Brand700, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        } else { Spacer(Modifier.width(8.dp)) }
                        if (state.matchStatus != null) { StatusBadge(state.matchStatus!!) }
                    }

                    Text(s.schemeName, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Brand800, lineHeight = 32.sp)
                    if (!s.ministry.isNullOrEmpty()) {
                        Text(s.ministry!!, color = Muted, fontSize = 14.sp)
                    }
                }

                // ── AI Verdict Hero Header ─────────────────────────────────────────────
                if (state.suggestion != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EC)),
                        border = BorderStroke(1.dp, Brand500.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = Brand500.copy(0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.AutoAwesome, null, tint = Brand500, modifier = Modifier.padding(6.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SchemeWise AI Analysis", color = Brand700, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
                                }
                                IconButton(
                                    onClick = { ttsManager.toggle(state.suggestion!!) },
                                    modifier = Modifier.size(28.dp).background(if (isSpeaking) Brand500.copy(0.1f) else Color.Transparent, CircleShape)
                                ) {
                                    // Removed small speaker icon above content
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                state.suggestion!!,
                                color = Brand900,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(16.dp))

                            // Parity Action Buttons (Ask AI, Listen)
                            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                                Button(
                                    onClick = { onAskAi("Please provide a detailed explanation of the scheme \"${s.schemeName}\" and explain exactly how and why I am eligible for it based on my profile.") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand500.copy(0.1f), contentColor = Brand600),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.SmartToy, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Ask AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { ttsManager.toggle(state.suggestion!!) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSpeaking) Brand500 else Color.White.copy(0.5f),
                                        contentColor = if (isSpeaking) Color.White else Brand800
                                    ),
                                    border = BorderStroke(1.dp, Brand500.copy(0.2f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(if (isSpeaking) "Stop Audio" else "Listen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        // Bottom decorative glow strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = GradElectricSunset
                                    )
                                )
                        )
                    }
                }

                // ── Pre-Flight Bureaucrat Check ─────────────────────────────────────────────


                // Tabs
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = Brand500,
                    divider = { HorizontalDivider(color = Border) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) }
                        )
                    }
                }

                // Tab Content
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTabIndex == 0) {
                        Text("Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand800)

                        if (!s.description.isNullOrEmpty()) {
                            Text(s.description!!, color = OnSurface, fontSize = 14.sp, lineHeight = 20.sp)
                            Spacer(Modifier.height(12.dp))
                        }
                        
                        Text("For a detailed explanation, please click the AI Fetch Details button below.", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(16.dp))

                        if (state.aiDetails != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    // Removed the Explain like I'm 10 switch entirely
                                    
                                    Button(
                                        onClick = { ttsManager.toggle(state.aiDetails!!) },
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if(isSpeaking) Brand500.copy(0.1f) else Color.Transparent, contentColor = Brand600),
                                        elevation = null
                                    ) {
                                        Icon(if (isSpeaking) Icons.Filled.Stop else Icons.Filled.VolumeUp, "Listen", modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (isSpeaking) "Stop" else "Listen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(state.aiDetails!!, color = Brand800, fontSize = 14.sp, lineHeight = 20.sp)
                                    }
                                }
                            }
                        } else if (!state.isFetchingAiDetails) {
                            Button(
                                onClick = { viewModel.fetchAiDetails() },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Brand50.copy(0.5f)),
                                border = BorderStroke(1.dp, Brand500.copy(0.2f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                    Icon(Icons.Filled.AutoAwesome, "Guide", tint = Brand600)
                                    Spacer(Modifier.width(8.dp))
                                    Text("GET APPLICATION GUIDE", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Brand600, letterSpacing = 1.sp)
                                }
                            }
                        } else {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        Text("Official Requirements", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand800)

                        FlowRow(modifier = Modifier.fillMaxWidth(), mainAxisSpacing = 12.dp, crossAxisSpacing = 12.dp) {
                            RequirementCard("Age Limits", "${s.targetAgeMin ?: 0}+ to ${s.targetAgeMax ?: "No Limit"}")
                            RequirementCard("Gender", s.targetGender ?: "All")
                            RequirementCard("Income Limit", if (s.incomeMax != null) "Up to ₹${s.incomeMax}" else "No Limit")
                            RequirementCard("Social Groups", s.eligibleCategories?.toString() ?: "All")
                            RequirementCard("Occupation", s.occupationRequired?.toString() ?: "Any")
                            RequirementCard("Area", s.ruralUrban ?: "Both")
                        }
                    }
                    Spacer(Modifier.height(80.dp)) // Padding for bottom bar
                }
            }
        }
    }
}

@Composable
fun RequirementCard(label: String, value: String) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.width(160.dp),
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label.uppercase(), color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Brand800, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(mainAxisSpacing),
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing),
        content = { content() }
    )
}
