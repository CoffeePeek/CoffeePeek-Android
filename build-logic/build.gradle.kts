plugins {
    `kotlin-dsl`
}

group = "com.coffeepeek"
version = "1.0"

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("coffeePeekModules") {
            id = "com.coffeepeek.modules"
            implementationClass = "com.coffeepeek.buildlogic.ModulesSettingsPlugin"
        }
    }
}
