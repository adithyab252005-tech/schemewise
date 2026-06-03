-if class com.schemewise.app.data.model.ChatMessage
-keepnames class com.schemewise.app.data.model.ChatMessage
-if class com.schemewise.app.data.model.ChatMessage
-keep class com.schemewise.app.data.model.ChatMessageJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
