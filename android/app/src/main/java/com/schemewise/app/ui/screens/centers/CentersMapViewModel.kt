package com.schemewise.app.ui.screens.centers

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

data class CentersMapUiState(
    val isLoading: Boolean = true,
    val profile: Profile? = null,
    val error: String? = null
)

@HiltViewModel
class CentersMapViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val prefs: PrefsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CentersMapUiState())
    val uiState: StateFlow<CentersMapUiState> = _uiState

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                if (prefs.userId != -1) {
                    val p = profileRepo.getProfile().getOrNull()
                    _uiState.value = CentersMapUiState(isLoading = false, profile = p)
                } else {
                    _uiState.value = CentersMapUiState(isLoading = false, error = "User not logged in")
                }
            } catch (e: Exception) {
                // If network fails, try to load offline profile from prefs? 
                // We'll just leave it null if offline, the UI handles missing location gracefully.
                _uiState.value = CentersMapUiState(isLoading = false, error = e.localizedMessage)
            }
        }
    }
}
