package com.schemewise.app.data.repository

import com.schemewise.app.data.api.ApiService
import com.schemewise.app.data.model.EligibilityResult
import com.schemewise.app.data.model.Profile
import com.schemewise.app.data.model.Scheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SchemeRepository @Inject constructor(private val api: ApiService) {

    /** Fetches all active schemes. Client-side filtering handles all other params. */
    suspend fun getSchemes(): Result<List<Scheme>> = runCatching {
        val r = api.getSchemes()
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    suspend fun getCategories(state: String?): Result<List<String>> = runCatching {
        val r = api.getCategories(state)
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    suspend fun getSchemeDetail(schemeId: String): Result<Scheme> = runCatching {
        val r = api.getSchemeDetail(schemeId)
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    /** Evaluate eligibility — returns EligibilityResult list, not full Scheme objects. */
    suspend fun evaluateEligibility(profile: Profile): Result<List<EligibilityResult>> = runCatching {
        val r = api.evaluateEligibility(profile)
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    suspend fun getSavedSchemes(userId: Int): Result<List<Scheme>> = runCatching {
        val r = api.getSavedSchemes(userId)
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    suspend fun saveScheme(userId: Int, schemeId: String): Result<Unit> = runCatching {
        val r = api.saveScheme(schemeId, mapOf("user_id" to userId))
        if (!r.isSuccessful) error("HTTP ${r.code()}")
    }

    suspend fun removeSavedScheme(userId: Int, schemeId: String): Result<Unit> = runCatching {
        val r = api.removeSavedScheme(schemeId, userId)
        if (!r.isSuccessful) error("HTTP ${r.code()}")
    }

    suspend fun fetchDetailedExplanation(schemeId: String, schemeName: String, profile: com.schemewise.app.data.model.Profile?): Result<String> = runCatching {
        // 1. Fetch raw details via scraper endpoint
        val r = api.fetchAiSchemeDetails(schemeId)
        val extractedText = if (r.isSuccessful) r.body()?.contentHash ?: "No additional details found." else "No additional details found."
        
        // 2. Generate personalized explanation using LLM chat endpoint
        val basePrompt = "Please provide a highly detailed, step-by-step guide on how and where to apply for the scheme \"$schemeName\". Include exact document requirements, portal URLs if possible, and specific application procedures. Structure it in a professional, easily actionable format based on the following scheme details. DO NOT use markdown formatting like asterisks or hashes. Use plain text with simple dashes for lists:"
        val prompt = "$basePrompt\n\n$extractedText"
        
        val chatReq = com.schemewise.app.data.model.ChatRequest(
            message = prompt,
            history = emptyList(),
            userProfile = profile
        )
        
        val chatRes = api.chat(chatReq)
        if (chatRes.isSuccessful) {
            chatRes.body()?.reply ?: error("Empty AI response")
        } else {
            error("AI Fetch HTTP Error: ${chatRes.code()}")
        }
    }

    suspend fun fetchAnomalyCheck(schemeText: String, profile: com.schemewise.app.data.model.Profile): Result<List<String>> = runCatching {
        val req = com.schemewise.app.data.api.AnomalyCheckRequest(userProfile = profile, schemeText = schemeText)
        val r = api.checkAnomaly(req)
        if (r.isSuccessful) {
            val body = r.body() ?: error("Empty AI response")
            if (body.error != null) error(body.error)
            body.anomalies
        } else {
            error("AI Fetch HTTP Error: ${r.code()}")
        }
    }

    suspend fun getUpdates(): Result<List<Map<String, String>>> = runCatching {
        val r = api.getUpdates()
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }

    suspend fun compareSchemes(sidA: String, sidB: String, profile: Profile): Result<com.schemewise.app.data.api.CompareResponse> = runCatching {
        val r = api.compareSchemes(com.schemewise.app.data.api.CompareRequest(sidA, sidB, profile))
        if (r.isSuccessful) r.body()!! else error("HTTP ${r.code()}")
    }
}
