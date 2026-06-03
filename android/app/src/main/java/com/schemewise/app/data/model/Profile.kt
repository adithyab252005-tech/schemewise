package com.schemewise.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Profile(
    @Json(name = "id")                 val id:                 Int?    = null,
    @Json(name = "user_id")            val userId:             Int?    = null,
    @Json(name = "name")               val name:               String? = null,
    @Json(name = "age")                val age:                Double? = null,
    @Json(name = "dob")                val dob:                String? = null,
    @Json(name = "gender")             val gender:             String? = null,
    @Json(name = "state")              val state:              String? = null,
    @Json(name = "category")           val category:           String? = null,
    @Json(name = "occupation")         val occupation:         String? = null,
    @Json(name = "income")             val income:             Double? = null,
    @Json(name = "ruralUrban")         val ruralUrban:         String? = null,
    @Json(name = "isStudent")          val isStudent:          String? = "No",
    @Json(name = "isBPL")              val isBPL:              String? = "No",
    @Json(name = "isDifferentlyAbled") val isDifferentlyAbled: String? = "No",
    @Json(name = "maritalStatus")      val maritalStatus:      String? = null,
    @Json(name = "interestedScheme")   val interestedScheme:   String? = null,
    
    // Student detail fields (Hardening Sync)
    @Json(name = "studentLevel")       val studentLevel:       String? = null,
    @Json(name = "studentClass")       val studentClass:       String? = null,
    @Json(name = "studentDegreeType")  val studentDegreeType:  String? = null,
    @Json(name = "studentCourse")      val studentCourse:      String? = null,
    
    // Employment & Farmer fields
    @Json(name = "isFarmer")           val isFarmer:           String? = "No",
    @Json(name = "employmentType")     val employmentType:     String? = null,
    
    // JSONB Array
    @Json(name = "hasDocuments")       val hasDocuments:       List<String> = emptyList(),
    
    // Location Detail Fields (matching web DetailsFillingPage.jsx)
    @Json(name = "district")           val district:           String? = null,
    @Json(name = "city")               val city:               String? = null,
    @Json(name = "area")               val area:               String? = null,
)
