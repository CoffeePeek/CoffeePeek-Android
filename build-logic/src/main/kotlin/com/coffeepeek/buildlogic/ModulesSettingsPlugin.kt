package com.coffeepeek.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class ModulesSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        check(Modules.all.distinct().size == Modules.all.size) { "Duplicate module paths" }
        Modules.all.forEach { path ->
            check(path.startsWith(":")) { "Module path must be absolute: $path" }
            check(settings.settingsDir.resolve(path.removePrefix(":").replace(':', '/'))
                .resolve("build.gradle.kts").isFile) { "Missing build file for $path" }
        }
        settings.include(*Modules.all.toTypedArray())
    }
}
