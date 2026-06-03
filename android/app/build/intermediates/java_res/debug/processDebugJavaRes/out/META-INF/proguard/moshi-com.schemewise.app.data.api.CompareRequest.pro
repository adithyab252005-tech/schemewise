-if class com.schemewise.app.data.api.CompareRequest
-keepnames class com.schemewise.app.data.api.CompareRequest
-if class com.schemewise.app.data.api.CompareRequest
-keep class com.schemewise.app.data.api.CompareRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
