package com.schemewise.app.ui.screens.centers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.Profile
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*

data class CenterType(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val bgHint: Color,
    val queryBase: String,
    val tip: String
)

val CENTER_TYPES = listOf(
    CenterType("eseva", "e-Seva / CSC", Icons.Filled.Business, Color(0xFFF97316), Color(0xFFFFF7ED), "e-Seva center", "Apply for Aadhaar, PAN, pension, PMAY and 300+ govt services here."),
    CenterType("panchayat", "Gram Panchayat", Icons.Filled.AccountBalance, Color(0xFF10B981), Color(0xFFECFDF5), "Gram Panchayat office", "Physical panchayat office for rural scheme applications and certificates."),
    CenterType("collector", "Collector / Tahsildar", Icons.Filled.Description, Color(0xFF3B82F6), Color(0xFFEFF6FF), "District Collector office", "Revenue certificates, caste certificates, income certificates issued here."),
    CenterType("ration", "Ration / PDS", Icons.Filled.Store, Color(0xFFA855F7), Color(0xFFFAF5FF), "ration shop PDS", "Public Distribution System shop for BPL ration card and food entitlements.")
)

fun buildMapQuery(type: CenterType, profile: Profile?): String {
    val parts = listOfNotNull(profile?.state, profile?.ruralUrban).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "${type.queryBase} near me in India"
    return "${type.queryBase} near ${parts.joinToString(", ")}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentersMapScreen(
    onBack: () -> Unit,
    viewModel: CentersMapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(CENTER_TYPES.first()) }
    
    val locationLabel = remember(uiState.profile) {
        val p = uiState.profile
        listOfNotNull(p?.ruralUrban, p?.state).filter { it.isNotBlank() }.joinToString(", ").ifEmpty { "Unknown Location" }
    }
    val hasLocation = uiState.profile?.state?.isNotBlank() == true

    Scaffold(
        topBar = {
            SchemeWiseTopBar(
                title = "Assistance Centers",
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") }
                }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand500)
            }
            return@Scaffold
        }

        if (!hasLocation) {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, WarningAmber.copy(0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)).background(WarningAmber.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Warning, null, tint = WarningAmber, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(16.dp))
                        Text("Location Not Set", fontWeight = FontWeight.Black, fontSize = 20.sp, color = OnSurface)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "To show nearby e-Seva centres and Panchayat offices, we need your state from your profile.",
                            color = Muted, fontSize = 14.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            return@Scaffold
        }

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Location
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.Place, null, tint = Brand500, modifier = Modifier.size(16.dp))
                Text("Showing results near", color = Muted, fontSize = 13.sp)
                Text(locationLabel, fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 13.sp)
            }

            // Categories Selector
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(CENTER_TYPES) { type ->
                    val isSelected = selectedType.id == type.id
                    val bgColor = if (isSelected) type.bgHint else Color.White
                    val borderColor = if (isSelected) type.color.copy(0.3f) else Border

                    Surface(
                        onClick = { selectedType = type },
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(type.icon, null, tint = if (isSelected) type.color else Muted, modifier = Modifier.size(16.dp))
                            Text(
                                type.label,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) type.color else Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Tip Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = selectedType.bgHint,
                border = BorderStroke(1.dp, selectedType.color.copy(0.2f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Info, null, tint = selectedType.color, modifier = Modifier.size(18.dp))
                    Text(selectedType.tip, color = selectedType.color.copy(alpha = 0.9f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Map Intent Card
            val query = buildMapQuery(selectedType, uiState.profile)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("SEARCHING FOR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Muted, letterSpacing = 1.sp)
                        Text("\"$query\"", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnSurface, modifier = Modifier.padding(top = 4.dp))
                    }
                    
                    Button(
                        onClick = {
                            val mapIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                // Fallback to browser
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(query)}"))
                                context.startActivity(browserIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Map, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open in Google Maps", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // National Helplines
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Phone, null, tint = Muted, modifier = Modifier.size(14.dp))
                        Text("NATIONAL HELPLINES", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Muted, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    val helplines = listOf(
                        "CSC / e-Seva" to "1800-121-3468",
                        "PM Kisan" to "155261",
                        "Grievance" to "1800-11-8111",
                        "Ayushman Bharat" to "14555"
                    )
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(140.dp),
                        userScrollEnabled = false
                    ) {
                        items(helplines) { (name, number) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Border),
                                modifier = Modifier.clickable {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                                    context.startActivity(dialIntent)
                                }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Muted)
                                    Text(number, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Brand600)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
