# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep stack trace information for crashes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ================================
# Bluetooth & BLE Classes
# ================================
-keep class android.bluetooth.** { *; }
-keep class com.kyutae.applicationtest.bluetooth.** { *; }
-keepclassmembers class com.kyutae.applicationtest.bluetooth.** { *; }

# ================================
# Data Classes & Models
# ================================
-keep class com.kyutae.applicationtest.dataclass.** { *; }
-keepclassmembers class com.kyutae.applicationtest.dataclass.** { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keep class com.kyutae.applicationtest.database.** { *; }
-keepclassmembers class com.kyutae.applicationtest.database.** { *; }

# ================================
# Gson & JSON Serialization
# ================================
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Klaxon JSON
-keep class com.beust.klaxon.** { *; }

# ================================
# WorkManager
# ================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.InputMerger
-keep class androidx.work.** { *; }
-keepclassmembers class * extends androidx.work.Worker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class com.kyutae.applicationtest.utils.AutoReconnectManager$** { *; }

# ================================
# ViewModel & LiveData
# ================================
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
-keepclassmembers class ** {
    @androidx.lifecycle.OnLifecycleEvent *;
}

# ================================
# MPAndroidChart
# ================================
-keep class com.github.mikephil.charting.** { *; }

# ================================
# Google Play Services (Ads)
# ================================
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ================================
# Material Design Components
# ================================
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# ================================
# Kotlin Coroutines
# ================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ================================
# Application Class
# ================================
-keep class com.kyutae.applicationtest.BluesCanApplication { *; }

# ================================
# Fragments & Activities
# ================================
-keep class com.kyutae.applicationtest.MainActivity { *; }
-keep class com.kyutae.applicationtest.MainFragment { *; }
-keep class com.kyutae.applicationtest.SettingFragment { *; }

# ================================
# Adapters
# ================================
-keep class com.kyutae.applicationtest.adapters.** { *; }

# ================================
# Utils
# ================================
-keep class com.kyutae.applicationtest.utils.** { *; }

# ================================
# Remove Logging in Release
# ================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep only ERROR and WARNING logs
-assumenosideeffects class android.util.Log {
    public static *** e(...);
    public static *** w(...);
}

# ================================
# General Android
# ================================
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**