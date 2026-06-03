package com.schemewise.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.schemewise.app.data.repository.SchemeRepository

data class DetailsFillingUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error:     String? = null,
    val dynamicCastes: List<String> = listOf("General", "OBC", "SC", "ST", "Minority")
)

@HiltViewModel
class DetailsFillingViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val schemeRepo:  SchemeRepository,
    private val prefs:       PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsFillingUiState())
    val uiState: StateFlow<DetailsFillingUiState> = _uiState

    fun saveDetails(profile: Profile) {
        viewModelScope.launch {
            _uiState.value = DetailsFillingUiState(isLoading = true)
            // ensure the userId and name are populated correctly from prefs if missing
            val fullProfile = profile.copy(
                userId = prefs.userId,
                name = profile.name ?: prefs.userName
            )
            
            profileRepo.saveProfile(fullProfile)
                .onSuccess { saved ->
                    // Optionally update prefs if name changed
                    saved.name?.let { prefs.userName = it }
                    _uiState.value = DetailsFillingUiState(isSuccess = true)
                }
                .onFailure { err ->
                    _uiState.value = DetailsFillingUiState(isLoading = false, error = err.message ?: "Failed to save profile")
                }
        }
    }

    fun fetchCategories(state: String) {
        viewModelScope.launch {
            schemeRepo.getCategories(state)
                .onSuccess { castes ->
                    _uiState.value = _uiState.value.copy(dynamicCastes = castes)
                }
                .onFailure {
                    // Fallback handled by default state
                }
        }
    }
}
