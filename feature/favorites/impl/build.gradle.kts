import com.coffeepeek.buildlogic.module
import com.coffeepeek.config.Config
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.fromTarget(Config.JVM_VERSION)) } }

    sourceSets {
        commonMain.dependencies {
            api(project(module.feature.favorites.api))
            implementation(project(module.feature.favorites.domain))
            implementation(project(module.core.designSystem))
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kamel)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation("androidx.compose.ui:ui-tooling-preview:${libs.versions.androidx.composeUi.get()}")
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
        }
    }
}
dependencies { add("debugImplementation", compose.uiTooling) }

android {
    namespace = "com.coffeepeek.feature.favorites.impl"
    compileSdk = 37
    defaultConfig {
        minSdk = Config.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = Config.JAVA_VERSION
        targetCompatibility = Config.JAVA_VERSION
    }
}
