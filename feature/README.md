# Feature migration: API / implementation

This directory now contains independent favorites api/domain/data/impl/di modules.
They are not connected to the old application. See favorites/README.md.
Actual registration remains in build-logic Modules.all. Android is the integration
target; no native iOS implementation is part of these slices.

## Target layout and graph

For each migrated business capability (not each individual screen):

```text
feature/<name>/
  api/                   routes and minimal cross-feature capabilities
  domain/                models, repository contracts, meaningful business rules
  data/                  remote/local, DTOs/entities/DAOs, mappers, repository impl
  impl/
    src/commonMain/.../
      ui/                screens, ViewModels, state/events, feature components
  di/                    optional Koin assembly and composition bridges
```

Each boundary becomes a separate KMP Gradle module as its code is prepared.
Do not create empty placeholders or a second application. Api has no data/UI
implementation or Koin declarations. Domain uses pure Kotlin/approved domain
dependencies; it may depend on api only when those contracts are equally pure.
Data depends on domain and needed core infrastructure. Impl depends on api/domain,
design-system and data wiring factories; ui packages cannot access data internals.
Keep data implementations internal and expose only the narrow construction
surface required for feature composition. Enforce UI import boundaries as well
as Gradle edges, because impl also contains composition.

Other features use api for navigation/screen entry and may directly consume
intentionally supported pure domain contracts, as agreed for favorites. Never
depend on another feature's data/impl/di from business/UI. Shared domain ABI
must be deliberately supported, not moved to core/common.
Composition owns the root graph and assembles feature implementations.

HTTP requests belong to feature data/remote, not screens. Room persistence belongs
to data/local. Core provides transport/driver infrastructure without feature DTOs,
DAOs or endpoints. Preserve Kotlin Result and rethrow coroutine cancellation.
Not every feature needs HTTP, Room or a use case wrapping a single repository call.

## Modules DSL

Keep existing module.core.network / module.legacy.domain accessors compatible.
As actual feature modules are added, introduce a feature catalog following the
existing object-plus-instance-accessor pattern:

```kotlin
implementation(project(module.feature.favorites.api))
implementation(project(module.feature.favorites.domain))
implementation(project(module.feature.favorites.data))
implementation(project(module.feature.favorites.impl))
```

Favorites accessors are now callable, including module.feature.favorites.di.
Register only real module
paths in Modules.all. Domain/data are nested by business owner, not new global
technical-layer buckets. Core api/impl splits need their own concrete ABI/reuse
reason; a transport module does not need artificial business domain/data modules.

## Branch and PR stack

Every next slice starts from the current migration tip, not main. A PR targets
its immediate parent branch; keep its diff independently reviewable.
Favorites foundation is based on feature/core-design-system-adaptive-layout (#49);
favorites UI/DI is based on that foundation, not main.
After a parent merges to main, retarget its child to main before merging it.
If a parent changes, update descendants explicitly and re-run affected checks;
never rewrite published stack history or unrelated work without agreement.

## Ordered slices

1. Confirm these boundaries and finish shared UI readiness: refresh policy,
   brand font/resources and RTL-safe insets. Keep light/dark previews current.
2. Extend build-logic for actual feature paths; extract convention plugins only
   for repeated configuration observed while adding the first modules.
3. Choose one pilot capability. Prepare its api/domain and contract tests.
4. Prepare data implementations/adapters and mapper/persistence tests, with no
   application switching yet. Document current vs target models/schema.
5. Prepare impl/ui using core design-system, injected dependencies, caller
   navigation callbacks and PreviewLightDark fixtures.
6. Integrate only that capability through root DI/navigation in a dedicated PR.
   Build the Android app, smoke-test user flows and verify stored-data compatibility.
7. Delete legacy copies only after every consumer has switched. Repeat by owner;
   do not relocate all screens/models/repositories at once.

## Pilot audit: favorites (selected, prepared independently)

Current UI: composeApp ui/screen/favorites, FavoritesViewModel and FavoritesScreen.
Current contract: modules/domain FavoriteRepository and CoffeeShop/Details.
Current data: modules/data FavoriteRepositoryImpl and LocalFavoriteShopDto,
using modules/room DatabaseCore.settingRepository and local_favorite_shops JSON.
There is no favorites HTTP service to move.

Dependencies to resolve before integration:

- UI uses feed.ShopCard and app location helpers: do not depend on feed impl or
  put CoffeeShop business presentation in design-system just to bypass this.
- FavoriteSync is a global app event bus also observed by other consumers:
  decide a minimal favorites api observation contract and its ownership.
- Navigator and BaseViewModel couple presentation to app/global scope: inject
  navigation actions and use caller/lifecycle-owned coroutine work.
- Persistence must preserve existing serialized rows/key during transition.
- Session cleanup also consumes favorites: audit all repository consumers before
  deleting the legacy implementation.

Possible pilot public capabilities: favorite observation/change and route/entry
contracts. Final signatures depend on consumer audit, not speculative DTO export.
Domain/data/UI preparation can proceed independently; switching the app requires
its own testable integration slice.

## Completion gate per slice

Document owner, allowed consumers/dependencies, public surface, migration steps
and risks. Compile changed modules and app whenever integration changes; run
contract/UI tests, check cycles/imports and DTO/entity containment. Inspect both
preview themes, record modal/font/RTL limitations, and keep main unchanged until
the corresponding PR is merged.
