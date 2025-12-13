package com.coffeepeek.config

import org.gradle.api.JavaVersion

object Config {

    val APP_VERSION get() = "1.0.$COMMIT_COUNT"
    val COMMIT_COUNT = GitFunctions.getCommitCount()
    const val JVM_VERSION = "17"
    val JAVA_VERSION = JavaVersion.VERSION_17

    const val APPLICATION_ID = "com.coffeepeek"
    const val MAIN_CLASS = "com.coffeepeek.admin.MainKt"

    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36

}
