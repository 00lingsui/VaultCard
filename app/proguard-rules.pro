# Room Database, DAOs & Entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**
-keep class com.example.cardmanager.core.database.entity.** { *; }

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt & Dependency Injection
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep interface * extends dagger.hilt.internal.GeneratedComponent { *; }

# Jetpack Compose runtime & animations
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Biometric & Crypto
-dontwarn androidx.biometric.**
-keep class androidx.biometric.** { *; }

# Strip verbose/debug logs in release builds to reduce APK size
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
