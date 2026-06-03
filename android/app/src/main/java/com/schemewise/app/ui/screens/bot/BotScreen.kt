package com.schemewise.app.ui.screens.bot

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*
import kotlinx.coroutines.launch

/** Full BotScreen matching web BotAgentPage.jsx — AI Chat tab + SHIELD scam detector tab */
@Composable
fun BotScreen(
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    initialQuery: String = "",
    viewModel: BotViewModel = hiltViewModel()
) {
    val messages  by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()
    var input      by remember { mutableStateOf("") }
    var activeTab  by remember { mutableStateOf("chat") } // "chat" | "shield"

    // Auto-fire context query when arriving from SchemeDetail
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank() && messages.isEmpty()) {
            viewModel.sendMessage(initialQuery)
        }
    }

    // Shield state
    var scamInput    by remember { mutableStateOf("") }
    var shieldLoading by remember { mutableStateOf(false) }
    var shieldResult  by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty() || isLoading) listState.animateScrollToItem(0)
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            Column {
                SchemeWiseTopBar(
                    title    = if (activeTab == "chat") "AI Assistant" else "SHIELD Scam Firewall",
                    subtitle = if (activeTab == "chat") "Powered by SchemeWise AI" else "Paste suspicious messages to verify",
                    onSettingsClick = onSettingsClick,
                    onProfileClick  = onProfileClick
                )
                // Tab switcher — orange pill style matching web
                Surface(
                    color     = Surface,
                    shadowElevation = 0.dp,
                    modifier  = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TabChip(
                            label    = "AI Assistant",
                            selected = activeTab == "chat",
                            icon     = Icons.Filled.SmartToy,
                            onClick  = { activeTab = "chat" },
                            modifier = Modifier.weight(1f)
                        )
                        TabChip(
                            label    = "SHIELD",
                            selected = activeTab == "shield",
                            icon     = Icons.Filled.Shield,
                            onClick  = { activeTab = "shield" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    HorizontalDivider(color = Border)
                }
            }
        },
        bottomBar = {
            if (activeTab == "chat") {
                Surface(shadowElevation = 12.dp, color = Surface) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value         = input,
                            onValueChange = { input = it },
                            placeholder   = { Text("Type your query...", color = Muted) },
                            modifier      = Modifier.weight(1f),
                            singleLine    = true,
                            shape         = RoundedCornerShape(100),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Brand500,
                                unfocusedBorderColor = Border,
                                focusedContainerColor   = Surface2,
                                unfocusedContainerColor = Surface2,
                            )
                        )
                        FloatingActionButton(
                            onClick = {
                                if (input.isNotBlank()) {
                                    viewModel.sendMessage(input)
                                    input = ""
                                    scope.launch { listState.animateScrollToItem(0) }
                                }
                            },
                            containerColor = Brand500,
                            modifier       = Modifier.size(50.dp),
                            shape          = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.Send, "Send", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (activeTab == "chat") {
            LazyColumn(
                state          = listState,
                modifier       = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                reverseLayout  = true,
            ) {
                if (isLoading) {
                    item { TypingDotsIndicator() }
                }
                items(messages.reversed()) { msg ->
                    Box(modifier = Modifier.padding(bottom = 10.dp)) {
                        ChatBubble(role = msg.role, content = msg.content)
                    }
                }
                if (messages.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Surface(
                                color = Brand50,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.SmartToy, null, tint = Brand500, modifier = Modifier.size(36.dp))
                                }
                            }
                            Text("Hello! I am SchemeWise AI", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSurface)
                            Text(
                                "Analyzing the live directory of central and state welfare programs.",
                                color = Muted, fontSize = 13.sp, textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            }
        } else {
            // SHIELD Tab
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(20.dp),
                    colors    = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.SearchOff, null, tint = Brand500, modifier = Modifier.size(18.dp))
                            Text("Analyze a Message", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        OutlinedTextField(
                            value = scamInput,
                            onValueChange = { scamInput = it },
                            placeholder = { Text("Paste the SMS, WhatsApp forward, or claim text here to verify...", color = Muted) },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brand500,
                                unfocusedBorderColor = Border,
                                focusedContainerColor = Surface2,
                                unfocusedContainerColor = Surface2,
                            ),
                            maxLines = 6,
                        )
                        Button(
                            onClick  = { /* shield scan handled via viewModel if wired */ },
                            enabled  = scamInput.isNotBlank() && !shieldLoading,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Brand500),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            if (shieldLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Analyzing...", fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Filled.Shield, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Run Fact-Check", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Placeholder verdict card when result arrives
                shieldResult?.let {
                    ShieldResultCard(it)
                } ?: run {
                    // Info card
                    Card(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = BadgeBlueBg),
                        border    = androidx.compose.foundation.BorderStroke(1.dp, BadgeBlueText.copy(.2f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Filled.Info, null, tint = BadgeBlueText, modifier = Modifier.size(20.dp))
                            Column {
                                Text("How SHIELD works", fontWeight = FontWeight.Bold, color = BadgeBlueText, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Paste a suspicious government scheme claim. SHIELD cross-verifies it against our verified scheme database and flags scam patterns using AI.",
                                    color = BadgeBlueText.copy(.8f), fontSize = 12.sp, lineHeight = 17.sp
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
private fun TabChip(
    label: String, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        modifier  = modifier.height(38.dp),
        color     = if (selected) Brand500 else Background,
        shape     = RoundedCornerShape(10.dp),
        onClick   = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (selected) Color.White else Muted, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                label,
                color      = if (selected) Color.White else Muted,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize   = 13.sp
            )
        }
    }
}

@Composable
private fun TypingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier.padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            color = Background,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            tween(400, delayMillis = i * 150),
                            RepeatMode.Reverse
                        ),
                        label = "dot$i"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .alpha(alpha)
                            .background(Muted, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(role: String, content: String) {
    val isUser = role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) Brand500 else Surface,
            shape = RoundedCornerShape(
                topStart    = 18.dp, topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd   = if (isUser) 4.dp else 18.dp,
            ),
            modifier        = Modifier.widthIn(max = 290.dp),
            shadowElevation = if (isUser) 0.dp else 2.dp,
        ) {
            Text(
                text     = content,
                color    = if (isUser) Color.White else OnSurface,
                fontSize = 14.sp, lineHeight = 21.sp,
                modifier = Modifier.padding(12.dp, 10.dp),
            )
        }
    }
}

@Composable
private fun ShieldResultCard(result: Map<String, Any>) {
    val verdict = result["verdict"] as? String ?: "UNKNOWN"
    val summary = result["summary"] as? String ?: ""
    val advice  = result["advice"]  as? String ?: ""
    val (borderColor, bgColor, textColor) = when (verdict) {
        "SCAM"       -> Triple(BadgeRedText, BadgeRedBg, BadgeRedText)
        "SUSPICIOUS" -> Triple(BadgeAmberText, BadgeAmberBg, BadgeAmberText)
        else         -> Triple(BadgeGreenText, BadgeGreenBg, BadgeGreenText)
    }
    val icon = when (verdict) {
        "SCAM"       -> Icons.Filled.Warning
        "SUSPICIOUS" -> Icons.Filled.Shield
        else         -> Icons.Filled.CheckCircle
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = bgColor),
        border   = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, null, tint = textColor, modifier = Modifier.size(32.dp))
                Column {
                    Text("$verdict DETECTED", fontWeight = FontWeight.Black, fontSize = 20.sp, color = textColor)
                    Text(summary, color = textColor.copy(.7f), fontSize = 13.sp)
                }
            }
            if (advice.isNotEmpty()) {
                HorizontalDivider(color = borderColor.copy(.2f))
                Text("Recommended Action", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor.copy(.6f), letterSpacing = 0.5.sp)
                Text(advice, fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
            }
        }
    }
}
