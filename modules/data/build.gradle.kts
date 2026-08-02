import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION))
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation("androidx.security:security-crypto:1.0.0")
        }
        commonMain.dependencies {
            implementation(project(":modules:domain"))
            implementation(project(":modules:network"))
            implementation(project(":modules:room"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }
}

android {
    namespace = "${Config.APPLICATION_ID}.data"
    compileSdk = Config.COMPILE_SDK
    compileOptions {
        sourceCompatibility = Config.JAVA_VERSION
        targetCompatibility = Config.JAVA_VERSION
    }
}
