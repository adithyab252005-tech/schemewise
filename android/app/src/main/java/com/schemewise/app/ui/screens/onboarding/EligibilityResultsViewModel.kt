package com.schemewise.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.data.repository.ProfileRepository
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EligibilityResultsUiState(
    val isLoading: Boolean = true,
    val results: List<EligibilityResult> = emptyList(),
    val interestedMatches: List<EligibilityResult> = emptyList(),
    val interestedScheme: String? = null,
    val profile: com.schemewise.app.data.model.Profile? = null,
    val error: String? = null
)

@HiltViewModel
class EligibilityResultsViewModel @Inject constructor(
    private val schemeRepo: SchemeRepository,
    private val profileRepo: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EligibilityResultsUiState())
    val uiState: StateFlow<EligibilityResultsUiState> = _uiState

    init {
        loadResults()
    }

    fun loadResults() {
        viewModelScope.launch {
            _uiState.value = EligibilityResultsUiState(isLoading = true)
            
            val profileRes = profileRepo.getProfile()
            if (profileRes.isSuccess) {
                val p = profileRes.getOrThrow()
                schemeRepo.evaluateEligibility(p)
                    .onSuccess { results ->
                        val query = p.interestedScheme?.lowercase()?.trim()
                        val filteredResults = results.filter { it.status == "Eligible" || it.status == "Partially Eligible" }
                        
                        val matches = if (!query.isNullOrEmpty()) {
                            filteredResults.filter { 
                                it.schemeName.lowercase().contains(query)
                            }.take(3)
                        } else emptyList()

                        _uiState.value = EligibilityResultsUiState(
                            isLoading = false,
                            results = filteredResults,
                            interestedMatches = matches,
                            interestedScheme = p.interestedScheme,
                            profile = p
                        )
                    }
                    .onFailure {
                        _uiState.value = EligibilityResultsUiState(isLoading = false, error = it.message)
                    }
            } else {
                _uiState.value = EligibilityResultsUiState(isLoading = false, error = "Failed to load profile")
            }
        }
    }
}
