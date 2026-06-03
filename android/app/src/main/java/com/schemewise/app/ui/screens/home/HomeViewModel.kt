package com.schemewise.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.data.repository.ProfileRepository
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading:     Boolean              = true,
    val eligibleCount: Int                  = 0,
    val topPicks:      List<EligibilityResult> = emptyList(),
    val savedSchemes:  List<Scheme>         = emptyList(),
    val profile:       com.schemewise.app.data.model.Profile? = null,
    val civicScore:    Int                  = 0,
    val profileCompletion: Int              = 0,
    val error:         String?              = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val schemeRepo:  SchemeRepository,
    private val profileRepo: ProfileRepository,
    private val prefs:       PrefsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    val userName: String get() = prefs.userName ?: "Citizen"
    val userId:   Int    get() = prefs.userId

    init { loadDashboard() }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            try {
                val saved = schemeRepo.getSavedSchemes(userId).getOrDefault(emptyList())
                
                // Fetch profile to run true eligibility check via backend
                val profileRes = profileRepo.getProfile()
                val profileObj = profileRes.getOrNull()
                
                var totalEvaluated = 0
                val eligibleMatches = if (profileRes.isSuccess && profileObj != null) {
                    val allResults = schemeRepo.evaluateEligibility(profileObj).getOrDefault(emptyList())
                    totalEvaluated = allResults.size
                    allResults
                        .filter { it.status == "Eligible" || it.status == "Partially Eligible" }
                        .sortedByDescending { it.scorePercentage }
                } else emptyList()

                val eligibleOnly = eligibleMatches.count { it.status == "Eligible" }
                val civicScoreVal = if (totalEvaluated > 0) ((eligibleOnly.toFloat() / totalEvaluated) * 100).toInt() else 0
                
                var profileCompletionVal = 0
                if (profileObj != null) {
                    val fields = listOf(
                        profileObj.name, profileObj.dob, profileObj.age?.toString(), profileObj.gender,
                        profileObj.income?.toString(), profileObj.occupation,
                        profileObj.state, profileObj.ruralUrban, profileObj.category
                    )
                    val filled = fields.count { !it.isNullOrBlank() }
                    profileCompletionVal = ((filled.toFloat() / fields.size) * 100).toInt()
                }

                _uiState.value = HomeUiState(
                    isLoading     = false,
                    eligibleCount = eligibleOnly,
                    topPicks      = eligibleMatches.take(4),
                    savedSchemes  = saved,
                    profile       = profileObj,
                    civicScore    = civicScoreVal,
                    profileCompletion = profileCompletionVal
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(isLoading = false, error = e.message)
            }
        }
    }
}
