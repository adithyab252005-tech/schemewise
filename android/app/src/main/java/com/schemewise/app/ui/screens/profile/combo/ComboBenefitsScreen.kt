package com.schemewise.app.ui.screens.profile.combo

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

private data class FamilyMember(
    val id: Long,
    val name: String,
    val relation: String,
    val age: String,
    val gender: String,
    val category: String,
    val income: String,
    val isBpl: Boolean,
    val isStudent: Boolean,
)

private fun loadMembers(ctx: Context, userId: Int): List<FamilyMember> {
    val raw = ctx.getSharedPreferences("combo_prefs", Context.MODE_PRIVATE)
        .getString("family_$userId", "[]") ?: "[]"
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            FamilyMember(
                id       = o.getLong("id"),
                name     = o.getString("name"),
                relation = o.getString("relation"),
                age      = o.optString("age", ""),
                gender   = o.optString("gender", "Male"),
                category = o.optString("category", "General"),
                income   = o.optString("income", ""),
                isBpl    = o.optBoolean("isBpl", false),
                isStudent = o.optBoolean("isStudent", false),
            )
        }
    } catch (e: Exception) { emptyList() }
}

private fun saveMembers(ctx: Context, userId: Int, members: List<FamilyMember>) {
    val arr = JSONArray()
    members.forEach { m ->
        val o = JSONObject().apply {
            put("id", m.id); put("name", m.name); put("relation", m.relation)
            put("age", m.age); put("gender", m.gender); put("category", m.category)
            put("income", m.income); put("isBpl", m.isBpl); put("isStudent", m.isStudent)
        }
        arr.put(o)
    }
    ctx.getSharedPreferences("combo_prefs", Context.MODE_PRIVATE)
        .edit().putString("family_$userId", arr.toString()).apply()
}

// Fixed household combo schemes (mirroring web ComboBenefitsPage)
private data class ComboScheme(
    val id: Int, val title: String, val tag: String,
    val amount: String, val members: Int, val color: Color
)
private val householdCombos = listOf(
    ComboScheme(1, "Family Healthcare Alliance (PM-JAY)", "Medical",   "₹5,00,000",     3, Color(0xFFF43F5E)),
    ComboScheme(2, "Agricultural Household Subsidy",      "Farming",   "₹12,000 / yr",  2, Color(0xFF22C55E)),
    ComboScheme(3, "First-Generation Scholar Sync",       "Education", "Tuition Waiver", 2, Color(0xFF6366F1)),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComboBenefitsScreen(onBack: () -> Unit) {
    val ctx        = LocalContext.current
    val prefs      = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val userId     = prefs.getInt("user_id", 0)

    var members    by remember { mutableStateOf(loadMembers(ctx, userId)) }
    var showForm   by remember { mutableStateOf(false) }

    // Form state
    var fName      by remember { mutableStateOf("") }
    var fRelation  by remember { mutableStateOf("Spouse") }
    var fAge       by remember { mutableStateOf("") }
    var fGender    by remember { mutableStateOf("Male") }
    var fCategory  by remember { mutableStateOf("General") }
    var fIncome    by remember { mutableStateOf("") }
    var fBpl       by remember { mutableStateOf(false) }
    var fStudent   by remember { mutableStateOf(false) }

    fun resetForm() { fName=""; fRelation="Spouse"; fAge=""; fGender="Male"; fCategory="General"; fIncome=""; fBpl=false; fStudent=false }

    fun addMember() {
        if (fName.isBlank()) return
        val updated = members + FamilyMember(
            id = System.currentTimeMillis(), name = fName.trim(), relation = fRelation,
            age = fAge, gender = fGender, category = fCategory, income = fIncome,
            isBpl = fBpl, isStudent = fStudent
        )
        members = updated
        saveMembers(ctx, userId, updated)
        resetForm()
        showForm = false
    }

    fun removeMember(id: Long) {
        val updated = members.filter { it.id != id }
        members = updated
        saveMembers(ctx, userId, updated)
    }

    val totalSchemes = 12 + members.sumOf { m ->
        var bonus = 1
        if (m.isBpl) bonus += 2
        if (m.isStudent) bonus += 1
        if (m.category in listOf("SC","ST","OBC","Minority")) bonus += 1
        bonus
    }

    val relations = listOf("Spouse","Child","Parent","Sibling","Other")
    val genders   = listOf("Male","Female","Transgender")
    val castes    = listOf("General","OBC","SC","ST","Minority")

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title = "Household Benefits",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = OnSurface)
                    }
                },
                actions = {
                    IconButton(onClick = { showForm = !showForm }) {
                        Icon(Icons.Filled.PersonAdd, "Add Member", tint = Brand500)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── Hero Card ────────────────────────────────────────────────
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
                            .background(Brush.linearGradient(listOf(Color(0xFF78350F), Color(0xFFF97316))), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Surface(color = Color(0xFFF59E0B).copy(0.2f), shape = RoundedCornerShape(8.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.Groups, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(11.dp))
                                    Text("Family Link Active", color = Color(0xFFF59E0B),
                                        fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                                }
                            }
                            Text("Household\nBenefits", color = Color.White,
                                fontSize = 28.sp, fontWeight = FontWeight.Black, lineHeight = 32.sp)
                            Text(
                                "Link family members to unlock combined household schemes like Family Healthcare and Ration Cards.",
                                color = Color.White.copy(0.7f), fontSize = 12.sp, lineHeight = 17.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$totalSchemes", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFFFBBF24))
                                Text("Total Combined Schemes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFBBF24).copy(0.8f))
                            }
                        }
                    }
                }
            }

            // ── Add Member Form ───────────────────────────────────────────
            item {
                AnimatedVisibility(visible = showForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Add Family Member", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnSurface)

                            val tfColors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Brand500, unfocusedBorderColor = Border
                            )

                            OutlinedTextField(
                                value = fName, onValueChange = { fName = it },
                                label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
                                singleLine = true, shape = RoundedCornerShape(10.dp), colors = tfColors
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Relation dropdown
                                var relExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = relExpanded, onExpandedChange = { relExpanded = it }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = fRelation, onValueChange = {}, readOnly = true,
                                        label = { Text("Relation") }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(relExpanded) },
                                        shape = RoundedCornerShape(10.dp), colors = tfColors)
                                    ExposedDropdownMenu(expanded = relExpanded, onDismissRequest = { relExpanded = false }) {
                                        relations.forEach { r -> DropdownMenuItem(text = { Text(r) }, onClick = { fRelation = r; relExpanded = false }) }
                                    }
                                }
                                OutlinedTextField(
                                    value = fAge, onValueChange = { fAge = it },
                                    label = { Text("Age") }, modifier = Modifier.weight(1f),
                                    singleLine = true, shape = RoundedCornerShape(10.dp), colors = tfColors,
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                // Gender dropdown
                                var genderExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = fGender, onValueChange = {}, readOnly = true,
                                        label = { Text("Gender") }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                                        shape = RoundedCornerShape(10.dp), colors = tfColors)
                                    ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                                        genders.forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { fGender = g; genderExpanded = false }) }
                                    }
                                }
                                // Category dropdown
                                var catExpanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(value = fCategory, onValueChange = {}, readOnly = true,
                                        label = { Text("Category") }, modifier = Modifier.menuAnchor().fillMaxWidth(),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                                        shape = RoundedCornerShape(10.dp), colors = tfColors)
                                    ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                        castes.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { fCategory = c; catExpanded = false }) }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = fIncome, onValueChange = { fIncome = it },
                                label = { Text("Annual Income (₹)") }, modifier = Modifier.fillMaxWidth(),
                                singleLine = true, shape = RoundedCornerShape(10.dp), colors = tfColors,
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = fBpl, onCheckedChange = { fBpl = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Brand500))
                                    Text("BPL", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = fStudent, onCheckedChange = { fStudent = it },
                                        colors = CheckboxDefaults.colors(checkedColor = Brand500))
                                    Text("Student", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(onClick = { showForm = false; resetForm() }, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                                    Text("Cancel", fontWeight = FontWeight.Bold)
                                }
                                Button(onClick = ::addMember, modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                                    shape = RoundedCornerShape(10.dp)) {
                                    Text("Add Member", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Household Members ─────────────────────────────────────────
            if (members.isNotEmpty()) {
                item {
                    Text("LINKED FAMILY MEMBERS", fontSize = 10.sp, fontWeight = FontWeight.Black,
                        color = Muted, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 4.dp))
                }
                items(members, key = { it.id }) { m ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF7ED)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(m.name.take(1).uppercase(), fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFF97316))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(m.relation, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                                    if (m.age.isNotBlank()) Text("· ${m.age} yrs", fontSize = 11.sp, color = Muted)
                                    if (m.category != "General") {
                                        Surface(color = Brand50, shape = RoundedCornerShape(4.dp)) {
                                            Text(m.category, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                                color = Brand600, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { removeMember(m.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, "Remove", tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            } else {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.FamilyRestroom, null, tint = Brand500, modifier = Modifier.size(52.dp))
                            Text("No family members linked yet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface)
                            Text("Add members to unlock combined household benefit schemes.",
                                fontSize = 12.sp, color = Muted, textAlign = TextAlign.Center)
                            Button(onClick = { showForm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                                shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Filled.PersonAdd, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add First Member", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Combined Scheme Cards ─────────────────────────────────────
            item {
                Text("HOUSEHOLD COMBO SCHEMES", fontSize = 10.sp, fontWeight = FontWeight.Black,
                    color = Muted, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 4.dp))
            }
            items(householdCombos) { combo ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, combo.color.copy(0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Surface(color = combo.color.copy(0.1f), shape = RoundedCornerShape(6.dp)) {
                                Text(combo.tag, color = combo.color, fontSize = 10.sp, fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                            Surface(color = Background, shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Border)) {
                                Text("Requires ${combo.members} members", fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = Muted, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        Text(combo.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface, lineHeight = 20.sp)
                        HorizontalDivider(color = Border)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("YIELD", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Muted, letterSpacing = 1.sp)
                                Text(combo.amount, fontWeight = FontWeight.Black, fontSize = 16.sp, color = OnSurface)
                            }
                            val isEligible = members.size >= combo.members
                            Surface(
                                color = if (isEligible) Color(0xFFDCFCE7) else Color(0xFFFFF7ED),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (isEligible) "✓ Eligible" else "Need ${combo.members - members.size} more",
                                    color = if (isEligible) Color(0xFF15803D) else Color(0xFFC2410C),
                                    fontSize = 11.sp, fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── How it works ─────────────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Background), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.HelpOutline, null, tint = Muted, modifier = Modifier.size(22.dp))
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("How do household benefits work?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface)
                            Text(
                                "Certain government schemes (like ration frameworks and health coverage) require linked household profiles. By ensuring all family members are added, the engine can find special packages meant for your entire family.",
                                fontSize = 12.sp, color = Muted, lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
