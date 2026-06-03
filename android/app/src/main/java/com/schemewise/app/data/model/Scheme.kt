package com.schemewise.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Scheme(
    @Json(name = "scheme_id")          val schemeId:           Int,
    @Json(name = "scheme_name")        val schemeName:         String,
    @Json(name = "description")        val description:        String? = null,
    @Json(name = "ministry")           val ministry:           String? = null,
    @Json(name = "scheme_category")    val schemeCategory:     String? = null,
    @Json(name = "scheme_type")        val schemeType:         String? = null,
    @Json(name = "state_applicable")   val stateApplicable:    String? = null,
    @Json(name = "status")             val status:             String? = null,
    @Json(name = "last_updated")       val lastUpdated:        String? = null,
    @Json(name = "eligible_categories") val eligibleCategories: Any? = null,
    @Json(name = "occupation_required") val occupationRequired: Any? = null,
    @Json(name = "rural_urban")        val ruralUrban:         String? = null,
    @Json(name = "target_gender")      val targetGender:       String? = "All",
    @Json(name = "target_age_min")     val targetAgeMin:       Double? = null,
    @Json(name = "target_age_max")     val targetAgeMax:       Double? = null,
    @Json(name = "income_min")         val incomeMin:          Double? = null,
    @Json(name = "income_max")         val incomeMax:          Double? = null,
    @Json(name = "apply_url")          val applyUrl:           String? = null,
    @Json(name = "source_url")         val sourceUrl:          String? = null,
    @Json(name = "saved_at")           val savedAt:            String? = null,
    @Json(name = "improvement_suggestion") val improvementSuggestion: String? = null,
)
