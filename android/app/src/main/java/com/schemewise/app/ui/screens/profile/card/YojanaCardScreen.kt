package com.schemewise.app.ui.screens.profile.card

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.schemewise.app.ui.screens.profile.ProfileViewModel
import com.schemewise.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YojanaCardScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Yojana Card", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Brand500)
            }
            return@Scaffold
        }

        val profile = state.profile
        if (profile == null) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Shield, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("No Profile Found", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Update your profile first.", color = Muted)
                }
            }
            return@Scaffold
        }

        val qrData = """
SCHEMEWISE DIGITAL PASS
Name: ${profile.name ?: "N/A"}
DOB: ${profile.dob ?: "N/A"} (Age: ${profile.age?.toInt() ?: "N/A"})
Category: ${profile.category ?: "N/A"}
Income: ₹${profile.income?.toLong() ?: 0}
Sector: ${profile.ruralUrban ?: "Unknown"}, ${profile.state ?: "Unknown"}
Occupation: ${profile.occupation ?: "N/A"}
        """.trimIndent()

        var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

        LaunchedEffect(qrData) {
            try {
                val encoder = BarcodeEncoder()
                qrBitmap = encoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 600, 600)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text("Digital Yojana Card", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Brand900)
            Text(
                "Show this QR code at local government centers for instant eligibility verification.",
                fontSize = 14.sp,
                color = Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(32.dp))

            // The Card Box
            Box(modifier = Modifier.fillMaxWidth()) {
                // Glow effect backplate
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(bottom = 12.dp)
                        .background(brush = Brush.horizontalGradient(GradElectricSunset), shape = RoundedCornerShape(32.dp))
                        .blur(24.dp)
                )

                // Foreground Card
                Card(
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header Strip
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF1E293B))
                                .padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("MEMBER PASS", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(profile.name ?: "Guest", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                            }
                            Icon(Icons.Filled.Shield, null, tint = Brand500.copy(0.4f), modifier = Modifier.size(48.dp))
                        }

                        // Code Area
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "QR Code",
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White)
                                        .border(1.dp, Border, RoundedCornerShape(16.dp))
                                        .padding(12.dp)
                                )
                            } else {
                                Box(modifier = Modifier.size(240.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            // Demographics Grid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text("DOB", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text(profile.dob ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Text("AGE", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("${profile.age?.toInt() ?: "—"} years", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Text("INCOME", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text("₹${profile.income?.toLong()?.let { String.format("%,d", it) } ?: "—"}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("CATEGORY", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text(profile.category ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                    
                                    Spacer(Modifier.height(16.dp))
                                    
                                    Text("OCCUPATION", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                    Text(profile.occupation ?: "—", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                            HorizontalDivider(color = Brand50)
                            Spacer(Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Map, null, tint = Muted, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(profile.state ?: "—", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.LocationOn, null, tint = Muted, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(profile.ruralUrban ?: "—", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, qrData)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Yojana Card Data")
                    context.startActivity(shareIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Share, "Share", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Print / Share Pass", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
            Text(
                "This QR Code encodes your secure demographic footprint. It cannot be used for financial transactions.",
                color = Muted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
