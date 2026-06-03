package com.schemewise.app.ui.screens.updates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.repository.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UpdatesUiState(
    val isLoading: Boolean = true,
    val updates: List<Map<String, String>> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val schemeRepository: SchemeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UpdatesUiState())
    val uiState: StateFlow<UpdatesUiState> = _uiState

    init {
        loadUpdates()
    }

    fun loadUpdates() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val updates = schemeRepository.getUpdates().getOrDefault(emptyList())
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    updates = updates
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
