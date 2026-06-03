-if class com.schemewise.app.data.model.Scheme
-keepnames class com.schemewise.app.data.model.Scheme
-if class com.schemewise.app.data.model.Scheme
-keep class com.schemewise.app.data.model.SchemeJsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
}
-if class com.schemewise.app.data.model.Scheme
-keepnames class kotlin.jvm.internal.DefaultConstructorMarker
-if class com.schemewise.app.data.model.Scheme
-keepclassmembers class com.schemewise.app.data.model.Scheme {
    public synthetic <init>(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Object,java.lang.Object,java.lang.String,java.lang.String,java.lang.Double,java.lang.Double,java.lang.Double,java.lang.Double,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,kotlin.jvm.internal.DefaultConstructorMarker);
}
