package com.schemewise.app.ui.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.ui.components.*
import com.schemewise.app.ui.theme.*

/** Mirrors web ExploreSchemesPage — search bar, filter chips, paginated scheme list */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onSchemeClick:   (String) -> Unit,
    onSettingsClick: () -> Unit = {},
    onProfileClick:  () -> Unit = {},
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    
    val selectedState by viewModel.selectedState.collectAsState()
    val selectedCaste by viewModel.selectedCaste.collectAsState()
    val selectedCat   by viewModel.selectedCat.collectAsState()
    val selectedGender by viewModel.selectedGender.collectAsState()
    val selectedAge   by viewModel.selectedAge.collectAsState()
    val selectedRes   by viewModel.selectedResidence.collectAsState()
    val selectedOcc   by viewModel.selectedOccupation.collectAsState()
    val eligibilityMap by viewModel.eligibilityMap.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    var showFilters by remember { mutableStateOf(false) }
    var isMapView   by remember { mutableStateOf(false) }

    val states = listOf("Andaman and Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal")
    val castes     = listOf("General","OBC","SC","ST", "Minority")
    val categories = listOf("Agriculture","Education","Health","Housing","Women","Business", "Social", "Skills")
    val genders    = listOf("Male", "Female", "Transgender")
    val residences = listOf("Rural", "Urban", "Both")
    val occupations= listOf("Student", "Farmer", "Business Owner", "Employee", "Daily Wage Worker", "Unemployed")

    Scaffold(
        topBar = { 
            SchemeWiseTopBar(
                title = "Explore Directory",
                onSettingsClick = onSettingsClick,
                onProfileClick = onProfileClick,
                actions = {
                    IconButton(onClick = { showFilters = true }) {
                        BadgedBox(badge = {
                            val activeCount = listOf(selectedState, selectedCaste, selectedCat, selectedGender, selectedAge, selectedRes, selectedOcc).count { it != null && it != "ALL" && it != "" }
                            if (activeCount > 0) Badge { Text("$activeCount") }
                        }) {
                            Icon(Icons.Filled.FilterList, "Filters")
                        }
                    }
                }
            ) 
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            OutlinedTextField(
                value       = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search schemes, grants, keywords...", color = Muted, fontSize = 15.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Brand500) },
                modifier    = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(56.dp),
                singleLine  = true,
                colors      = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Brand500,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    cursorColor = Brand500
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(100), // Pill shape for modern look
            )

            // Map vs List Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isMapView) "Points of Interest" else "Directory",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Brand600
                )
                Card(
                    shape = RoundedCornerShape(100),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        val pillColorList = if (!isMapView) Brand500 else Color.Transparent
                        val textColorList = if (!isMapView) Color.White else Muted
                        Box(modifier = Modifier.background(pillColorList, RoundedCornerShape(100))
                            .clickable { isMapView = false }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text("List", color = textColorList, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        val pillColorMap = if (isMapView) Brand500 else Color.Transparent
                        val textColorMap = if (isMapView) Color.White else Muted
                        Box(modifier = Modifier.background(pillColorMap, RoundedCornerShape(100))
                            .clickable { isMapView = true }.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text("Map", color = textColorMap, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            if (isMapView) {
                // MAP VIEW MOCKUP
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                        .border(1.dp, Border, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.LocationOn, "Map", tint = Brand500, modifier = Modifier.size(48.dp))
                        Text("Interactive Scheme Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Brand600)
                        Text("Locating fulfillment centers and enrollment hubs near your configured state...", 
                             fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center,
                             modifier = Modifier.padding(horizontal = 32.dp))
                        
                        // Fake Cards
                        Spacer(Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text("📍 Seva Kendra (3.2 km)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("142 active housing schemes processed daily", fontSize = 12.sp, color = Muted)
                            }
                        }
                    }
                }
            } else {
                if (state.isLoading && state.schemes.isEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(6) { SchemeCardSkeleton() }
                    }
                } else if (state.error != null) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon     = Icons.Filled.Search,
                            title    = "Error Loading Schemes",
                            subtitle = state.error ?: "Unknown error",
                        )
                    }
                } else if (state.schemes.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon     = Icons.Filled.Search,
                            title    = "No schemes found",
                            subtitle = "Try adjusting your search or filters.",
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), 
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${state.schemes.size}", fontWeight = FontWeight.Bold, color = OnSurface, fontSize = 14.sp)
                        Text(" schemes match your filters", color = Muted, fontSize = 14.sp)
                    }
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.schemes, key = { it.schemeId }) { scheme ->
                            SchemeCard(
                                scheme = scheme, 
                                eligibilityScore = eligibilityMap[scheme.schemeId],
                                onCardClick = { onSchemeClick(scheme.schemeId.toString()) }
                            )
                        }
                    }
                } // closes if (state.schemes.isEmpty()) .. else block
        } // closes if (isMapView) .. else block
        } // closes Column

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Filters", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = {
                            viewModel.selectedState.value = null
                            viewModel.selectedCaste.value = null
                            viewModel.selectedCat.value = null
                            viewModel.selectedGender.value = null
                            viewModel.selectedAge.value = null
                            viewModel.selectedResidence.value = null
                            viewModel.selectedOccupation.value = null
                        }) {
                            Text("Reset All", color = Color.Red)
                        }
                    }

                    FilterSection("State") {
                        FilterDropdown(selectedState ?: "ALL", listOf("ALL") + states) { viewModel.selectedState.value = it }
                    }

                    FilterSection("Category") {
                        FilterDropdown(selectedCat ?: "ALL", listOf("ALL") + categories) { viewModel.selectedCat.value = it }
                    }

                    FilterSection("Gender") {
                        FilterDropdown(selectedGender ?: "ALL", listOf("ALL") + genders) { viewModel.selectedGender.value = it }
                    }

                    FilterSection("Age") {
                        OutlinedTextField(
                            value = selectedAge ?: "",
                            onValueChange = { viewModel.selectedAge.value = it },
                            placeholder = { Text("Enter Age") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    FilterSection("Caste / Category") {
                        FilterDropdown(selectedCaste ?: "ALL", listOf("ALL") + castes) { viewModel.selectedCaste.value = it }
                    }

                    FilterSection("Residence") {
                        FilterDropdown(selectedRes ?: "ALL", listOf("ALL") + residences) { viewModel.selectedResidence.value = it }
                    }

                    FilterSection("Occupation") {
                        FilterDropdown(selectedOcc ?: "ALL", listOf("ALL") + occupations) { viewModel.selectedOccupation.value = it }
                    }

                    Button(
                        onClick = { showFilters = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp).padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Show Results", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted, letterSpacing = 1.sp)
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(8.dp)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
