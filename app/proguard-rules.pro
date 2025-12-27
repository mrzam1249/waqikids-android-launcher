# Add project specific ProGuard rules here.

# Keep Hilt
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.* <methods>;
    @dagger.hilt.* <fields>;
}

# Keep Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep data classes
-keep class com.waqikids.launcher.data.** { *; }
-keep class com.waqikids.launcher.domain.model.** { *; }

# Keep Compose
-dontwarn androidx.compose.**
