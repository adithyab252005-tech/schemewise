package com.schemewise.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.schemewise.app.data.model.Profile
import com.schemewise.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val states = listOf(
        "Select State",
        "Andaman and Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar",
        "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Goa",
        "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka",
        "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya",
        "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim",
        "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal"
    )

    val genders  = listOf("Male","Female","Other")
    val occupations = listOf("Farmer","Student","Government Employee","Private Employee","Self-Employed","Unemployed","Other")

    var name       by remember { mutableStateOf("") }
    var dob        by remember { mutableStateOf("") }
    var age        by remember { mutableStateOf("") }
    var district   by remember { mutableStateOf("") }
    var city       by remember { mutableStateOf("") }
    var area       by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(genders[0]) }
    var selectedState  by remember { mutableStateOf(states[0]) }

    LaunchedEffect(selectedState) {
        if (selectedState != "Select State") {
            viewModel.fetchCategories(selectedState)
        }
    }

    val dynamicCastes = uiState.dynamicCastes
    var selectedCaste  by remember { mutableStateOf(dynamicCastes.firstOrNull() ?: "General") }

    LaunchedEffect(dynamicCastes) {
        if (selectedCaste !in dynamicCastes && dynamicCastes.isNotEmpty()) {
            selectedCaste = dynamicCastes[0]
        }
    }

    var selectedOcc    by remember { mutableStateOf(occupations[0]) }
    var income     by remember { mutableStateOf("") }
    var interestedScheme by remember { mutableStateOf("") }
    var isBpl      by remember { mutableStateOf(false) }
    var isDisabled by remember { mutableStateOf(false) }
    var isStudent  by remember { mutableStateOf(false) }
    var isFarmer   by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }
    var categoryConsent by remember { mutableStateOf(true) }
    var selectedMaritalStatus by remember { mutableStateOf("Single") }
    var selectedRuralUrban    by remember { mutableStateOf("Urban") }
    var selectedEmploymentType by remember { mutableStateOf("Unemployed") }
    var selectedStudentLevel  by remember { mutableStateOf("college") }
    var selectedStudentClass  by remember { mutableStateOf("9-10") }
    var selectedStudentDegreeType by remember { mutableStateOf("ug") }
    var selectedStudentCourse by remember { mutableStateOf("engineering") }
    
    val maritalOptions  = listOf("Single", "Married", "Widowed", "Divorced")
    val ruralUrbanOpts  = listOf("Urban", "Rural")
    val employmentTypes = listOf("Farmer / Cultivator", "Agricultural Labourer", "Govt. Employee", "Private Employee", "Self-employed / Business", "Daily Wage Worker", "Unemployed", "Student", "Homemaker")
    val studentLevels   = listOf("school" to "School (Class 1–12)", "college" to "College (UG / Diploma)", "university" to "University (PG / PhD)")
    val studentClasses  = listOf("1-5" to "Class 1–5 (Primary)", "6-8" to "Class 6–8 (Middle)", "9-10" to "Class 9–10 (Secondary)", "11-12" to "Class 11–12 (Higher Secondary)")
    val degreeTypes     = listOf("diploma" to "Diploma / Certificate", "ug" to "UG – Bachelor's", "pg" to "PG – Master's", "phd" to "PhD / Doctoral", "professional" to "Professional (MBBS/LLB/CA)")
    val studentCourses  = listOf("engineering" to "Engineering & Technology", "medical" to "Medical & Health Sciences", "science" to "Pure Sciences", "arts" to "Arts & Humanities", "commerce" to "Commerce & Management", "law" to "Law", "agriculture" to "Agriculture", "education" to "Education / B.Ed", "pharmacy" to "Pharmacy", "nursing" to "Nursing / Paramedical", "polytechnic" to "Polytechnic / ITI", "other" to "Other")

    // Populate from Profile
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { p ->
            name = p.name ?: ""
            dob = p.dob ?: ""
            age = p.age?.toString() ?: ""
            district = p.district ?: ""
            city = p.city ?: ""
            area = p.area ?: ""
            selectedGender = p.gender ?: "Male"
            selectedState = p.state ?: "Maharashtra"
            selectedCaste = p.category ?: dynamicCastes.firstOrNull() ?: "General"
            selectedOcc = p.occupation ?: occupations[0]
            income = p.income?.toString() ?: ""
            isBpl = p.isBPL == "Yes"
            isDisabled = p.isDifferentlyAbled == "Yes"
            isStudent = p.isStudent == "Yes"
            isFarmer = p.isFarmer == "Yes"
            selectedMaritalStatus = p.maritalStatus ?: "Single"
            selectedRuralUrban = p.ruralUrban ?: "Urban"
            selectedEmploymentType = p.employmentType ?: "Unemployed"
            selectedStudentLevel = p.studentLevel ?: "college"
            selectedStudentClass = p.studentClass ?: "9-10"
            selectedStudentDegreeType = p.studentDegreeType ?: "ug"
            selectedStudentCourse = p.studentCourse ?: "engineering"
            interestedScheme = p.interestedScheme ?: ""
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack()
        }
    }

    @Composable
    fun SectionLabel(text: String) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Muted,
            letterSpacing = 0.06.sp, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        val existingProfile = uiState.profile ?: Profile()
                        val p = existingProfile.copy(
                            name = name,
                            dob = dob.ifBlank { null },
                            age = age.toDoubleOrNull() ?: 0.0,
                            gender = selectedGender,
                            state = selectedState,
                            district = district.ifBlank { null },
                            city = city.ifBlank { null },
                            area = area.ifBlank { null },
                            category = selectedCaste,
                            occupation = selectedOcc,
                            income = income.toDoubleOrNull() ?: 0.0,
                            ruralUrban = selectedRuralUrban,
                            isStudent = if (isStudent) "Yes" else "No",
                            isBPL = if (isBpl) "Yes" else "No",
                            isDifferentlyAbled = if (isDisabled) "Yes" else "No",
                            isFarmer = if (isFarmer) "Yes" else "No",
                            maritalStatus = selectedMaritalStatus,
                            employmentType = selectedEmploymentType,
                            studentLevel = if (isStudent) selectedStudentLevel else null,
                            studentClass = if (isStudent && selectedStudentLevel == "school") selectedStudentClass else null,
                            studentDegreeType = if (isStudent && selectedStudentLevel != "school") selectedStudentDegreeType else null,
                            studentCourse = if (isStudent && selectedStudentLevel != "school") selectedStudentCourse else null,
                            interestedScheme = if (interestedScheme.isBlank()) null else interestedScheme
                        )
                        viewModel.saveProfile(p)
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isLoading && name.isNotBlank() && age.isNotBlank() && categoryConsent
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionLabel("PERSONAL")
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.all { char -> char.isLetter() || char.isWhitespace() }) name = it },
                label = { Text("Name as in Aadhar") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = dob, onValueChange = { dob = it }, label = { Text("DOB (YYYY-MM-DD)") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = age, onValueChange = { age = it }, label = { Text("Age") }, modifier = Modifier.weight(1f), singleLine = true)
            }

            SectionLabel("GENDER")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                genders.forEach { g ->
                    FilterChip(selected = selectedGender == g, onClick = { selectedGender = g }, label = { Text(g) })
                }
            }

            SectionLabel("STATE")
            ExposedDropdownMenuBox(
                expanded = stateExpanded,
                onExpandedChange = { stateExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedState,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("State") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(stateExpanded) }
                )
                ExposedDropdownMenu(
                    expanded = stateExpanded,
                    onDismissRequest = { stateExpanded = false }
                ) {
                    states.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s) },
                            onClick = {
                                selectedState = s
                                stateExpanded = false
                            }
                        )
                    }
                }
            }

            SectionLabel("CASTE CATEGORY")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                dynamicCastes.forEach { c ->
                    FilterChip(selected = selectedCaste == c, onClick = { selectedCaste = c }, label = { Text(c) })
                }
            }
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Checkbox(
                    checked = categoryConsent,
                    onCheckedChange = { categoryConsent = it },
                    colors = CheckboxDefaults.colors(checkedColor = Brand500)
                )
                Text(
                    text = "I consent to the processing of my category data.",
                    fontSize = 11.sp,
                    color = Muted,
                    lineHeight = 14.sp
                )
            }

            SectionLabel("LOCATION")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.weight(1f), singleLine = true)
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
            }
            OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("Local Area (Optional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            SectionLabel("AREA TYPE")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ruralUrbanOpts.forEach { opt ->
                    FilterChip(selected = selectedRuralUrban == opt, onClick = { selectedRuralUrban = opt }, label = { Text(opt) })
                }
            }

            SectionLabel("OCCUPATION & EMPLOYMENT")
            OutlinedTextField(
                value = selectedOcc,
                onValueChange = { selectedOcc = it },
                label = { Text("Occupation") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                employmentTypes.forEach { et ->
                    FilterChip(selected = selectedEmploymentType == et, onClick = { selectedEmploymentType = et }, label = { Text(et, fontSize = 11.sp) })
                }
            }

            SectionLabel("ANNUAL INCOME (₹)")
            OutlinedTextField(value = income, onValueChange = { income = it }, label = { Text("e.g. 250000") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            SectionLabel("MARITAL STATUS")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                maritalOptions.forEach { m ->
                    FilterChip(selected = selectedMaritalStatus == m, onClick = { selectedMaritalStatus = m }, label = { Text(m) })
                }
            }

            SectionLabel("ADDITIONAL")
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Below Poverty Line (BPL)?", fontSize = 14.sp, color = Brand800)
                    Switch(checked = isBpl, onCheckedChange = { isBpl = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Person with Disability?", fontSize = 14.sp, color = Brand800)
                    Switch(checked = isDisabled, onCheckedChange = { isDisabled = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Currently a Student?", fontSize = 14.sp, color = Brand800)
                    Switch(checked = isStudent, onCheckedChange = { isStudent = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Are you a Farmer?", fontSize = 14.sp, color = Brand800)
                    Switch(checked = isFarmer, onCheckedChange = { isFarmer = it })
                }

                if (isStudent) {
                    SectionLabel("STUDY LEVEL")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        studentLevels.forEach { (value, label) ->
                            FilterChip(selected = selectedStudentLevel == value, onClick = { selectedStudentLevel = value }, label = { Text(label, fontSize = 11.sp) })
                        }
                    }
                    if (selectedStudentLevel == "school") {
                        SectionLabel("CLASS / GRADE")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            studentClasses.forEach { (value, label) ->
                                FilterChip(selected = selectedStudentClass == value, onClick = { selectedStudentClass = value }, label = { Text(label, fontSize = 11.sp) })
                            }
                        }
                    } else {
                        SectionLabel("DEGREE TYPE")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            degreeTypes.forEach { (value, label) ->
                                FilterChip(selected = selectedStudentDegreeType == value, onClick = { selectedStudentDegreeType = value }, label = { Text(label, fontSize = 11.sp) })
                            }
                        }
                        SectionLabel("COURSE / STREAM")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                            studentCourses.forEach { (value, label) ->
                                FilterChip(selected = selectedStudentCourse == value, onClick = { selectedStudentCourse = value }, label = { Text(label, fontSize = 11.sp) })
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}", 
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBox(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ExposedDropdownMenuBoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        androidx.compose.material3.ExposedDropdownMenuBox(expanded, onExpandedChange, content = content)
    }
}
