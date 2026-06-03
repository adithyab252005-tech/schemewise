package com.schemewise.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role")    val role:    String,  // "user" or "assistant"
    @Json(name = "content") val content: String,
)

@JsonClass(generateAdapter = true)
data class ChatRequest(
    @Json(name = "message") val message: String,
    @Json(name = "history") val history: List<ChatMessage> = emptyList(),
    @Json(name = "user_profile") val userProfile: Profile? = null
)

@JsonClass(generateAdapter = true)
data class ChatResponse(
    @Json(name = "reply")   val reply:   String,
    @Json(name = "sources") val sources: List<String>? = null,
)
