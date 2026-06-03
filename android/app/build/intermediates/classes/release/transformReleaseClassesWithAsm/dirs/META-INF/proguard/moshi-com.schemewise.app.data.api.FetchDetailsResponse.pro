-if class com.schemewise.app.data.api.FetchDetailsResponse
-keepnames class com.schemewise.app.data.api.FetchDetailsResponse
-if class com.schemewise.app.data.api.FetchDetailsResponse
-keep class com.schemewise.app.data.api.FetchDetailsResponseJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.api.FetchDetailsResponse
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.api.FetchDetailsResponse
-keepclassmembers class com.schemewise.app.data.api.FetchDetailsResponse {
    public synthetic <init>(java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
