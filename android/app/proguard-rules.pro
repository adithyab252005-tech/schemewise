# Retrofit / Moshi
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# App models (keep for JSON parsing)
-keep class com.schemewise.app.data.model.** { *; }
-keep class com.schemewise.app.data.remote.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-dontwarn kotlin.**
