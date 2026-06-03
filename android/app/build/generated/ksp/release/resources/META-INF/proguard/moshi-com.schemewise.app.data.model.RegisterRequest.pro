-if class com.schemewise.app.data.model.RegisterRequest
-keepnames class com.schemewise.app.data.model.RegisterRequest
-if class com.schemewise.app.data.model.RegisterRequest
-keep class com.schemewise.app.data.model.RegisterRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
