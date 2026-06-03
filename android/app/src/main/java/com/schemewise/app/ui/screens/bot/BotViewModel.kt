package com.schemewise.app.ui.screens.bot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.api.ApiService
import com.schemewise.app.data.model.ChatMessage
import com.schemewise.app.data.model.ChatRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BotViewModel @Inject constructor(
    private val api: ApiService,
    private val profileRepo: com.schemewise.app.data.repository.ProfileRepository
) : ViewModel() {

    private val _messages  = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage("user", text)
        _messages.value = _messages.value + userMsg
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val profile = profileRepo.getProfile().getOrNull()
                val response = api.chat(ChatRequest(text, _messages.value.dropLast(1), profile))
                if (response.isSuccessful) {
                    _messages.value = _messages.value + ChatMessage("assistant", response.body()!!.reply)
                } else {
                    _messages.value = _messages.value + ChatMessage("assistant", "Sorry, I couldn't process that. Please try again.")
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("assistant", "Connection error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
