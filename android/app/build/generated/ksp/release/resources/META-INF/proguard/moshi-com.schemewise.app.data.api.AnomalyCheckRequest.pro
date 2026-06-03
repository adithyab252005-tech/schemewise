-if class com.schemewise.app.data.api.AnomalyCheckRequest
-keepnames class com.schemewise.app.data.api.AnomalyCheckRequest
-if class com.schemewise.app.data.api.AnomalyCheckRequest
-keep class com.schemewise.app.data.api.AnomalyCheckRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
