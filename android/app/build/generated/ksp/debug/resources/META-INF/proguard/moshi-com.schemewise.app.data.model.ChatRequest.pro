-if class com.schemewise.app.data.model.ChatRequest
-keepnames class com.schemewise.app.data.model.ChatRequest
-if class com.schemewise.app.data.model.ChatRequest
-keep class com.schemewise.app.data.model.ChatRequestJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.ChatRequest
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.ChatRequest
-keepclassmembers class com.schemewise.app.data.model.ChatRequest {
    public synthetic <init>(java.lang.String,java.util.List,com.schemewise.app.data.model.Profile,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
