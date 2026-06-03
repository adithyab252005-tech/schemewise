-if class com.schemewise.app.data.model.AuthRequest
-keepnames class com.schemewise.app.data.model.AuthRequest
-if class com.schemewise.app.data.model.AuthRequest
-keep class com.schemewise.app.data.model.AuthRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
