-if class com.schemewise.app.data.api.CompareResponse
-keepnames class com.schemewise.app.data.api.CompareResponse
-if class com.schemewise.app.data.api.CompareResponse
-keep class com.schemewise.app.data.api.CompareResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.api.CompareResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.api.CompareResponse
-keepclassmembers class com.schemewise.app.data.api.CompareResponse {
    public synthetic <init>(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
