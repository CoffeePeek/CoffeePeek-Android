import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION))
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":modules:domain"))
            implementation(project(":modules:network"))
            implementation(project(":modules:room"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
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
