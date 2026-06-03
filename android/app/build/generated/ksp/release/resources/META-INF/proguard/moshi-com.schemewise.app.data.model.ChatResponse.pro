-if class com.schemewise.app.data.model.ChatResponse
-keepnames class com.schemewise.app.data.model.ChatResponse
-if class com.schemewise.app.data.model.ChatResponse
-keep class com.schemewise.app.data.model.ChatResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.ChatResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.ChatResponse
-keepclassmembers class com.schemewise.app.data.model.ChatResponse {
    public synthetic <init>(java.lang.String,java.util.List,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
