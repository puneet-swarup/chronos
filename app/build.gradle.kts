import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.plugin.compose")
 id("org.jetbrains.kotlin.plugin.serialization")
 id("com.google.devtools.ksp")
}

// CI passes the real values; a local build stays at the defaults below so a
// developer never has to think about version codes.
val buildVersionCode = (project.findProperty("versionCode") as String?)?.toIntOrNull() ?: 1
val buildVersionName = (project.findProperty("versionName") as String?) ?: "0.1.0"

android {
 namespace = "com.chronos.app"
 compileSdk = 36

 defaultConfig {
 applicationId = "com.chronos.app"
 minSdk = 28
 targetSdk = 36
 versionCode = buildVersionCode
 versionName = buildVersionName
 testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 vectorDrawables { useSupportLibrary = true }
 }

 // A dedicated test key, committed on purpose so CI can produce builds that
 // install over each other without any manual secret setup. It is public: see
 // keystore/README.md for what that means and when to replace it.
 signingConfigs {
 create("testRelease") {
 storeFile = rootProject.file("keystore/test-release.p12")
 storePassword = "chronos-test"
 keyAlias = "chronos"
 keyPassword = "chronos-test"
 storeType = "PKCS12"
 }
 }

 buildTypes {
 release {
 isMinifyEnabled = false
 signingConfig = signingConfigs.getByName("testRelease")
 proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
 }
 debug {
 applicationIdSuffix = ".debug"
 versionNameSuffix = "-debug"
 }
 }

 compileOptions {
 sourceCompatibility = JavaVersion.VERSION_17
 targetCompatibility = JavaVersion.VERSION_17
 }

 buildFeatures {
 compose = true
 buildConfig = true
 }

 testOptions {
 unitTests.isReturnDefaultValues = true
 unitTests.isIncludeAndroidResources = true
 }
}

kotlin {
 compilerOptions {
 jvmTarget.set(JvmTarget.JVM_17)
 }
}

ksp {
 arg("room.schemaLocation", "build/schemas")
}

dependencies {
 implementation(project(":chronos-core"))

 implementation("androidx.core:core-ktx:1.13.1")
 implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
 // LocalLifecycleOwner for the first-run guide, which re-reads permission state
 // when the screen resumes.
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.activity:activity-compose:1.9.3")

 val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
 implementation(composeBom)
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 // Scroll modifiers and layout primitives; material3 brings this in
 // transitively, but the first-run guide uses foundation directly.
 implementation("androidx.compose.foundation:foundation")
 implementation("androidx.compose.material3:material3")
 // The curated icon set: enough for the navigation bar without pulling in the
 // multi-megabyte extended library.
 implementation("androidx.compose.material:material-icons-core")
 implementation("androidx.navigation:navigation-compose:2.8.4")

 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

 implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

 implementation("androidx.room:room-runtime:2.7.1")
 implementation("androidx.room:room-ktx:2.7.1")
 ksp("androidx.room:room-compiler:2.7.1")

 testImplementation("junit:junit:4.13.2")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.test:core:1.6.1")
 testImplementation("androidx.room:room-testing:2.7.1")
}
