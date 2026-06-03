package com.schemewise.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.repository.AuthRepository
import com.schemewise.app.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.schemewise.app.data.repository.SchemeRepository

data class ProfileUiState(
    val isLoading:  Boolean  = true,
    val profile:    Profile? = null,
    val saveSuccess:Boolean  = false,
    val error:      String?  = null,
    val isLoggedOut:Boolean  = false,
    val dynamicCastes: List<String> = listOf("General", "OBC", "SC", "ST", "Minority", "Prefer not to say")
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val schemeRepo:  SchemeRepository,
    private val authRepo:    AuthRepository,
    val prefs:               PrefsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init { loadProfile() }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState(isLoading = true)
            profileRepo.getProfile()
                .onSuccess { _uiState.value = ProfileUiState(isLoading = false, profile = it) }
                .onFailure { _uiState.value = ProfileUiState(isLoading = false, error = it.message) }
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            profileRepo.saveProfile(profile)
                .onSuccess { _uiState.value = _uiState.value.copy(saveSuccess = true, profile = it) }
                .onFailure { _uiState.value = _uiState.value.copy(error = it.message) }
        }
    }

    fun logout() {
        authRepo.logout()
        _uiState.value = _uiState.value.copy(isLoggedOut = true)
    }

    fun fetchCategories(state: String?) {
        viewModelScope.launch {
            schemeRepo.getCategories(state)
                .onSuccess { castes ->
                    _uiState.value = _uiState.value.copy(dynamicCastes = castes)
                }
                .onFailure {
                    // Fallback
                }
        }
    }
}
