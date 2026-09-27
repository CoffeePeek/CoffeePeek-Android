import com.coffeepeek.buildlogic.module
import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION)) } }

    sourceSets {
        commonMain.dependencies {
            api(compose.runtime)
            api(libs.androidx.navigation3.runtime)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.serialization.json)
        }

    }
}

android {
    namespace = "com.coffeepeek.feature.favorites.api"
    // Navigation 3 1.2 requires API 37; legacy app remains on its existing SDK.
    compileSdk = 37
    defaultConfig {
        minSdk = Config.MIN_SDK

    }
    compileOptions {
        sourceCompatibility = Config.JAVA_VERSION
        targetCompatibility = Config.JAVA_VERSION
    }
}
