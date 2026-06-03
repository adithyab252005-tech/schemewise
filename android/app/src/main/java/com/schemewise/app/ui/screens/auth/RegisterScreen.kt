package com.schemewise.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var name            by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPwd         by remember { mutableStateOf(false) }
    var showConfirmPwd  by remember { mutableStateOf(false) }
    var localError      by remember { mutableStateOf<String?>(null) }
    var otp             by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isVerified) {
        if (uiState.isVerified) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.emailSent) "Verify Your Email" else "Create Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── OTP Verification State ──────────────────────────────────────────────
            if (uiState.emailSent) {
                Spacer(Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    tint = Brand500,
                    modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Check Your Inbox!",
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Brand800,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "We've sent a 6-digit verification code to\n${uiState.sentToEmail}\n\nEnter it below to complete your registration.",
                    color = Muted, fontSize = 14.sp, lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))

                OutlinedTextField(
                    value = otp,
                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) otp = it },
                    placeholder = { Text("••••••", letterSpacing = 8.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, letterSpacing = 8.sp, fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand500,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        localError = null
                        if (otp.length == 6) {
                            viewModel.verifyOtp(uiState.sentToEmail, otp)
                        } else {
                            localError = "Please enter the 6-digit code."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                    enabled = !uiState.isLoading && otp.length == 6
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Verify & Continue", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (!uiState.error.isNullOrEmpty() || localError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = localError ?: uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))
                // Wrong email warning card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFFBEB)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Wrong email address?", fontSize = 13.sp, color = androidx.compose.ui.graphics.Color(0xFF92400E))
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Restart Registration", fontWeight = FontWeight.Bold, color = Brand600)
                        }
                    }
                }

                return@Column
            }

            // ── Registration Form ─────────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(56.dp),
                    color = Brand500,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("S", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Create your Civic ID",
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Brand800,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Your universal gateway to schemes.",
                    color = Muted, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(8.dp))

            // Name
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FULL LEGAL NAME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) name = it },
                    placeholder = { Text("As in Aadhaar card") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = Muted) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand500,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }

            // Email
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    placeholder = { Text("name@example.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = Muted) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand500,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }

            // Password
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    placeholder = { Text("Min. 8 characters") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Muted) },
                    trailingIcon = {
                        IconButton(onClick = { showPwd = !showPwd }) {
                            Icon(if (showPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    },
                    visualTransformation = if (showPwd) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand500,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }

            // Confirm Password
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CONFIRM PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
                OutlinedTextField(
                    value = confirmPassword, onValueChange = { confirmPassword = it },
                    placeholder = { Text("Repeat password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Muted) },
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPwd = !showConfirmPwd }) {
                            Icon(if (showConfirmPwd) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                        }
                    },
                    visualTransformation = if (showConfirmPwd) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Brand500,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                )
            }
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text("Passwords do not match", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            // Errors (ViewModel or local)
            localError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }

            val isFormValid = name.isNotBlank() && email.isNotBlank()
                && password.isNotBlank() && password == confirmPassword

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    localError = null
                    if (password != confirmPassword) {
                        localError = "Passwords do not match."
                        return@Button
                    }
                    viewModel.register(name, email, password)
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !uiState.isLoading && isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Verify & Continue →", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", color = Muted, fontSize = 14.sp)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In", color = Brand500, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

