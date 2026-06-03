package com.schemewise.app.data.repository

import com.schemewise.app.data.api.ApiService
import com.schemewise.app.data.api.VerifyOtpRequest
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.AuthRequest
import com.schemewise.app.data.model.AuthResponse
import com.schemewise.app.data.model.RegisterRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api:   ApiService,
    private val prefs: PrefsManager,
) {
    suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(AuthRequest(email, password))
        if (response.isSuccessful) {
            response.body()!!.also { auth ->
                prefs.token    = auth.token
                prefs.userId   = auth.user?.id ?: -1
                prefs.userName = auth.user?.name
                prefs.isAdmin  = auth.user?.isAdmin ?: false
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val msg = when {
                response.code() == 403 -> "Please verify your email before logging in."
                errorBody.contains("message") -> errorBody.substringAfter("\"message\":\"").substringBefore("\"")
                else -> "Login failed (${response.code()})"
            }
            error(msg)
        }
    }

    // Returns a Result<String> — the success message (not a user, user is unverified until they click email link)
    suspend fun register(name: String, email: String, password: String): Result<String> =
        runCatching {
            val response = api.register(RegisterRequest(name, email, password))
            if (response.isSuccessful) {
                response.body()?.message ?: "Check your email to verify your account."
            } else {
                val code = response.code()
                val errorBody = response.errorBody()?.string() ?: ""
                val msg = when {
                    errorBody.contains("error") -> errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                    else -> "Registration failed ($code)"
                }
                error(msg)
            }
        }

    fun logout() = prefs.clear()

    suspend fun verifyOtp(email: String, otp: String): Result<AuthResponse> = runCatching {
        val response = api.verifyOtp(VerifyOtpRequest(email, otp))
        if (response.isSuccessful) {
            response.body()!!.also { auth ->
                prefs.token    = auth.token
                prefs.userId   = auth.user?.id ?: -1
                prefs.userName = auth.user?.name
                prefs.isAdmin  = auth.user?.isAdmin ?: false
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val msg = when {
                errorBody.contains("error") -> errorBody.substringAfter("\"error\":\"").substringBefore("\"")
                else -> "Verification failed (${response.code()})"
            }
            error(msg)
        }
    }

    suspend fun forgotPassword(email: String) = runCatching {
        val res = api.forgotPassword(com.schemewise.app.data.api.ForgotPasswordRequest(email))
        if (res.isSuccessful) res.body()!! else error("Request failed")
    }

    suspend fun resetPassword(token: String, newPassword: String?) = runCatching {
        val res = api.resetPassword(com.schemewise.app.data.api.ResetPasswordRequest(token, newPassword))
        if (res.isSuccessful) res.body()!! else error("Request failed")
    }

    val isLoggedIn: Boolean get() = prefs.isLoggedIn
}
