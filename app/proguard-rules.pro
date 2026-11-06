# Room Proguard rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt Proguard rules
-keep class * extends androidx.lifecycle.ViewModel { *; }
