-if class com.schemewise.app.data.api.ProfileResponse
-keepnames class com.schemewise.app.data.api.ProfileResponse
-if class com.schemewise.app.data.api.ProfileResponse
-keep class com.schemewise.app.data.api.ProfileResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
