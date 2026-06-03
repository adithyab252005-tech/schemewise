package com.schemewise.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess:       () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword:     () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPwd  by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .imePadding()                          // ← shifts content above keyboard
            .verticalScroll(rememberScrollState()) // ← allows scrolling when keyboard is open
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Brush.verticalGradient(listOf(Brand900, Brand700))),
        ) {
            // Tricolor accent bar at top
            Row(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFF97316)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color.White.copy(0.3f)))
                Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF22C55E)))
            }
            Column(
                modifier = Modifier.align(Alignment.Center).padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Welcome Back", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Sign in to your SchemeWise account", color = Color.White.copy(0.6f), fontSize = 13.sp)
            }
        }

        // Card form — sits directly below header, no floating/centering
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-24).dp), // slight overlap with header for polish
            shape  = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand800)

                // Email
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email address") },
                    leadingIcon   = { Icon(Icons.Filled.Email, null, tint = Muted) },
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine    = true,
                )
                // Password
                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = { Text("Password") },
                    leadingIcon   = { Icon(Icons.Filled.Lock, null, tint = Muted) },
                    trailingIcon  = {
                        IconButton(onClick = { showPwd = !showPwd }) {
                            Icon(if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    },
                    visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier  = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                    Text("Forgot password?", color = Brand500, fontSize = 12.sp)
                }

                // Error
                uiState.error?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                // Sign in button
                Button(
                    onClick  = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled  = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                    colors   = ButtonDefaults.buttonColors(containerColor = Brand500),
                    shape    = RoundedCornerShape(10.dp),
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Text("Sign In", fontWeight = FontWeight.Bold)
                }

                // Register link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text("Don't have an account?", color = Muted, fontSize = 13.sp)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Register", color = Brand500, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Extra bottom spacing so content doesn't feel cramped
        Spacer(modifier = Modifier.height(32.dp))
    }
}
