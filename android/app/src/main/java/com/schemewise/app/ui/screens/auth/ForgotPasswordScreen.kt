package com.schemewise.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onResetTokenReceived: (token: String) -> Unit = {},   // called in dev mode if token returned
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    var email      by remember { mutableStateOf("") }
    var loading    by remember { mutableStateOf(false) }
    var sent       by remember { mutableStateOf(false) }
    var devLink    by remember { mutableStateOf("") }
    var errorMsg   by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    fun sendReset() {
        if (email.isBlank()) return
        scope.launch {
            loading = true
            errorMsg = ""
            try {
                val res = viewModel.forgotPassword(email.trim().lowercase())
                if (res.isSuccess) {
                    val body = res.getOrThrow()
                    // Dev mode: backend returns the reset link directly when SMTP not configured
                    val link = body["dev_reset_link"]
                    if (!link.isNullOrBlank()) {
                        devLink = link
                        // Extract token from link for navigation
                        val token = link.substringAfter("token=").substringBefore("&")
                        onResetTokenReceived(token)
                    }
                    sent = true
                } else {
                    errorMsg = "Failed to send reset link. Please try again."
                }
            } catch (e: Exception) {
                errorMsg = "Network error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Forgot your password?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Brand800)
            Text(
                "Enter the email address associated with your account and we'll send you a reset link.",
                color = Muted, fontSize = 13.sp, lineHeight = 18.sp,
            )

            if (sent) {
                // ── Success state ─────────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape  = RoundedCornerShape(12.dp),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "✅ Reset link sent! Check your email inbox.",
                            color = Color(0xFF15803D), fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Look for an email from schemewise.in@gmail.com. Click 'Login Directly' or 'Reset My Password'.",
                            color = Color(0xFF166534), fontSize = 12.sp, lineHeight = 16.sp,
                        )
                        if (devLink.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "🔧 Dev mode — reset link returned in API response.\nUse 'Login Directly' button in-app.",
                                color = Color(0xFF92400E), fontSize = 11.sp, lineHeight = 15.sp,
                            )
                        }
                    }
                }
            } else {
                // ── Input form ────────────────────────────────────────────
                if (errorMsg.isNotBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        shape  = RoundedCornerShape(10.dp),
                    ) {
                        Text(errorMsg, modifier = Modifier.padding(12.dp), color = Color(0xFF991B1B), fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = Muted) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    enabled = !loading,
                )
                Button(
                    onClick   = { sendReset() },
                    modifier  = Modifier.fillMaxWidth().height(48.dp),
                    enabled   = email.isNotBlank() && !loading,
                    colors    = ButtonDefaults.buttonColors(containerColor = Brand500),
                    shape     = RoundedCornerShape(10.dp),
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Reset Link", fontWeight = FontWeight.Bold)
                    }
                }
            }

            TextButton(onClick = onBack) {
                Text("← Back to Sign In", color = Brand500)
            }
        }
    }
}
