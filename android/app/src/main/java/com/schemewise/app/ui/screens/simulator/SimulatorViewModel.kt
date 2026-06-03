package com.schemewise.app.ui.screens.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SimulatorUiState(
    val isLoading:  Boolean              = false,
    val results:    List<EligibilityResult> = emptyList(),
    val isDone:     Boolean              = false,
    val error:      String?              = null,
)

@HiltViewModel
class SimulatorViewModel @Inject constructor(
    private val schemeRepo: SchemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SimulatorUiState())
    val uiState: StateFlow<SimulatorUiState> = _uiState

    fun runSimulation(profile: Profile) {
        viewModelScope.launch {
            _uiState.value = SimulatorUiState(isLoading = true)
            schemeRepo.evaluateEligibility(profile)
                .onSuccess { _uiState.value = SimulatorUiState(results = it, isDone = true) }
                .onFailure { _uiState.value = SimulatorUiState(error = it.message, isDone = false) }
        }
    }

    fun reset() { _uiState.value = SimulatorUiState() }
}
