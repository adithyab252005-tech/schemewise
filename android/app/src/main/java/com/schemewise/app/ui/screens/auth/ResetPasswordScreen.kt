package com.schemewise.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.AuthResponse
import com.schemewise.app.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Handles two flows:
 * 1. Magic Login  — POST /reset-password { token } → logs user in without password
 * 2. Set Password — POST /reset-password { token, newPassword } → updates pass and logs in
 *
 * Mirrors web ResetPasswordPage.jsx
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    token: String,
    onLoginSuccess: (AuthResponse) -> Unit,
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading         by remember { mutableStateOf(false) }
    var errorMsg        by remember { mutableStateOf("") }
    var successMsg      by remember { mutableStateOf("") }
    val scope           = rememberCoroutineScope()

    // ── Magic login ───────────────────────────────────────────────────────
    fun doMagicLogin() {
        scope.launch {
            loading = true
            errorMsg = ""
            try {
                val res = viewModel.resetPassword(token = token, newPassword = null)
                if (res.isSuccess) {
                    onLoginSuccess(res.getOrThrow())
                } else {
                    errorMsg = "Invalid or expired link. Please request a new one."
                }
            } catch (e: Exception) {
                errorMsg = "Network error: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    // ── Set new password ──────────────────────────────────────────────────
    fun doResetPassword() {
        if (newPassword != confirmPassword) { errorMsg = "Passwords do not match."; return }
        if (newPassword.length < 6)        { errorMsg = "Minimum 6 characters.";    return }
        scope.launch {
            loading = true
            errorMsg = ""
            try {
                val res = viewModel.resetPassword(token = token, newPassword = newPassword)
                if (res.isSuccess) {
                    successMsg = "Password updated! Logging you in…"
                    onLoginSuccess(res.getOrThrow())
                } else {
                    errorMsg = "Invalid or expired link. Please request a new one."
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
                title = { Text("Set New Password", fontWeight = FontWeight.Bold) },
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
            Text("Recover your account", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Brand800)
            Text("Choose how you'd like to access your account.", color = Muted, fontSize = 13.sp)

            if (errorMsg.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                    shape  = RoundedCornerShape(10.dp),
                ) {
                    Text(errorMsg, modifier = Modifier.padding(12.dp), color = Color(0xFF991B1B), fontSize = 13.sp)
                }
            }

            if (successMsg.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    shape  = RoundedCornerShape(10.dp),
                ) {
                    Text(successMsg, modifier = Modifier.padding(12.dp), color = Color(0xFF15803D), fontSize = 13.sp)
                }
            }

            // ── Option 1: Magic Login ─────────────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                shape  = RoundedCornerShape(12.dp),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "🚀 OPTION 1 — LOGIN WITHOUT PASSWORD",
                        fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = Color(0xFF4338CA), letterSpacing = 0.5.sp,
                    )
                    Text(
                        "Skip setting a password and log in instantly with your magic link.",
                        fontSize = 13.sp, color = Color(0xFF3730A3), lineHeight = 18.sp,
                    )
                    Button(
                        onClick   = { doMagicLogin() },
                        enabled   = !loading,
                        modifier  = Modifier.fillMaxWidth().height(44.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Brand500),
                        shape     = RoundedCornerShape(10.dp),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Login Directly →", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Divider ───────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text("  OR SET A NEW PASSWORD  ", fontSize = 11.sp, color = Muted)
                Divider(Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            // ── Option 2: Set New Password ────────────────────────────────
            OutlinedTextField(
                value = newPassword, onValueChange = { newPassword = it },
                label = { Text("New Password") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading,
            )
            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading,
            )
            Button(
                onClick   = { doResetPassword() },
                enabled   = newPassword.isNotBlank() && !loading,
                modifier  = Modifier.fillMaxWidth().height(48.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Brand500),
                shape     = RoundedCornerShape(10.dp),
            ) {
                Text("Update Password", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBack) {
                Text("← Back", color = Brand500)
            }
        }
    }
}
