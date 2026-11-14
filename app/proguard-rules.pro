# Room Database, DAOs, Entities & TypeConverters
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.example.cardmanager.core.database.AppDatabase* { *; }
-keep class com.example.cardmanager.core.database.CardConverters { *; }
-keep class com.example.cardmanager.core.database.entity.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.**

# Backup, Serialization & Enums
-keep class com.example.cardmanager.core.backup.** { *; }
-keep class com.example.cardmanager.core.currency.AppCurrency { *; }
-keep class com.example.cardmanager.core.language.AppLanguage { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# WorkManager Workers
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Hilt, Dagger & ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }
-keep interface * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep class javax.inject.** { *; }

# Biometric & Crypto
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

# Suppress harmless missing optional class/annotation warnings under R8
-dontwarn androidx.**
-dontwarn dagger.**
-dontwarn com.google.**
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.**
-dontwarn kotlin.**
-dontwarn kotlinx.**
