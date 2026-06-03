package com.schemewise.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id")       val id:      Int,
    @Json(name = "name")     val name:    String,
    @Json(name = "email")    val email:   String,
    @Json(name = "is_admin") val isAdmin: Boolean = false,
    
    // Auth Hydration Fields
    @Json(name = "studentLevel")       val studentLevel:       String? = null,
    @Json(name = "studentCourse")      val studentCourse:      String? = null,
    @Json(name = "isFarmer")           val isFarmer:           String? = "No",
    @Json(name = "employmentType")     val employmentType:     String? = null,
)

@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "email")    val email:    String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "name")     val name:     String,
    @Json(name = "email")    val email:    String,
    @Json(name = "password") val password: String,
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token")   val token:   String? = null,
    @Json(name = "user")    val user:    User?   = null,
    @Json(name = "message") val message: String? = null,
)

@JsonClass(generateAdapter = true)
data class VerifyEmailResponse(
    @Json(name = "message") val message: String? = null,
    @Json(name = "user")    val user:    User?   = null,
)
