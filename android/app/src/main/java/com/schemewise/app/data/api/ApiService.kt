package com.schemewise.app.data.api

import com.schemewise.app.data.model.AuthRequest
import com.schemewise.app.data.model.AuthResponse
import com.schemewise.app.data.model.ChatRequest
import com.schemewise.app.data.model.ChatResponse
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.model.RegisterRequest
import com.schemewise.app.data.model.Scheme
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ── Auth (Matching your Flask users_bp) ──────────────────────────
    @POST("api/v1/users/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    @POST("api/v1/users/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<Map<String, String>>

    @POST("api/v1/users/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<AuthResponse>

    // Added trailing slash back. Flask is strict about "/" matching route("/")
    @POST("api/v1/users/")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/v1/users/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<AuthResponse>

    // ── Profile ───────────────────────────────────────────────────────
    @GET("api/v1/users/{userId}")
    suspend fun getProfile(@Path("userId") userId: Int): Response<ProfileResponse>

    @POST("api/v1/users/{userId}")
    suspend fun saveProfile(
        @Path("userId") userId: Int,
        @Body profile: Profile
    ): Response<ProfileResponse>

    // ── Schemes ───────────────────────────────────────────────────────
    // Backend /api/v1/schemes only supports: state, active_only
    @GET("api/v1/schemes")
    suspend fun getSchemes(
        @Query("state")        state:      String?  = null,
        @Query("active_only")  activeOnly: Boolean  = false,
    ): Response<List<Scheme>>

    @GET("api/v1/schemes/{schemeId}")
    suspend fun getSchemeDetail(@Path("schemeId") schemeId: String): Response<Scheme>

    @POST("api/v1/schemes/{schemeId}/fetch-details")
    suspend fun fetchAiSchemeDetails(@Path("schemeId") schemeId: String): Response<FetchDetailsResponse>

    @GET("api/v1/categories")
    suspend fun getCategories(@Query("state") state: String?): Response<List<String>>

    // ── Eligibility ───────────────────────────────────────────────────
    // Blueprint prefix: /api/v1, route: /evaluate → full path: /api/v1/evaluate
    // Backend returns {scheme_id, scheme_name, status, score_percentage, ...} NOT full Scheme
    @POST("api/v1/evaluate")
    suspend fun evaluateEligibility(@Body profile: Profile): Response<List<EligibilityResult>>

    // ── Saved Schemes ─────────────────────────────────────────────────
    @GET("api/v1/saved_schemes")
    suspend fun getSavedSchemes(@Query("user_id") userId: Int): Response<List<Scheme>>

    @POST("api/v1/schemes/{schemeId}/save")
    suspend fun saveScheme(
        @Path("schemeId") schemeId: String,
        @Body request: Map<String, Int> // To pass {"user_id": userId}
    ): Response<Unit>

    @DELETE("api/v1/schemes/{schemeId}/save")
    suspend fun removeSavedScheme(
        @Path("schemeId") schemeId: String,
        @Query("user_id") userId: Int
    ): Response<Unit>

    // ── Bot ───────────────────────────────────────────────────────────
    @POST("api/v1/chat")
    suspend fun chat(@Body request: ChatRequest): Response<ChatResponse>

    @POST("api/v1/anomaly-check")
    suspend fun checkAnomaly(@Body request: AnomalyCheckRequest): Response<AnomalyCheckResponse>

    // ── Updates ───────────────────────────────────────────────────────
    @GET("api/v1/updates")
    suspend fun getUpdates(): Response<List<Map<String, String>>>

    @POST("api/v1/chat/compare")
    suspend fun compareSchemes(@Body request: CompareRequest): Response<CompareResponse>
}

@JsonClass(generateAdapter = true)
data class ProfileResponse(val user: Profile)

@JsonClass(generateAdapter = true)
data class ForgotPasswordRequest(val email: String)

@JsonClass(generateAdapter = true)
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String? = null   // null = magic login, non-null = set password
)

@JsonClass(generateAdapter = true)
data class FetchDetailsResponse(
    @Json(name = "content_hash") val contentHash: String?,
    @Json(name = "error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

@JsonClass(generateAdapter = true)
data class AnomalyCheckRequest(
    @Json(name = "user_profile") val userProfile: Profile,
    @Json(name = "scheme_text") val schemeText: String
)

@JsonClass(generateAdapter = true)
data class AnomalyCheckResponse(
    val anomalies: List<String>,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class CompareRequest(
    @Json(name = "scheme_id_a") val schemeIdA: String,
    @Json(name = "scheme_id_b") val schemeIdB: String,
    @Json(name = "user_profile") val userProfile: Profile
)

@JsonClass(generateAdapter = true)
data class CompareResponse(
    @Json(name = "scheme_a") val schemeA: String,
    @Json(name = "scheme_b") val schemeB: String,
    @Json(name = "verdict") val verdict: String,
    val error: String? = null
)
