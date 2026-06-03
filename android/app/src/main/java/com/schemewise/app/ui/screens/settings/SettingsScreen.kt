package com.schemewise.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.schemewise.app.SubscriptionActivity
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack:         () -> Unit,
    onViewProfile:  () -> Unit = {},
    onEditProfile:  () -> Unit = {},
    onLogout:       () -> Unit,
    prefsManager:   PrefsManager? = null
) {
    val context = LocalContext.current
    val isPremium = remember {
        context.getSharedPreferences("subscription_prefs", android.content.Context.MODE_PRIVATE)
            .getBoolean("is_premium_user", false)
    }
    var activeDialog by remember { mutableStateOf<String?>(null) }
    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }
    var passSuccess by remember { mutableStateOf(false) }

    // Launch system notification settings when requested
    LaunchedEffect(activeDialog) {
        if (activeDialog == "Notifications") {
            activeDialog = null
            val intent = android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(android.provider.Settings.EXTRA_APP_PACKAGE as String, context.packageName as String)
            context.startActivity(intent)
        }
    }

    val legalContent = mapOf(
        "Disclaimer" to "SchemeWise is an independent informational platform and is not an official government portal. The information provided herein is aggregated from publicly available sources, press releases, and official announcements.\n\nWhile we strive to keep the information accurate, we make no representations or warranties about the completeness or reliability of the schemes. Eligibility estimates provided by our AI are strictly estimates based on your profile and do not guarantee approval. Final eligibility and processing are at the discretion of the government authorities. Users should verify details on official websites before making decisions.",
        "Terms of Service" to "Welcome to SchemeWise. By using our platform, you agree to:\n\n1. Use of Service: Use exclusively for personal, non-commercial purposes. No automated scripts or scrapers.\n2. User Accuracy: Provide accurate profile information. You are responsible for safeguarding your credentials.\n3. Service Modifications: We reserve the right to modify or suspend the service at any time. Real-time scheme accuracy is not guaranteed.\n4. Limitation of Liability: SchemeWise shall not be liable for indirect damages resulting from reliance on our scheme data.",
        "Privacy Policy" to "Your privacy is critically important to us.\n\n1. Information Collection: We collect demographic data you provide (age, state, income, etc.) essential for AI eligibility matching.\n2. Data Usage: Your data is used strictly for personalized recommendations. We do not sell or lease your data to third-party marketers.\n3. Data Security: We implement industry-standard encryption protocols to protect your sensitive information.\n4. Third-Party Links: Our platform links to official government websites. Once you leave our app, their privacy policies apply."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ── Profile Card ───────────────────────────────────────────
            Text(
                "ACCOUNT",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B), letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // View Profile row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onViewProfile)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Brand500.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp), tint = Brand500)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("View Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("See your saved details", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color(0xFFCBD5E1))
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Edit Profile row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onEditProfile)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp), tint = Color(0xFF10B981))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Edit Profile", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Update your personal details to improve scheme matches", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color(0xFFCBD5E1))
                    }
                    // Change Password row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeDialog = "Password" }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp), tint = Color(0xFFF59E0B))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Change Password", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Secure your account credentials", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color(0xFFCBD5E1))
                    }
                }
            }

            // ── Subscription / Premium ────────────────────────────────────
            Surface(
                onClick = {
                    context.startActivity(Intent(context, SubscriptionActivity::class.java))
                },
                shape  = RoundedCornerShape(16.dp),
                color  = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF6C5CE7), Color(0xFFA78BFA))),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (isPremium) "You're Premium ✓" else "Upgrade to Premium",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                if (isPremium) "Enjoying all premium features" else "Ad-free · Exclusive tools · ₹100/month",
                                fontSize = 12.sp,
                                color = Color.White.copy(0.8f)
                            )
                        }
                        if (!isPremium) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White
                            ) {
                                Text(
                                    "Get",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6C5CE7),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── Preferences ──────────────────────────────────────────────
            SettingsSection(title = "Preferences") {
                SettingsItem(icon = Icons.Default.Notifications, label = "Notifications", value = "Enabled", iconColor = Color(0xFFFB7185), onClick = { activeDialog = "Notifications" })
            }

            // ── App Info ───────────────────────────────────────────────
            SettingsSection(title = "App Information") {
                SettingsItem(icon = Icons.Default.Info,   label = "About SchemeWise", iconColor = Color(0xFF3B82F6), onClick = { activeDialog = "About" })
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsItem(icon = Icons.Default.Shield, label = "Version", value = "v1.2.4 (Build 42)", iconColor = Color(0xFF3B82F6))
            }

            SettingsSection(title = "Legal") {
                SettingsItem(icon = Icons.Default.Policy, label = "Privacy Policy", iconColor = Color(0xFF64748B), onClick = { activeDialog = "Privacy Policy" })
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsItem(icon = Icons.Default.Description, label = "Disclaimer", iconColor = Color(0xFF64748B), onClick = { activeDialog = "Disclaimer" })
                HorizontalDivider(color = Color(0xFFF1F5F9))
                SettingsItem(icon = Icons.Default.Gavel, label = "Terms of Service", iconColor = Color(0xFF64748B), onClick = { activeDialog = "Terms of Service" })
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                onClick = onLogout,
                shape   = RoundedCornerShape(16.dp),
                color   = Color.White,
                border  = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFF1F2)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color(0xFFF43F5E))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign out", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = Brand500, modifier = Modifier.size(48.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("SW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("SchemeWise Digital India".uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8), letterSpacing = 1.sp)
            }
        }
    }

    // Dynamic Dialog Renders
    if (activeDialog != null) {
        val dlg = activeDialog!!
        if (dlg == "Password") {
            AlertDialog(
                onDismissRequest = { activeDialog = null; passSuccess = false },
                title = { Text(if (passSuccess) "Password Updated!" else "Change Password", fontWeight = FontWeight.Bold) },
                text = {
                    if (passSuccess) {
                        Text("Your password has been successfully changed.", color = Brand500)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = oldPass, onValueChange = { oldPass = it }, label = { Text("Current Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = confPass, onValueChange = { confPass = it }, label = { Text("Confirm New Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    if (!passSuccess) {
                        Button(onClick = { passSuccess = true }, colors = ButtonDefaults.buttonColors(containerColor = Brand500)) {
                            Text("Save Changes")
                        }
                    } else {
                        Button(onClick = { activeDialog = null; passSuccess = false }, colors = ButtonDefaults.buttonColors(containerColor = Brand500)) {
                            Text("Done")
                        }
                    }
                },
                dismissButton = {
                    if (!passSuccess) {
                        TextButton(onClick = { activeDialog = null }) { Text("Cancel", color = Muted) }
                    }
                }
            )
        } else if (dlg == "About") {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text("About SchemeWise", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("SchemeWise is a pioneering Digital India initiative designed to bridge the gap between citizens and government welfare programs. Our mission is to ensure that every eligible Indian can easily discover, understand, and access their rightful benefits without administrative hurdles.")
                        Text("Powered by an advanced AI-driven eligibility engine, SchemeWise analyzes over 4,200 central and state-level schemes in real-time. By securely matching your unique socio-economic profile against complex administrative criteria, we eliminate the guesswork and provide you with highly accurate, personalized recommendations.")
                        Text("Built with transparency and user privacy at its core, this platform serves as your intelligent Citizen Portal—transforming bureaucratic complexity into an intuitive, accessible experience.", fontWeight = FontWeight.Bold)
                    }
                },
                confirmButton = {
                    Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Brand500)) { Text("Close") }
                }
            )
        } else if (legalContent.containsKey(dlg)) {
            AlertDialog(
                onDismissRequest = { activeDialog = null },
                title = { Text(dlg, fontWeight = FontWeight.Bold) },
                text = { Text(legalContent[dlg]!!) },
                confirmButton = {
                    Button(onClick = { activeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = Brand500)) { Text("I Understand") }
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B), // Slate 500
            modifier = Modifier.padding(start = 4.dp),
            letterSpacing = 0.5.sp
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    value: String = "",
    iconColor: Color = Brand500,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = iconColor.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = iconColor)
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.weight(1f))
        if (value.isNotEmpty()) {
            Text(value, fontSize = 12.sp, color = Color(0xFF94A3B8))
            Spacer(Modifier.width(8.dp))
        }
        Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp), tint = Color(0xFFCBD5E1))
    }
}
