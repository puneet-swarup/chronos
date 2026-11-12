// Root build file. Declares plugins for all modules.
// Note: Hilt's Gradle plugin (2.52) is not yet compatible with AGP 9.2.0
// ("Android BaseExtension not found"), so Chronos uses manual dependency
// injection via a ServiceLocator instead. Room works fine with KSP.

plugins {
 kotlin("jvm") version "2.0.21" apply false
 id("com.android.application") version "9.2.0" apply false
 id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
 id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}

group = "com.chronos"
version = "0.1.0"
