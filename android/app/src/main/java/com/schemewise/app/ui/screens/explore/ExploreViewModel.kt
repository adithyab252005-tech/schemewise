package com.schemewise.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.data.repository.SchemeRepository
import com.schemewise.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val isLoading: Boolean      = true,
    val schemes:   List<Scheme> = emptyList(),
    val error:     String?      = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val schemeRepo: SchemeRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {

    private val _uiState  = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState

    val searchQuery   = MutableStateFlow("")
    val selectedState = MutableStateFlow<String?>(null)
    val selectedCaste = MutableStateFlow<String?>(null)
    val selectedCat   = MutableStateFlow<String?>(null)
    val selectedGender     = MutableStateFlow<String?>(null)
    val selectedAge        = MutableStateFlow<String?>(null)
    val selectedResidence  = MutableStateFlow<String?>(null)
    val selectedOccupation = MutableStateFlow<String?>(null)
    
    val justForMe          = MutableStateFlow(false)
    val eligibilityMap     = MutableStateFlow<Map<Int, Int>>(emptyMap())
    
    private val allSchemes = MutableStateFlow<List<Scheme>>(emptyList())

    private var dataLoaded = false

    init {
        loadInitialData()

        // Debounced search + filter reactive pipeline
        combine(
            listOf(
                searchQuery.debounce(300),
                selectedState, selectedCaste, selectedCat,
                selectedGender, selectedAge, selectedResidence,
                selectedOccupation, allSchemes, justForMe, eligibilityMap
            )
        ) { array ->
            @Suppress("UNCHECKED_CAST")
            FilterParams(
                search = array[0] as String,
                state  = array[1] as String?,
                caste  = array[2] as String?,
                cat    = array[3] as String?,
                gender = array[4] as String?,
                age    = array[5] as String?,
                res    = array[6] as String?,
                occ    = array[7] as String?,
                allList = array[8] as List<Scheme>,
                justMe  = array[9] as Boolean,
                eligMap = array[10] as Map<Int, Int>
            )
        }.onEach { params ->
            if (dataLoaded) applyFilters(params)
        }.launchIn(viewModelScope)
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            // Load Profile / Eligibility
            val profileRes = profileRepo.getProfile()
            if (profileRes.isSuccess) {
                profileRes.getOrNull()?.let { p ->
                    val evals = schemeRepo.evaluateEligibility(p).getOrDefault(emptyList())
                    eligibilityMap.value = evals.associate { it.schemeId to it.scorePercentage }
                }
            }
            schemeRepo.getSchemes()
                .onSuccess { schemes ->
                    dataLoaded = true
                    allSchemes.value = schemes           // triggers reactive combine
                    _uiState.value = ExploreUiState(isLoading = false, schemes = schemes)
                }
                .onFailure { error ->
                    _uiState.value = ExploreUiState(isLoading = false, error = error.message)
                }
        }
    }

    private fun applyFilters(p: FilterParams) {
        val filtered = p.allList.filter { s ->
            val matchesSearch = p.search.isBlank() || s.schemeName.contains(p.search, ignoreCase = true)
            val matchesState  = p.state == null || p.state == "ALL" || s.stateApplicable == "ALL" || s.stateApplicable == p.state
            val matchesCat    = p.cat == null || p.cat == "ALL" || s.schemeCategory?.contains(p.cat) == true
            val parsedCaste = s.eligibleCategories?.toString() ?: "[]"
            val matchesCaste  = p.caste == null || p.caste == "ALL" || parsedCaste == "[]" || parsedCaste.contains("ALL", ignoreCase = true) || parsedCaste.contains(p.caste, ignoreCase = true)
            val justMeFilter  = if (p.justMe) (p.eligMap[s.schemeId] ?: 0) >= 80 else true
            
            val matchesGender = p.gender == null || p.gender == "ALL" || s.targetGender == "All" || s.targetGender == p.gender
            
            var matchesAge = true
            if (p.age != null && p.age.isNotBlank()) {
                val ageInt = p.age.toIntOrNull() ?: 0
                if (s.targetAgeMin != null && ageInt < s.targetAgeMin.toInt()) matchesAge = false
                if (s.targetAgeMax != null && ageInt > s.targetAgeMax.toInt()) matchesAge = false
            }

            val matchesRes = p.res == null || p.res == "ALL" || s.ruralUrban == "Both" || s.ruralUrban == p.res
            
            val parsedOcc = s.occupationRequired?.toString() ?: "[]"
            val matchesOcc = p.occ == null || p.occ == "ALL" || parsedOcc == "[]" || parsedOcc.contains("Any", ignoreCase = true) || parsedOcc.contains(p.occ, ignoreCase = true)

            matchesSearch && matchesState && matchesCat && matchesCaste && matchesGender && matchesAge && matchesRes && matchesOcc && justMeFilter
        }
        _uiState.value = ExploreUiState(isLoading = false, schemes = filtered)
    }
}

data class FilterParams(
    val search: String, val state: String?, val caste: String?, val cat: String?, 
    val gender: String?, val age: String?, val res: String?, val occ: String?,
    val allList: List<Scheme>, val justMe: Boolean, val eligMap: Map<Int, Int>
)
