package com.schemewise.app.ui.screens.scheme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.model.Scheme
import com.schemewise.app.data.repository.ProfileRepository
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CompareUiState(
    val isLoading:   Boolean              = false,
    val schemes:     List<Scheme>         = emptyList(),
    val verdict:     String?              = null,
    val schemeA:     Scheme?              = null,
    val schemeB:     Scheme?              = null,
    val error:       String?              = null,
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val schemeRepo:  SchemeRepository,
    private val profileRepo: ProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState

    init {
        loadSchemes()
    }

    private fun loadSchemes() {
        viewModelScope.launch {
            schemeRepo.getSchemes().onSuccess { list ->
                _uiState.value = _uiState.value.copy(schemes = list)
            }
        }
    }

    fun selectSchemeA(schemeId: String) {
        val s = _uiState.value.schemes.find { it.schemeId.toString() == schemeId }
        _uiState.value = _uiState.value.copy(schemeA = s, verdict = null)
    }

    fun selectSchemeB(schemeId: String) {
        val s = _uiState.value.schemes.find { it.schemeId.toString() == schemeId }
        _uiState.value = _uiState.value.copy(schemeB = s, verdict = null)
    }

    fun compare() {
        val sA = _uiState.value.schemeA ?: return
        val sB = _uiState.value.schemeB ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val profileRes = profileRepo.getProfile()
            val profile = profileRes.getOrNull() ?: Profile() // Fallback to empty profile
            
            schemeRepo.compareSchemes(sA.schemeId.toString(), sB.schemeId.toString(), profile)
                .onSuccess { res ->
                    _uiState.value = _uiState.value.copy(isLoading = false, verdict = res.verdict)
                }
                .onFailure { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                }
        }
    }
}
