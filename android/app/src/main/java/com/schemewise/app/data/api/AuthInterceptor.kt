package com.schemewise.app.data.api

import com.schemewise.app.data.local.PrefsManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches the JWT Bearer token to every request if the user is logged in.
 * Mirrors the web app's axios interceptor that adds Authorization headers.
 */
class AuthInterceptor @Inject constructor(
    private val prefs: PrefsManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.token
        // Always request English from the backend regardless of device language.
        // The app UI is in English; the backend falls back to Hindi/Tamil based on Accept-Language.
        val builder = chain.request().newBuilder()
            .addHeader("Accept-Language", "en")
        if (!token.isNullOrBlank()) {
            builder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(builder.build())
    }
}
