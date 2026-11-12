import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
 kotlin("jvm")
}

kotlin {
 compilerOptions {
 jvmTarget.set(JvmTarget.JVM_17)
 }
}

java {
 sourceCompatibility = JavaVersion.VERSION_17
 targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
 testImplementation(kotlin("test"))
 testImplementation("junit:junit:4.13.2")
 testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

tasks.test {
 useJUnit()
 testLogging {
 events("passed", "skipped", "failed")
 }
}
