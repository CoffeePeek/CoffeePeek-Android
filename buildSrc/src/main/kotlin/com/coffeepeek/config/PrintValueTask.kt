package com.coffeepeek.config

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class PrintValueTask : DefaultTask() {
    @get:Input
    abstract val value: Property<String>

    @TaskAction
    fun printValue() {
        logger.quiet(value.get())
    }
}
