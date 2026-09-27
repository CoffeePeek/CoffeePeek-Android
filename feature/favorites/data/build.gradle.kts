import com.coffeepeek.buildlogic.module
import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION)) } }
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(project(module.feature.favorites.domain))
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

    }
}

android {
    namespace = "com.coffeepeek.feature.favorites.data"
    compileSdk = Config.COMPILE_SDK
    defaultConfig {
        minSdk = Config.MIN_SDK
    }
    compileOptions {
        sourceCompatibility = Config.JAVA_VERSION
        targetCompatibility = Config.JAVA_VERSION
    }
}
