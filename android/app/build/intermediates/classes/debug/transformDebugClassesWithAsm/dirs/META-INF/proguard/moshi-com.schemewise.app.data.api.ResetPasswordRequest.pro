-if class com.schemewise.app.data.api.ResetPasswordRequest
-keepnames class com.schemewise.app.data.api.ResetPasswordRequest
-if class com.schemewise.app.data.api.ResetPasswordRequest
-keep class com.schemewise.app.data.api.ResetPasswordRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.api.ResetPasswordRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.api.ResetPasswordRequest
-keepclassmembers class com.schemewise.app.data.api.ResetPasswordRequest {
    public synthetic <init>(java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
