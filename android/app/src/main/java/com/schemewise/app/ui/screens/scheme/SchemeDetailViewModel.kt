package com.schemewise.app.ui.screens.scheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.data.repository.ProfileRepository
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SchemeDetailUiState(
    val isLoading:   Boolean = true,
    val scheme:      Scheme?  = null,
    val matchStatus: String?  = null,
    val suggestion:  String?  = null,
    val isSaving:    Boolean = false,
    val error:       String?  = null,
    val saveMessage: String?  = null,
    val isFetchingAiDetails: Boolean = false,
    val aiDetails:   String?  = null,
    val isFetchingAnomalies: Boolean = false,
    val anomalies:   List<String>? = null
)

@HiltViewModel
class SchemeDetailViewModel @Inject constructor(
    private val schemeRepo:  SchemeRepository,
    private val profileRepo: ProfileRepository,
    private val prefs:       PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SchemeDetailUiState())
    val uiState: StateFlow<SchemeDetailUiState> = _uiState

    fun loadDetails(schemeId: String) {
        viewModelScope.launch {
            _uiState.value = SchemeDetailUiState(isLoading = true)

            // Step 1: Load scheme detail first and show it immediately
            val detailRes = schemeRepo.getSchemeDetail(schemeId)
            if (detailRes.isFailure) {
                _uiState.value = SchemeDetailUiState(
                    isLoading = false,
                    error = "Failed to load scheme: ${detailRes.exceptionOrNull()?.message}"
                )
                return@launch
            }

            val s = detailRes.getOrThrow()
            // Show scheme immediately without waiting for eligibility
            _uiState.value = SchemeDetailUiState(isLoading = false, scheme = s)

            // Step 2: Async eligibility check — update UI if successful, never block
            try {
                val profileRes = profileRepo.getProfile()
                if (profileRes.isSuccess) {
                    val p = profileRes.getOrThrow()
                    val eligRes = schemeRepo.evaluateEligibility(p)
                    if (eligRes.isSuccess) {
                        val match = eligRes.getOrThrow().find { it.schemeId == s.schemeId }
                        if (match != null) {
                            _uiState.value = _uiState.value.copy(
                                matchStatus = match.status,
                                suggestion  = match.improvementSuggestion
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Silently fail — scheme detail is still visible
            }
        }
    }


    fun saveToHub(context: android.content.Context) {
        val s = _uiState.value.scheme ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            schemeRepo.saveScheme(prefs.userId, s.schemeId.toString())
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveMessage = "Saved to Hub (Deadline Alert Scheduled)")
                    // Schedule a deadline notification 14 days out for demo
                    com.schemewise.app.util.DeadlineAlertEngine.scheduleAlert(
                        context = context,
                        schemeId = s.schemeId.toString(),
                        schemeName = s.schemeName,
                        daysUntilDeadline = 14L
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isSaving = false, saveMessage = "Failed to save")
                }
        }
    }

    fun fetchAiDetails() {
        val s = _uiState.value.scheme ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingAiDetails = true, aiDetails = null, error = null)
            
            try {
                // Fetch profile to personalize the AI response
                val profileRes = profileRepo.getProfile()
                val profileParams = if (profileRes.isSuccess) profileRes.getOrNull() else null
                
                val result = schemeRepo.fetchDetailedExplanation(s.schemeId.toString(), s.schemeName, profileParams)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isFetchingAiDetails = false,
                        aiDetails = result.getOrThrow()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isFetchingAiDetails = false,
                        error = "Failed to fetch AI details: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isFetchingAiDetails = false,
                    error = "Failed to fetch AI details"
                )
            }
        }
    }

    fun runPreFlightCheck() {
        val s = _uiState.value.scheme ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingAnomalies = true, anomalies = null)
            try {
                val profileRes = profileRepo.getProfile()
                if (profileRes.isSuccess) {
                    val p = profileRes.getOrThrow()
                    // Create a composite string of scheme rules
                    val schemeText = "Name: ${s.schemeName}\nRules: ${s.description ?: ""}\nAge Limit: ${s.targetAgeMin}-${s.targetAgeMax}\nIncome Max: ${s.incomeMax}\nCategory: ${s.eligibleCategories ?: ""}"
                    val result = schemeRepo.fetchAnomalyCheck(schemeText, p)
                    if (result.isSuccess) {
                        _uiState.value = _uiState.value.copy(
                            isFetchingAnomalies = false,
                            anomalies = result.getOrThrow()
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isFetchingAnomalies = false)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isFetchingAnomalies = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isFetchingAnomalies = false)
            }
        }
    }
}
