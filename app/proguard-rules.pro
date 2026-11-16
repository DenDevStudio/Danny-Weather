# Add project specific ProGuard rules here.

# Keep Room Database and DAO interfaces
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# Keep Moshi models and adapters
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep Retrofit and OkHttp
-keepattributes Exceptions
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**

# Keep Domain and Data Models
-keep class com.example.data.model.** { *; }
-keep class com.example.data.api.** { *; }
-keep class com.example.data.local.** { *; }

# Keep App Widget
-keep class com.example.widget.** { *; }
