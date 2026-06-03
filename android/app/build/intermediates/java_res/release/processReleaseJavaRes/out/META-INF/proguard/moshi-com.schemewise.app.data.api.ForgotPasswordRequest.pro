-if class com.schemewise.app.data.api.ForgotPasswordRequest
-keepnames class com.schemewise.app.data.api.ForgotPasswordRequest
-if class com.schemewise.app.data.api.ForgotPasswordRequest
-keep class com.schemewise.app.data.api.ForgotPasswordRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
