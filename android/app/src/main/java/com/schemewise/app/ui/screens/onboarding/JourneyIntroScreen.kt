package com.schemewise.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

/** Mirrors web JourneyIntroPage.jsx — carousel of feature highlights before entering the app */
@Composable
fun JourneyIntroScreen(onEnterApp: () -> Unit) {
    val features = listOf(
        Triple(Icons.Filled.Search,    "Discover Schemes",   "Browse 1000+ central & state government welfare schemes filtered for you."),
        Triple(Icons.Filled.Bookmark,  "Save & Track",       "Bookmark schemes you like and track your application status."),
        Triple(Icons.Filled.SmartToy,  "Ask the AI Bot",     "Get instant answers about scheme eligibility and documents needed."),
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Brand900, Brand700))),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Spacer(Modifier.height(24.dp))
                Text("Your Journey Starts Here", color = Color.White, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("Here's what you can do with SchemeWise",
                    color = Color.White.copy(0.6f), fontSize = 13.sp, textAlign = TextAlign.Center)

                Spacer(Modifier.height(24.dp))
                features.forEach { (icon, title, desc) ->
                    FeatureTile(icon = icon, title = title, description = desc)
                }
            }

            Button(
                onClick  = onEnterApp,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Accent500),
                shape    = RoundedCornerShape(14.dp),
            ) {
                Text("Enter SchemeWise", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp)
                .background(Accent500.copy(0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Accent500, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(description, color = Color.White.copy(0.6f), fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}
