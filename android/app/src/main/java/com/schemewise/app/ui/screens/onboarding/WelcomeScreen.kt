package com.schemewise.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.schemewise.app.ui.theme.*

/** Mirrors web WelcomePage.jsx — post-login greeting before onboarding details */
@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Brand900, Brand700))),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Shield, contentDescription = null,
                tint = Color.White, modifier = Modifier.size(72.dp),
            )
            Text("Welcome to SchemeWise!", color = Color.White, fontSize = 26.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                "Let's set up your profile so we can find the government schemes you qualify for.",
                color = Color.White.copy(0.7f), fontSize = 14.sp, textAlign = TextAlign.Center,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick  = onContinue,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Accent500),
                shape    = RoundedCornerShape(12.dp),
            ) {
                Text("Get Started →", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
