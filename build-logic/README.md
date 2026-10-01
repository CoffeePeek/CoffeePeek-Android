# Build infrastructure

This is an included Gradle build, not an application/KMP runtime module.
It owns the module catalog and settings registration. It depends only on Gradle
build APIs; application source sets must never depend on it. Existing Config and
task helpers remain in buildSrc, which exposes this artifact to project scripts.
This build can host convention plugins when shared build policies are extracted.

`Modules.kt` contains actual paths and `Modules.all` registers them through
`com.coffeepeek.modules`. Add a new path and include it in `all`; the settings
plugin rejects duplicates and missing module build files. Paths and dependencies
remain unchanged during this migration.

Project scripts import `com.coffeepeek.buildlogic.module` and declare dependencies:

```kotlin
implementation(project(module.core.network))
implementation(project(module.legacy.domain))
```

Core references are available but not automatically added to the application.
Gradle's generated `projects` accessors also remain available. Do not create a
second manually maintained module catalog or a runtime umbrella core module.

An included build supplies settings plugins before buildSrc is available and
isolates build-only code from runtime modules; a runtime Kotlin package cannot
serve this responsibility. The buildSrc bridge shares the same compiled catalog
instead of copying constants. No new dependency versions or platform targets are
introduced by the catalog.
