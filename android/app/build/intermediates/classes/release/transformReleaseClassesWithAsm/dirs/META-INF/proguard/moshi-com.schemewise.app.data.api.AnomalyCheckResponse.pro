-if class com.schemewise.app.data.api.AnomalyCheckResponse
-keepnames class com.schemewise.app.data.api.AnomalyCheckResponse
-if class com.schemewise.app.data.api.AnomalyCheckResponse
-keep class com.schemewise.app.data.api.AnomalyCheckResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.api.AnomalyCheckResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.api.AnomalyCheckResponse
-keepclassmembers class com.schemewise.app.data.api.AnomalyCheckResponse {
    public synthetic <init>(java.util.List,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
