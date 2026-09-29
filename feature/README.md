# Feature migration: API / implementation

This directory now contains independent favorites api/domain/data/impl/di modules.
Android composition now uses favorites di plus a compatibility adapter; feed
and detail observe favorites domain membership, and the Android favorites route
renders the new screen. The shared root still uses Navigation 2, while iOS keeps
the old favorites screen and binding. See favorites/README.md.
Actual registration remains in build-logic Modules.all. Android is the integration
target; no native iOS implementation is part of these slices.

## Target layout and graph

For each migrated business capability (not each individual screen), normally:

```text
feature/<name>/
  api/                   routes and minimal cross-feature capabilities
  domain/                models, repository contracts, meaningful business rules
  data/                  remote/local, DTOs/entities/DAOs, mappers, repository impl
  impl/
    src/commonMain/.../
      ui/                screens, ViewModels, state/events, feature components
```

Each boundary becomes a separate KMP Gradle module as its code is prepared.
Do not create empty placeholders or a second application. Api has no data/UI
implementation or Koin declarations. Domain uses pure Kotlin/approved domain
dependencies; it may depend on api only when those contracts are equally pure.
Data depends on domain and needed core infrastructure. Impl depends on api/domain
and design-system; its UI cannot access data internals. The existing favorites/di
module is a temporary exception for its legacy bridge. New features should be
assembled by composeApp Koin/platform packages unless another Gradle boundary
has a demonstrated need.
Keep data implementations internal and expose only the narrow construction
surface required for feature composition. Enforce UI import boundaries as well
as Gradle edges.

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
implementation(project(module.feature.favorites.di)) // existing migration bridge only
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
favorites UI/DI, compatibility bridge and Android DI integration follow in order.
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
   navigation callbacks and colocated paired light/dark previews.
6. Integrate only that capability through root DI/navigation in a dedicated PR.
   Build the Android app, smoke-test user flows and verify stored-data compatibility.
7. Delete legacy copies only after every consumer has switched. Repeat by owner;
   do not relocate all screens/models/repositories at once.

## Next slices after Android favorites integration

Keep each item a separate stacked PR with its own build/tests. Do not turn this
list into empty modules or remove the iOS legacy path before its replacement works.

1. Give favorites UI its own text/accessibility resources. Audit actual locale
   directories first; do not invent a second translation to complete a diagram.
2. Move the temporary favorites Koin/Room/legacy bridge into Android application
   composition, preserving one repository instance/writer and its persistence tests.
   Remove favorites/di only when all app references have moved.
3. Audit the existing BaseViewModel against lifecycle, cancellation, Result and
   error presentation. Extract only a proven reusable pattern; do not require
   migrated ViewModels to inherit a generic base.
4. Move existing design-system preview-only fixtures beside their components in
   scoped groups, checking paired themes and preserving commonMain UI ownership.
5. Decide the iOS presentation boundary for favorites explicitly: shared Compose
   UI in the current SwiftUI host or a native SwiftUI screen over shared domain/data.
   Then prepare required native targets, platform storage/DI and tests before
   switching the iOS route and retiring its legacy writer/events.
6. Migrate root navigation separately after feature entry contracts work on both
   platforms. Do not mix a Navigation 3 root swap with persistence or UI parity.
7. Repeat the feature migration by business owner; remove legacy modules only
   after all consumers on Android and iOS have moved.

## Pilot audit: favorites (Android screen and DI integrated)

Legacy UI remains in composeApp ui/screen/favorites for iOS; Android now enters
the new feature screen through the shared root's Favorites destination.
Current contract: modules/domain FavoriteRepository and CoffeeShop/Details.
Current data: modules/data FavoriteRepositoryImpl and LocalFavoriteShopDto,
using modules/room DatabaseCore.settingRepository and local_favorite_shops JSON.
There is no favorites HTTP service to move.

Remaining integration concerns:

- The legacy iOS screen uses feed.ShopCard and app location helpers. Do not
  depend on feed impl or put CoffeeShop business presentation in design-system
  just to bypass this on Android.
- FavoriteSync remains for the legacy iOS screen; Android feed/detail consume
  the feature domain membership flow. Retire the event path only when every
  remaining consumer has switched.
- The Android feature screen uses injected navigation callbacks and a
  lifecycle-owned ViewModel; the shared root Navigator remains transitional.
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
