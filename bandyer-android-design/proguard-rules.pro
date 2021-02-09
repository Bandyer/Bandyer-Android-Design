# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep ic_bandyer_options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# Save the obfuscation mapping to a file, so we can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-dontshrink
-dontoptimize

-keepattributes Exceptions,InnerClasses,Signature, Deprecated, SourceFile, LineNumberTable,EnclosingMethod

# Picasso
-dontwarn com.squareup.picasso.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

-keep class com.bandyer.core_av.utils.logging.** { *; }
-keep class com.bandyer.core_av.utils.proximity_listener.ProximitySensor** { *; }

-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.bandyer.core_av.networking.models.** { *; }


# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------End: proguard configuration for Gson  ----------

-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep google missing class required at runtime by webrtc
-keep class com.google.devtools.build.android.desugar.runtime.** { *; }

