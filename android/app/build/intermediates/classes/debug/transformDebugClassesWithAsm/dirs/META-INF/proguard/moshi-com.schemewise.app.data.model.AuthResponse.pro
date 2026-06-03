-if class com.schemewise.app.data.model.AuthResponse
-keepnames class com.schemewise.app.data.model.AuthResponse
-if class com.schemewise.app.data.model.AuthResponse
-keep class com.schemewise.app.data.model.AuthResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.AuthResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.AuthResponse
-keepclassmembers class com.schemewise.app.data.model.AuthResponse {
    public synthetic <init>(java.lang.String,com.schemewise.app.data.model.User,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
