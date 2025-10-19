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

# --- Custom Rules ---

# Keep all data model classes
-keep class com.example.owoo.data.** { *; }

# Keep all network service classes
-keep class com.example.owoo.network.** { *; }

# Rules for GSON
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*


# AGGRESSIVE RULES FOR GOOGLE LIBRARIES
# This is necessary because the 'invalid_grant' error only happens in release mode,
# indicating an obfuscation issue.
-dontwarn javax.naming.**
-dontwarn org.ietf.jgss.**
-keep class com.google.api.** { *; }
-keep class com.google.auth.** { *; }
-dontwarn com.google.api.**
-dontwarn com.google.auth.**

# Keep some fundamental Java classes that might be accessed via reflection
-keep class jrockit.vm.MemSystem { *; }
-keep class java.util.logging.** { *; }

# General rule for libraries that use reflection
-keepattributes Signature,InnerClasses


# --- Custom Rules ---

# Keep all data model classes
-keep class com.example.owoo.data.** { *; }

# Keep all network service classes
-keep class com.example.owoo.network.** { *; }

# Rules for GSON
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepattributes *Annotation*

# Rules for Google API Client & Auth
-keepclassmembers class * extends com.google.api.client.util.GenericData {
    public <init>();
}
-keep class com.google.api.services.sheets.** { *; }
-dontwarn com.google.api.client.googleapis.util.Utils
-keep class com.google.api.client.json.GenericJson {
    <fields>;
    <methods>;
}

# General rule for libraries that use reflection
-keepattributes Signature,InnerClasses


# Keep all data model classes in the data package
-keep class com.example.owoo.data.** { *; }
-keep class com.google.gson.annotations.** { *; }

# Keep all network service classes
-keep class com.example.owoo.network.** { *; }

# Keep attributes for GSON
-keepattributes Signature
-keepattributes *Annotation*