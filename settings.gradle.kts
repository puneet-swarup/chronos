pluginManagement {
 repositories {
 google()
 mavenCentral()
 gradlePluginPortal()
 }
}

dependencyResolutionManagement {
 repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
 repositories {
 google()
 mavenCentral()
 }
}

rootProject.name = "chronos"

// Pure Kotlin/JVM domain module: scheduling math, recurrence, workflow model,
// action contract and deterministic validator. No Android dependency.
include(":chronos-core")

// Android application module: Compose UI, Room, AlarmManager/WorkManager
// adapters and platform receivers.
include(":app")
