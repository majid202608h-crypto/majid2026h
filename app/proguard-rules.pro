# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Poolakey (Cafe Bazaar In-App Billing)
-keep class ir.cafebazaar.poolakey.** { *; }
-keep interface ir.cafebazaar.poolakey.** { *; }
-dontwarn ir.cafebazaar.poolakey.**

# Keep AIDL interfaces for billing
-keep class com.android.vending.billing.** { *; }

# Room Database Entities
-keep @androidx.room.Entity class * { *; }
-keep class com.example.data.UserProfileEntity { *; }
-keep class com.example.data.StageStarsEntity { *; }
-keep class com.example.data.TableStatEntity { *; }

# Room DAOs and Databases
-keep interface * extends androidx.room.Dao { *; }
-keep class * extends androidx.room.RoomDatabase { *; }

# Suppress warnings for unused dependencies
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn com.squareup.moshi.**
-dontwarn kotlinx.serialization.**
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**

# Keep Jetpack Compose runtime/compiler attributes if needed
-keepattributes Signature,AnnotationDefault,EnclosingMethod,InnerClasses,SourceFile,LineNumberTable
