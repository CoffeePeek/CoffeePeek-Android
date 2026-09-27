rootProject.name = "buildSrc"

// Share the same catalog artifact with project scripts; settings use its plugin.
includeBuild("../build-logic")
