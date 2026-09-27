# Coroutine infrastructure

Owns injectable dispatchers and creation of supervised scopes. Application
composition and feature data/presentation may consume it. Its only runtime
dependency is coroutines, exported because the public contracts expose coroutine
types. There are no feature dependencies or DI bindings here.

A separate Gradle boundary makes dispatcher substitution available without
depending on the application module. Callers must cancel scopes they create;
this module does not create a global application scope automatically.

Android is the initial build target. Integration and additional dispatcher
contracts should follow concrete consumers, not speculative abstractions.
