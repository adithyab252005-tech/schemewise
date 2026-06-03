package com.schemewise.app.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedUiState(
    val isLoading: Boolean      = true,
    val schemes:   List<Scheme> = emptyList(),
    val error:     String?      = null,
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val schemeRepo: SchemeRepository,
    private val prefs:      PrefsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedUiState())
    val uiState: StateFlow<SavedUiState> = _uiState

    init { loadSaved() }

    fun loadSaved() {
        viewModelScope.launch {
            _uiState.value = SavedUiState(isLoading = true)
            schemeRepo.getSavedSchemes(prefs.userId)
                .onSuccess { _uiState.value = SavedUiState(isLoading = false, schemes = it) }
                .onFailure { _uiState.value = SavedUiState(isLoading = false, error = it.message) }
        }
    }

    fun remove(schemeId: String) {
        viewModelScope.launch {
            schemeRepo.removeSavedScheme(prefs.userId, schemeId).onSuccess { loadSaved() }
        }
    }
}
