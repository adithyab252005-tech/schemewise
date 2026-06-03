package com.schemewise.app.ui.screens.tracker

import android.content.Context
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

private val APPLICATION_STEPS = listOf(
    "Read scheme eligibility criteria",
    "Gather required documents",
    "Fill application form (online / offline)",
    "Submit application",
    "Payment / verification done",
    "Received acknowledgement / receipt",
    "Scheme benefit received",
)

private data class TrackedApp(
    val id: Long,
    val schemeName: String,
    val addedAt: String,
    val ticked: Set<Int>,
)

private fun loadApps(ctx: Context): List<TrackedApp> {
    val raw = ctx.getSharedPreferences("tracker_prefs", Context.MODE_PRIVATE)
        .getString("tracked_applications", "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            val tickedArr = obj.getJSONArray("ticked")
            val ticked = (0 until tickedArr.length()).map { tickedArr.getInt(it) }.toSet()
            TrackedApp(
                id = obj.getLong("id"),
                schemeName = obj.getString("schemeName"),
                addedAt = obj.getString("addedAt"),
                ticked = ticked,
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveApps(ctx: Context, apps: List<TrackedApp>) {
    val arr = JSONArray()
    apps.forEach { app ->
        val obj = JSONObject()
        obj.put("id", app.id)
        obj.put("schemeName", app.schemeName)
        obj.put("addedAt", app.addedAt)
        val ticked = JSONArray()
        app.ticked.forEach { ticked.put(it) }
        obj.put("ticked", ticked)
        arr.put(obj)
    }
    ctx.getSharedPreferences("tracker_prefs", Context.MODE_PRIVATE)
        .edit().putString("tracked_applications", arr.toString()).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsTrackerScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    var apps by remember { mutableStateOf(loadApps(ctx)) }
    var showForm by remember { mutableStateOf(false) }
    var newScheme by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Long?>(null) }

    fun persistAndSet(updated: List<TrackedApp>) {
        apps = updated
        saveApps(ctx, updated)
    }

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title = "My Applications",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.Add, "Add", tint = Brand500)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Info subtitle ────────────────────────────────────────────
            item {
                Text(
                    "Track your own scheme application progress step by step.",
                    fontSize = 13.sp, color = Muted, fontWeight = FontWeight.Medium
                )
            }

            // ── Add Form ─────────────────────────────────────────────────
            item {
                AnimatedVisibility(visible = showForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Brand500.copy(0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Add New Application", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Brand600)
                            OutlinedTextField(
                                value = newScheme,
                                onValueChange = { newScheme = it },
                                placeholder = { Text("e.g. PM Vishwakarma Yojana…", color = Muted, fontSize = 14.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Brand500,
                                    unfocusedBorderColor = Border,
                                )
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showForm = false; newScheme = "" },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Muted),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                                ) { Text("Cancel", fontWeight = FontWeight.Bold) }

                                Button(
                                    onClick = {
                                        if (newScheme.isNotBlank()) {
                                            val entry = TrackedApp(
                                                id = System.currentTimeMillis(),
                                                schemeName = newScheme.trim(),
                                                addedAt = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date()),
                                                ticked = emptySet()
                                            )
                                            persistAndSet(listOf(entry) + apps)
                                            expandedId = entry.id
                                            newScheme = ""
                                            showForm = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                                    shape = RoundedCornerShape(10.dp)
                                ) { Text("Add", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }

            // ── Empty State ───────────────────────────────────────────────
            if (apps.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(40.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Filled.Assignment, null, tint = Brand500, modifier = Modifier.size(52.dp))
                            Text("No applications tracked yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)
                            Text(
                                "Add a scheme you are applying for and tick off your progress at each step.",
                                fontSize = 13.sp, color = Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { showForm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Add Your First Application", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Application Cards ─────────────────────────────────────────
            items(apps, key = { it.id }) { app ->
                val progress = if (APPLICATION_STEPS.isEmpty()) 100 else
                    (app.ticked.size * 100 / APPLICATION_STEPS.size)
                val isExpanded = expandedId == app.id
                val progressColor = when {
                    progress >= 100 -> Color(0xFF22C55E)
                    progress > 0    -> Color(0xFFF97316)
                    else            -> Border
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (progress >= 100) Color(0xFF22C55E).copy(0.4f) else Border
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Header row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedId = if (isExpanded) null else app.id }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                Text(app.addedAt, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = Muted, letterSpacing = 0.5.sp)
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    app.schemeName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = OnSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = when {
                                        progress >= 100 -> Color(0xFFDCFCE7)
                                        progress > 0    -> Color(0xFFFFF7ED)
                                        else            -> Background
                                    },
                                    shape = RoundedCornerShape(100)
                                ) {
                                    Text(
                                        if (progress >= 100) "✓ Done" else "$progress%",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = when {
                                            progress >= 100 -> Color(0xFF15803D)
                                            progress > 0    -> Color(0xFFC2410C)
                                            else            -> Muted
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        persistAndSet(apps.filter { it.id != app.id })
                                        if (expandedId == app.id) expandedId = null
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, "Remove", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                                }
                                Icon(
                                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    null, tint = Muted, modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Progress bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(100))
                                .background(Border)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress / 100f)
                                    .fillMaxHeight()
                                    .background(progressColor, RoundedCornerShape(100))
                            )
                        }

                        // Expanded checklist
                        AnimatedVisibility(visible = isExpanded) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "APPLICATION STEPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Muted,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                APPLICATION_STEPS.forEachIndexed { idx, step ->
                                    val isTicked = idx in app.ticked
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val newTicked = if (isTicked) app.ticked - idx else app.ticked + idx
                                                persistAndSet(apps.map {
                                                    if (it.id == app.id) it.copy(ticked = newTicked) else it
                                                })
                                            }
                                            .padding(vertical = 8.dp, horizontal = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            if (isTicked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                            null,
                                            tint = if (isTicked) Brand500 else Muted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            step,
                                            fontSize = 14.sp,
                                            color = if (isTicked) Muted else OnSurface,
                                            textDecoration = if (isTicked) TextDecoration.LineThrough else TextDecoration.None
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "${app.ticked.size}/${APPLICATION_STEPS.size} steps completed",
                                    fontSize = 11.sp, color = Muted, fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
