# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

 # Retrofit/Gson need generic type (Signature) metadata at runtime to know
 # what to deserialize suspend-function return types into. Without this,
 # R8 full mode strips it and Gson silently returns raw LinkedTreeMap
 # instances instead of your DTOs -> ClassCastException in release only.
 -keepattributes Signature
 -keepattributes Exceptions
 -keepattributes InnerClasses,EnclosingMethod
 -keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
 -keepattributes AnnotationDefault

 # Gson-specific: keep type adapters/factories and the streaming classes it
 # reaches via reflection.
 -keep class com.google.gson.stream.** { *; }
 -keep class * implements com.google.gson.TypeAdapterFactory
 -keep class * implements com.google.gson.JsonSerializer
 -keep class * implements com.google.gson.JsonDeserializer
 -keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
 -keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

 # Keep your network DTOs intact (fields + no-arg access) so Gson's field-name
 # matching survives obfuscation.
 -keep class com.biblelib.core.network.dtos.** { *; }
 -keepclassmembers class com.biblelib.core.network.dtos.** { *; }

 # With R8 full mode generic signatures are stripped for classes that are not
 # kept. Suspend functions are wrapped in continuations where the type argument
 # is used.
 -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

 # R8 full mode strips generic signatures from return types if not kept.
 -if interface * { @retrofit2.http.* public *** *(...); }
 -keep,allowoptimization,allowshrinking,allowobfuscation class <3>

 # With R8 full mode generic signatures are stripped for classes that are not kept.
 -keep,allowobfuscation,allowshrinking class retrofit2.Response

 # Some JVM-portable libraries (e.g. OkHttp's platform detection) reference
 # java.lang.management classes in code paths that only run on a desktop JVM
 # and never execute on Android, where these classes don't exist. Safe to ignore.
 -dontwarn java.lang.management.ManagementFactory
 -dontwarn java.lang.management.RuntimeMXBean