# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.coffeepeek.api.model.**$$serializer { *; }
-keepclassmembers class com.coffeepeek.api.model.** {
    *** Companion;
}
-keep class com.coffeepeek.api.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Yandex MapKit
-keep class com.yandex.** { *; }
-dontwarn com.yandex.**

# Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Compose / Coil / Kamel
-dontwarn coil.**
-dontwarn io.kamel.**

# Google Play Services Auth
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
