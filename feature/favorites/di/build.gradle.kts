import com.coffeepeek.buildlogic.module
import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)

}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION)) } }

    sourceSets {
        commonMain.dependencies {
            api(project(module.feature.favorites.domain))
            api(project(module.feature.favorites.api))
            api(project(module.feature.favorites.data))
            api(project(module.legacy.domain))
            api(project(module.legacy.room))
            api(libs.koin.core)
            implementation(project(module.feature.favorites.impl))
            implementation(project(module.core.coroutines))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }

    }
}

android {
    namespace = "com.coffeepeek.feature.favorites.di"
    compileSdk = 37
    defaultConfig {
        minSdk = Config.MIN_SDK

    }
    compileOptions {
        sourceCompatibility = Config.JAVA_VERSION
        targetCompatibility = Config.JAVA_VERSION
    }
}
