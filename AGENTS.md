# CoffeePeek — Agent Rules

These rules apply to every change in this repository. `ARCHITECTURE.md` is the
detailed source of truth; this file is the operating checklist.

## Current state and migration

The current Gradle layout is transitional:

```text
composeApp/                 current Android/KMP application composition module
modules/domain/             legacy shared models and repository contracts
modules/network/            legacy HTTP infrastructure and feature API code
modules/data/               legacy repository implementations
modules/room/               legacy Room infrastructure and persistence
core/                      prepared independent infrastructure/design-system
feature/                   independent favorites api/domain/data/impl/di preparation
iosApp/                     native iOS application boundary, when present
```

Do not treat the complete target `app/`, `core/`, `feature/` layout as already
implemented. Before a structural change, inspect `settings.gradle.kts`, relevant
module build files, source sets, DI, navigation, and existing abstractions.

Migrate incrementally. Do not rename, split, or replace all modules in one
operation; do not create a second application module merely because a target
diagram names one. Keep legacy code temporarily when needed, but do not add new
code for an already migrated feature back to legacy modules.

## Ownership and boundaries

Organize new code by business feature, not by a global technical layer.

```text
feature/<name>/
├── api/       intentionally public contracts only
├── domain/    business rules, domain models, repository interfaces
├── data/      remote/local implementations, DTOs, entities, DAOs, mappers
├── impl/      screen entry point and ui/ presentation implementation
└── di/        optional Koin assembly and composition bridges
```

The agreed target for migrated business features is separate api/domain/data/impl
Gradle boundaries under singular feature/. Create these incrementally for the
feature being migrated, not empty modules for every future screen. UI packages
live inside impl; composition assembles data via narrow factories. A separate
feature di module keeps Koin/legacy bridges outside UI when needed.
This does not require every core infrastructure module to have a domain/data
pair. See feature/README.md for the migration sequence and dependency graph.

- `api` is public by necessity, not by default. Keep routes, entry points, and
  minimal cross-feature contracts there.
- `domain` must not depend on Android, Compose, SwiftUI, Ktor, Room, SQL, HTTP,
  Koin, or navigation.
- `data` implements domain contracts and contains feature-specific Ktor APIs,
  DTOs, Room entities/DAOs, data sources, and mappers. These must not escape
  data.
- `ui` renders state, handles user events, and depends on domain abstractions —
  never on Ktor, DAOs, DTOs, entities, or repository implementations.
- `core` owns genuinely shared infrastructure only. It never depends on a
  feature. Never introduce `core/common`, `core/base`, `core/utils`, or another
  dumping ground.
- The application composition module owns application startup, root DI, root
  navigation, and platform configuration; it does not own feature business logic.

Features use another feature's `api` for navigation/screen contracts. Intentionally
supported pure `domain` contracts may also be consumed directly, as agreed for
favorites; never depend on another feature's data/impl/di from business or UI code.
Domain/data use constructor/manual DI. An optional feature-owned `di` module may
assemble Koin and temporary composition bridges; only application composition
consumes it, and no DI wiring belongs in domain/data or generic core DI.
The dependency graph must remain acyclic. DI and navigation must not bypass these
boundaries.

## KMP and platforms

Prefer `commonMain` for platform-independent domain, data, shared presentation
logic, ViewModels, UI state, and Compose Multiplatform UI where applicable.

- `androidMain`: Android APIs, Android-only integrations, and Android-only UI.
- `iosMain`: Kotlin/Native implementations of actual platform abstractions.
- `iosApp`: native Swift/SwiftUI application, lifecycle, native navigation, and
  integration with public KMP APIs.

Do not put Android APIs, SwiftUI, or platform-specific business logic in
`commonMain`. Do not use `expect`/`actual` merely to compensate for a poor
module boundary.

## Before coding

- Continue the branch stack from the current migration tip, not main. Each new
  PR targets its immediate parent branch and describes only its own changes.
  Work only in migration branches. Before merging a child, retarget it to main
  after its parent has reached main; recheck its diff and tests. Do not merge
  children into an already merged parent and assume they reached main.
- Branch isolation does not prove application correctness: every integration
  slice must compile the Android app and test the affected behaviour.

1. Identify the business feature and the owning layer.
2. Search for a reusable existing implementation before creating a parallel one.
3. Inspect relevant Gradle dependencies and source sets.
4. For a new Gradle module, document: responsibility, allowed dependencies,
   allowed consumers, hidden implementation, and why packages are insufficient.
5. For a major migration, document current location, target location, public API,
   migration steps, dependencies, and risks.

Create a use case only for meaningful business behaviour, coordination, or rules;
do not wrap one repository call without value. Prefer composition over generic
`Base*`, `Manager`, `Helper`, or `Utils` abstractions. Prefer `internal` for
implementation details.

## After coding

- Compile affected modules and run relevant tests.
- Check Gradle dependencies, imports, source sets, and dependency cycles.
- Verify DTO/entity containment; infrastructure independence of domain; and that
  UI does not access data implementations.
- Keep changes scoped: architecture work must not silently include unrelated UI,
  dependency, navigation, or product changes.
- Never commit credentials, API keys, passwords, or tokens.
- Add/update PreviewLightDark fixtures for each reusable UI family and migrated
  screen. Keep Android preview tooling in androidMain, with fake/injected state
  and no production DI/network. Record modal/IDE rendering limitations.
- Preserve Kotlin Result request contracts and rethrow CancellationException;
  do not use suspend runCatching where it would swallow cancellation.

## Decision rule

When a change seems to require a convenient forbidden dependency, stop and ask:

1. Which feature owns this behaviour?
2. Which layer owns the responsibility?
3. Must it be public outside the feature?
4. What is the smallest change that preserves the boundary?

Prioritize correctness, dependency boundaries, business ownership, testability,
platform independence, and minimal public API over convenience.
