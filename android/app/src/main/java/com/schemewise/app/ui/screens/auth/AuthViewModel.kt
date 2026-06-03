package com.schemewise.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schemewise.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading:  Boolean = false,
    val isSuccess:  Boolean = false,
    val emailSent:  Boolean = false,   // Registration: verification email dispatched
    val isVerified: Boolean = false,   // Registration: OTP verified successfully
    val sentToEmail:String  = "",
    val error:      String? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepo.login(email, password)
                .onSuccess { _uiState.value = AuthUiState(isSuccess = true) }
                .onFailure { _uiState.value = AuthUiState(error = it.message ?: "Login failed") }
        }
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepo.register(name, email, password)
                .onSuccess { _uiState.value = AuthUiState(emailSent = true, sentToEmail = email) }
                .onFailure { _uiState.value = AuthUiState(error = it.message ?: "Registration failed") }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true, emailSent = true, sentToEmail = email)
            authRepo.verifyOtp(email, otp)
                .onSuccess { _uiState.value = AuthUiState(isVerified = true) }
                .onFailure { _uiState.value = AuthUiState(emailSent = true, sentToEmail = email, error = it.message ?: "Verification failed") }
        }
    }
}

// Ensure the old VerifyEmailViewModel is deleted since we use OTP inside RegisterScreen now.

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {
    suspend fun forgotPassword(email: String) = authRepo.forgotPassword(email)
    suspend fun resetPassword(token: String, newPassword: String?) = authRepo.resetPassword(token, newPassword)
}

