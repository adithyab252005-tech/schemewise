package com.schemewise.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Mirrors the response from POST /api/v1/evaluate
 * The backend returns lite eligibility result objects (not full Scheme objects).
 */
@JsonClass(generateAdapter = true)
data class EligibilityResult(
    @Json(name = "scheme_id")               val schemeId:              Int,
    @Json(name = "scheme_name")             val schemeName:            String,
    @Json(name = "status")                  val status:                String,  // "Eligible" | "Partially Eligible" | "Not Eligible"
    @Json(name = "score_percentage")        val scorePercentage:       Int      = 0,
    @Json(name = "missing_conditions")      val missingConditions:     List<String>? = null,
    @Json(name = "improvement_suggestion")  val improvementSuggestion: String?  = null,
    @Json(name = "impact_score")            val impactScore:           Int      = 0,
    @Json(name = "scheme_category")         val schemeCategory:        String?  = null,
    @Json(name = "state_applicable")        val stateApplicable:       String?  = null,
    @Json(name = "max_financial_value_inr") val maxFinancialValueInr:  Int      = 0,
    @Json(name = "target_gender")           val targetGender:          String?  = null,  // "Male" | "Female" | "All"
)
