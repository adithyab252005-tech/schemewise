package com.schemewise.app.ui.screens.profile

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.SubscriptionActivity
import com.schemewise.app.ui.components.ShimmerBox
import com.schemewise.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onComboBenefits: () -> Unit = {},
    onCivicReadiness: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val prefs = viewModel.prefs
    var showLogoutDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isPremium = remember {
        context.getSharedPreferences("subscription_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("is_premium_user", false)
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onBack()
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.Filled.Logout, null, tint = BadgeRedText) },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text  = { Text("Are you sure you want to sign out of your SchemeWise account?", color = Muted) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; viewModel.logout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = BadgeRedText),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Sign Out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }, shape = RoundedCornerShape(10.dp)) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold { padding ->
        if (state.isLoading) {
            Column(modifier = Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp), radius = 0.dp)
                repeat(4) { ShimmerBox(modifier = Modifier.fillMaxWidth(), height = 56.dp) }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding).fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Gradient header
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(NavyDark, NavyLight)))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(3.dp).align(Alignment.TopCenter)) {
                        Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF97316)))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(.3f)))
                        Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF22C55E)))
                    }
                    Row(
                        modifier = Modifier.padding(20.dp, 24.dp, 20.dp, 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(24.dp))
                                    .background(Color.White.copy(.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text((prefs.userName?.take(1) ?: "U").uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Column {
                                Text(prefs.userName ?: "User", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text("Citizen Account", color = Color.White.copy(.6f), fontSize = 12.sp)
                                Spacer(Modifier.height(6.dp))
                                Surface(color = Brand500.copy(.2f), shape = RoundedCornerShape(6.dp)) {
                                    Text("VERIFIED", color = Brand300, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp, modifier = Modifier.padding(8.dp, 3.dp))
                                }
                            }
                        }
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Filled.Logout, "Logout", tint = Color.White.copy(.7f))
                        }
                    }
                }

                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val p = state.profile

                    // ── Personal Info Card ────────────────────────────────────
                    // Sanitize category — reject UUID hashes or unrecognized values
                    val validCategories = setOf("General", "OBC", "SC", "ST", "Minority", "Other")
                    val displayCategory = if (p?.category != null &&
                        validCategories.any { p.category.equals(it, ignoreCase = true) }
                    ) p.category else "Not set"

                    ProfileCard(title = "Personal Info", onEdit = onEditClick) {
                        profileRows(
                            Triple(Icons.Filled.Person,         "Name",          p?.name ?: "Not set"),
                            Triple(Icons.Filled.Cake,           "Age",           p?.age?.toInt()?.toString() ?: "Not set"),
                            Triple(Icons.Filled.Person,         "Gender",        p?.gender ?: "Not set"),
                            Triple(Icons.Filled.Favorite,       "Marital Status", p?.maritalStatus ?: "Not set"),
                            Triple(Icons.Filled.Group,          "Category",      displayCategory),
                        )
                    }

                    // ── Location Card ─────────────────────────────────────────
                    ProfileCard(title = "Location") {
                        profileRows(
                            Triple(Icons.Filled.LocationOn,    "State",    p?.state ?: "Not set"),
                            Triple(Icons.Filled.LocationCity,  "District", p?.district ?: "Not set"),
                            Triple(Icons.Filled.Home,          "City",     p?.city ?: "Not set"),
                            Triple(Icons.Filled.Place,         "Area",     p?.area ?: "Not set"),
                            Triple(Icons.Filled.Landscape,     "Area Type", p?.ruralUrban ?: "Not set"),
                        )
                    }

                    // ── Economic Info Card ────────────────────────────────────
                    ProfileCard(title = "Economic Details") {
                        profileRows(
                            Triple(Icons.Filled.Work,           "Occupation",      p?.occupation ?: "Not set"),
                            Triple(Icons.Filled.BusinessCenter, "Employment Type", p?.employmentType ?: "Not set"),
                            Triple(Icons.Filled.AccountBalance, "Annual Income",   if (p?.income != null && p.income > 0) "₹${p.income.toLong()}" else "Not set"),
                            Triple(Icons.Filled.Agriculture,    "Farmer",          if (p?.isFarmer == "Yes") "✓ Yes" else "No"),
                            Triple(Icons.Filled.MoneyOff,       "BPL Family",      if (p?.isBPL == "Yes") "✓ Yes" else "No"),
                            Triple(Icons.Filled.AccessibleForward, "Differently Abled", if (p?.isDifferentlyAbled == "Yes") "✓ Yes" else "No"),
                        )
                    }

                    // ── Student Details Card (conditional) ───────────────────
                    if (p?.isStudent == "Yes") {
                        ProfileCard(title = "🎓 Student Details") {
                            profileRows(
                                Triple(Icons.Filled.School,    "Study Level",  when (p.studentLevel) {
                                    "school" -> "School (Class 1–12)"
                                    "college" -> "College (UG / Diploma)"
                                    "university" -> "University (PG / PhD)"
                                    else -> p.studentLevel ?: "Not set"
                                }),
                            )
                            if (p.studentLevel == "school" && p.studentClass != null) {
                                profileRows(Triple(Icons.Filled.Class, "Class / Grade", when (p.studentClass) {
                                    "1-5" -> "Class 1–5 (Primary)"
                                    "6-8" -> "Class 6–8 (Middle)"
                                    "9-10" -> "Class 9–10 (Secondary)"
                                    "11-12" -> "Class 11–12 (Higher Secondary)"
                                    else -> p.studentClass
                                }))
                            }
                            if (p.studentLevel != "school") {
                                if (p.studentDegreeType != null) {
                                    profileRows(Triple(Icons.Filled.MenuBook, "Degree Type", when (p.studentDegreeType) {
                                        "diploma" -> "Diploma / Certificate"
                                        "ug" -> "UG – Bachelor's"
                                        "pg" -> "PG – Master's"
                                        "phd" -> "PhD / Doctoral"
                                        "professional" -> "Professional (MBBS/LLB/CA)"
                                        else -> p.studentDegreeType
                                    }))
                                }
                                if (p.studentCourse != null) {
                                    profileRows(Triple(Icons.Filled.AutoStories, "Course / Stream", when (p.studentCourse) {
                                        "engineering" -> "Engineering & Technology"
                                        "medical" -> "Medical & Health Sciences"
                                        "science" -> "Pure Sciences"
                                        "arts" -> "Arts & Humanities"
                                        "commerce" -> "Commerce & Management"
                                        "law" -> "Law (LLB / LLM)"
                                        "agriculture" -> "Agriculture"
                                        "education" -> "Education / B.Ed"
                                        "pharmacy" -> "Pharmacy"
                                        "nursing" -> "Nursing / Paramedical"
                                        "polytechnic" -> "Polytechnic / ITI"
                                        else -> p.studentCourse
                                    }))
                                }
                            }
                        }
                    }

                    Button(onClick = onEditClick, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = Brand500), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    // ── Feature Access Cards (mirrors web Profile sidebar) ──
                    Text("QUICK ACCESS", fontSize = 10.sp, fontWeight = FontWeight.Black,
                        color = Muted, letterSpacing = 1.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))

                    data class ProfileLink(
                        val icon: androidx.compose.ui.graphics.vector.ImageVector,
                        val title: String,
                        val subtitle: String,
                        val onClick: () -> Unit,
                        val color: Color,
                    )
                    val featureLinks = listOf(
                        ProfileLink(Icons.Filled.FamilyRestroom, "Household Benefits",  "Unlock combo schemes for your family", onComboBenefits, Color(0xFFF59E0B)),
                        ProfileLink(Icons.Filled.VerifiedUser,   "Civic Readiness",     "Track your document compliance index",  onCivicReadiness, Color(0xFF22C55E)),
                    )
                    featureLinks.forEach { link ->
                        Card(
                            onClick   = link.onClick,
                            modifier  = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape     = RoundedCornerShape(18.dp),
                            colors    = CardDefaults.cardColors(containerColor = Surface),
                            elevation = CardDefaults.cardElevation(2.dp),
                            border    = BorderStroke(1.dp, Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(link.color.copy(0.15f), link.color.copy(0.05f))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(link.icon, null, tint = link.color, modifier = Modifier.size(24.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(link.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = OnSurface)
                                    Text(link.subtitle, fontSize = 11.sp, color = Muted, lineHeight = 14.sp)
                                }
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF8FAFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.ChevronRight, null, tint = Muted.copy(0.5f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    // ── Premium Banner ────────────────────────────────────────
                    Card(
                        onClick = { context.startActivity(Intent(context, SubscriptionActivity::class.java)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(18.dp),
                        colors   = CardDefaults.cardColors(containerColor = Color.Transparent),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFA78BFA))),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text("✨", fontSize = 28.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (isPremium) "Premium Active ✓" else "Upgrade to Premium",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        if (isPremium) "You have all premium features" else "Ad-free experience · ₹100/month",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(0.8f)
                                    )
                                }
                                if (!isPremium) {
                                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
                                        Text(
                                            "Get",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6C5CE7),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = BadgeRedText), border = androidx.compose.foundation.BorderStroke(1.dp, BadgeRedText.copy(.3f)), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(16.dp))

                }
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Brand50), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Brand500, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 0.5.sp)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
        }
    }
}

@Composable
fun ProfileCard(title: String, onEdit: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Surface), elevation = CardDefaults.cardElevation(1.dp)) {
        Column {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 15.sp, color = OnSurface)
                if (onEdit != null) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Filled.Edit, "Edit", tint = Brand500, modifier = Modifier.size(16.dp))
                    }
                }
            }
            HorizontalDivider(color = Border)
            content()
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun profileRows(vararg rows: Triple<ImageVector, String, String>) {
    Column {
        rows.forEachIndexed { i, (icon, label, value) ->
            ProfileDetailRow(icon = icon, label = label, value = value)
            if (i < rows.lastIndex) HorizontalDivider(color = Border.copy(.4f), modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}
