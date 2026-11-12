import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
 id("com.android.application")
 id("org.jetbrains.kotlin.plugin.compose")
 id("com.google.devtools.ksp")
}

android {
 namespace = "com.chronos.app"
 compileSdk = 36

 defaultConfig {
 applicationId = "com.chronos.app"
 minSdk = 28
 targetSdk = 36
 versionCode = 1
 versionName = "0.1.0"
 testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
 vectorDrawables { useSupportLibrary = true }
 }

 buildTypes {
 release {
 isMinifyEnabled = false
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
 implementation("androidx.activity:activity-compose:1.9.3")

 val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
 implementation(composeBom)
 implementation("androidx.compose.ui:ui")
 implementation("androidx.compose.ui:ui-tooling-preview")
 implementation("androidx.compose.material3:material3")
 implementation("androidx.navigation:navigation-compose:2.8.4")

 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

 implementation("androidx.room:room-runtime:2.7.1")
 implementation("androidx.room:room-ktx:2.7.1")
 ksp("androidx.room:room-compiler:2.7.1")


 testImplementation("junit:junit:4.13.2")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
 testImplementation("org.robolectric:robolectric:4.14.1")
 testImplementation("androidx.test:core:1.6.1")
 testImplementation("androidx.room:room-testing:2.6.1")
}
