package com.schemewise.app.data.repository

import com.schemewise.app.data.api.ApiService
import com.schemewise.app.data.local.PrefsManager
import com.schemewise.app.data.model.Profile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val api:   ApiService,
    private val prefs: PrefsManager,
) {
    suspend fun getProfile(): Result<Profile> = runCatching {
        val r = api.getProfile(prefs.userId)
        if (r.isSuccessful) r.body()!!.user else error("HTTP ${r.code()}")
    }

    suspend fun saveProfile(profile: Profile): Result<Profile> = runCatching {
        val r = api.saveProfile(prefs.userId, profile)
        if (r.isSuccessful) r.body()!!.user else error("HTTP ${r.code()}")
    }
}
