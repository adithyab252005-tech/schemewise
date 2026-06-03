package com.schemewise.app.ui.screens.simulator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.Profile
import com.schemewise.app.ui.components.SchemeWiseTopBar
import com.schemewise.app.ui.theme.*

/**
 * SimulatorScreen — mirrors web SimulatorPage.jsx.
 * Lets user input a hypothetical profile and instantly see matching schemes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulatorScreen(
    onResultsReady: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: SimulatorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    var age              by remember { mutableStateOf("") }
    var income           by remember { mutableStateOf("") }
    var state_           by remember { mutableStateOf("Maharashtra") }
    var district         by remember { mutableStateOf("") }
    var city             by remember { mutableStateOf("") }
    var caste            by remember { mutableStateOf("General") }
    var area             by remember { mutableStateOf("Urban") }
    var maritalStatus    by remember { mutableStateOf("Single") }
    var occupation       by remember { mutableStateOf("") }
    var gender           by remember { mutableStateOf("Male") }
    var studentLevel     by remember { mutableStateOf("None") }
    
    var isBpl            by remember { mutableStateOf(false) }
    var isMinority       by remember { mutableStateOf(false) }
    var isStudent        by remember { mutableStateOf(false) }
    var isDifferentlyAbled by remember { mutableStateOf(false) }

    LaunchedEffect(state.isDone) {
        if (state.isDone && state.results.isNotEmpty()) onResultsReady()
    }

    Scaffold(topBar = { SchemeWiseTopBar(title = "Eligibility Simulator", onSettingsClick = onSettingsClick, onProfileClick = onProfileClick) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Info banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Brand500.copy(.08f)),
                shape  = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Brand500.copy(0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                    Column {
                        Text("Hypothetical Check", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Brand600)
                        Text("Simulate eligibility without changing your saved profile.",
                            color = MaterialTheme.colorScheme.onSurface.copy(0.7f), fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            
            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Brand500,
                unfocusedBorderColor = Border,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = Brand500
            )

            // Section 1: Basic Personas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Basic Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Brand600)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                        
                        OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors)
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(value = occupation, onValueChange = { occupation = it }, label = { Text("Occupation", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors)
                            
                        OutlinedTextField(value = income, onValueChange = { income = it }, label = { Text("Income (₹)", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    }
                }
            }

            // Section 2: Localization
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Demographics & Location", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Brand600)
                    
                    OutlinedTextField(value = state_, onValueChange = { state_ = it }, label = { Text("State", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = textFieldColors)

                    Text("Category", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("General","OBC","SC","ST","Minority").forEach { c ->
                            AssistChip(
                                onClick = { caste = c }, 
                                label = { Text(c, fontWeight = if (caste==c) FontWeight.Bold else FontWeight.Normal) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (caste == c) Brand500 else Color.Transparent,
                                    labelColor = if (caste == c) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(borderColor = if (caste == c) Brand500 else Border, enabled=true)
                            )
                        }
                    }

                    Text("Area Type", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Urban","Rural").forEach { a ->
                            AssistChip(
                                onClick = { area = a }, 
                                label = { Text(a, fontWeight = if (area==a) FontWeight.Bold else FontWeight.Normal) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (area == a) Brand500 else Color.Transparent,
                                    labelColor = if (area == a) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(borderColor = if (area == a) Brand500 else Border, enabled=true)
                            )
                        }
                    }

                    Text("Marital Status", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Muted, modifier = Modifier.padding(top = 4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Single","Married","Divorced","Widowed").forEach { m ->
                            AssistChip(
                                onClick = { maritalStatus = m }, 
                                label = { Text(m, fontWeight = if (maritalStatus==m) FontWeight.Bold else FontWeight.Normal) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (maritalStatus == m) Brand500 else Color.Transparent,
                                    labelColor = if (maritalStatus == m) Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                border = AssistChipDefaults.assistChipBorder(borderColor = if (maritalStatus == m) Brand500 else Border, enabled=true)
                            )
                        }
                    }
                }
            }

            // Section 3: Extra Attributes
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Special Flags", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Brand600)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Below Poverty Line (BPL)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isBpl, onCheckedChange = { isBpl = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand500))
                    }
                    Divider(color = Border)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Minority Status", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isMinority, onCheckedChange = { isMinority = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand500))
                    }
                    Divider(color = Border)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Differently Abled", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isDifferentlyAbled, onCheckedChange = { isDifferentlyAbled = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand500))
                    }
                    Divider(color = Border)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Currently a Student", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(checked = isStudent, onCheckedChange = { isStudent = it }, colors = SwitchDefaults.colors(checkedTrackColor = Brand500))
                    }
                    if (isStudent) {
                        Divider(color = Border)
                        Text("Student Level", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Muted)
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("School","Undergraduate","Postgraduate").forEach { l ->
                                AssistChip(
                                    onClick = { studentLevel = l }, 
                                    label = { Text(l, fontWeight = if (studentLevel==l) FontWeight.Bold else FontWeight.Normal) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (studentLevel == l) Brand500 else Color.Transparent,
                                        labelColor = if (studentLevel == l) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    border = AssistChipDefaults.assistChipBorder(borderColor = if (studentLevel == l) Brand500 else Border, enabled=true)
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let { 
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)), shape = RoundedCornerShape(10.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp), color = Color(0xFF991B1B), fontSize = 13.sp) 
                }
            }

            // CTA
            Button(
                onClick = {
                    viewModel.runSimulation(
                        Profile(
                            age      = age.toDoubleOrNull(),
                            income   = income.toDoubleOrNull(),
                            category = caste,
                            state    = state_,
                            occupation = occupation,
                            gender   = gender,
                            ruralUrban = area,
                            maritalStatus = maritalStatus,
                            studentLevel = studentLevel,
                            isStudent = if (isStudent) "Yes" else "No",
                            isDifferentlyAbled = if (isDifferentlyAbled) "Yes" else "No",
                            isBPL    = if (isBpl) "Yes" else "No"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled  = !state.isLoading && age.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = Brand500),
                shape    = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Run Fast Simulation", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, letterSpacing = 0.5.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("⚡", fontSize = 18.sp)
                    }
                }
            }

            TextButton(
                onClick = viewModel::reset, 
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.textButtonColors(contentColor = Muted)
            ) {
                Text("Reset Fields", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
