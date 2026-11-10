# Room Database, DAOs & Entities
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.**
-keep class com.example.cardmanager.core.database.entity.** { *; }

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt & Dependency Injection
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep interface * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Jetpack Compose runtime & UI
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Biometric & Crypto
-dontwarn androidx.biometric.**
-keep class androidx.biometric.** { *; }

# Suppress external library warnings in R8 full mode
-dontwarn androidx.**
-dontwarn dagger.**
-dontwarn com.google.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.**
-ignorewarnings
