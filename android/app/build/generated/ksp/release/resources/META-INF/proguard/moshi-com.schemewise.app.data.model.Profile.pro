-if class com.schemewise.app.data.model.Profile
-keepnames class com.schemewise.app.data.model.Profile
-if class com.schemewise.app.data.model.Profile
-keep class com.schemewise.app.data.model.ProfileJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.Profile
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.Profile
-keepclassmembers class com.schemewise.app.data.model.Profile {
    public synthetic <init>(java.lang.Integer,java.lang.Integer,java.lang.String,java.lang.Double,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Double,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
