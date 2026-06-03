-if class com.schemewise.app.data.model.User
-keepnames class com.schemewise.app.data.model.User
-if class com.schemewise.app.data.model.User
-keep class com.schemewise.app.data.model.UserJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.User
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.User
-keepclassmembers class com.schemewise.app.data.model.User {
    public synthetic <init>(int,java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
