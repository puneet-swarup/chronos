# ProGuard / R8 rules for the Chronos app module.
#
# Minification is disabled for the current release build type
# (see app/build.gradle.kts), but the file is referenced there, so it must exist.
# Enable it only once the reflection surfaces are mapped: Room entities and DAOs,
# kotlinx.serialization models, and any workflow JSON adapter.

# Keep Room generated implementations
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep @androidx.room.Entity class * { *; }

# Keep line numbers for readable crash reports, hide the original file names
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
