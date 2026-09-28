# Favorites: incremental Android integration

Android application composition now binds the new repository and a legacy-contract
adapter as one writer. The old screen, navigation, repository implementation file,
JSON key and database schema remain; the old repository binding is omitted on
Android only. iOS keeps its existing binding and runtime behaviour. These changes
are stacked on #49 → favorites foundation → UI/DI → compatibility bridge.

## Boundaries and module gate

| Module | Owns / public surface | Dependencies | Consumers / why a module |
|---|---|---|---|
| api | Serializable FavoritesRoute: NavKey and minimal Composable FavoritesEntry interface | Navigation 3 runtime, Compose runtime, serialization | Root/feature navigation; small screen-construction ABI without VM/data |
| domain | FavoriteShop snapshot, FavoritesRepository Result/Flow contracts, ObserveFavoriteIdsUseCase | Kotlin + coroutines only | Own UI/data, explicitly supported cross-feature membership consumers; independent business ABI/tests |
| data | Internal StoredFavorite/mapping/repository; public storage construction port and factory | domain + serialization | di/manual composition only; UI cannot import DTOs or repository implementation |
| impl | Internal ViewModel/state/screen/cards; entry factory and Navigation 3 registration | api/domain, design-system, lifecycle, Kamel | di/root composition; public API consumers do not acquire screen or VM implementation |
| di | favoritesModule and favoritesRoomModule; internal settings bridge; temporary legacy repository adapter factory | api/domain/data, impl factory, Koin, temporary legacy Room and domain contracts | Application composition only; isolates legacy/Koin from both data and UI |

```text
Android root → di → data → domain ← impl/ui
                  └────→ impl entry factory → api
               bridge → existing SettingRepository
               adapter → existing FavoriteRepository contract
root Navigation 3 entryProvider → favoritesEntry → FavoritesEntry.Content (later)
```

Domain/data are manually constructed; no Koin annotations, contexts or service
lookups. Data requires an injected dispatcher for storage and parsing; production
DI supplies the core IO dispatcher. Feature modules declare Koin bindings but
only application composition starts Koin. The feature DI module is justified by its concrete legacy
storage bridge, not a generic core DI aggregator. Factories hide implementations.
Feature UI depends on neither data nor di; Gradle enforces this restriction.
Other business/UI features may consume favorites domain deliberately; api remains
the route/entry boundary. No generic BaseViewModel or Navigator singleton.

## Current vs target and behaviour

Legacy FavoritesViewModel/Screen, FavoriteRepositoryImpl and LocalFavoriteShopDto
stay in their existing folders. New UI uses a favorites-owned saved snapshot,
not the large CoffeeShopDetails aggregate or feed.ShopCard implementation.
The original feature has no favorites HTTP service; no artificial remote layer
or endpoint is introduced. Future HTTP DTOs/services belong in this data module.

The storage key remains local_favorite_shops. Internal JSON field names/defaults
match the existing saved format, including single roasterPhotoUrl fallback and
plural roasterPhotoUrls. Duplicate saved IDs retain their first (newest) snapshot.
A save replaces by ID and moves it to the front. Removing
the last row deletes the key, and clear deletes only this key. Corrupt data is an
explicit Result failure, not silently interpreted as empty and overwritten.
Clear is an explicit destructive user/session operation and can delete corrupt
data; it is never triggered by a read failure. Unknown fields are ignored like
the legacy serializer; their preservation on future writes is not promised.

One repository instance serializes its read-modify-write operations. Observation
comes from storage, covering external writes without FavoriteSync. Android now
shares one Koin instance across the new contract and legacy adapter. Independent
repository instances or direct old writers are not protected by its mutex; do not
reintroduce a parallel writer and assume shared JSON prevents races. iOS retains
its one legacy writer until its own migration.

For the transition, createLegacyFavoritesRepositoryBridge(newRepository) adapts
the existing repository contract for feed/detail/session consumers. It maps
CoffeeShop snapshots to FavoriteShop and saved snapshots back to the existing
CoffeeShopDetails shape without putting legacy models in domain/data. Both the
adapter and new screen MUST receive the same new repository singleton. The
factory itself does not register a Koin binding. Android composition now opts out
of dataModule's legacy binding and loads favoritesRoomModule plus
legacyFavoritesConsumersModule, which resolves the same new repository singleton.
iOS keeps dataModule's default legacy binding. Do not load both writers.
The old getFavoriteIds/isFavorite methods return no Result and previously treated
malformed rows as empty; the temporary adapter returns an empty membership set
on ordinary read failures so old feed/detail callers do not crash. Its new
repository and getFavorites still report Result.failure; add/remove fail without
overwriting corrupt rows. clearAll propagates failure. Cancellation is never
converted to a Result or an empty set. This legacy fallback must be removed when
those consumers migrate to Result/Flow observation with explicit error UI.
The adapter itself does not emit FavoriteSync events. Android feed and shop
details now observe ObserveFavoriteIdsUseCase from feature domain; failures retain
the last displayed membership, and newly loaded rows are reconciled against the
latest known ID set. On iOS the optional observer is absent and feed keeps its
old FavoriteSync subscription. Existing feed/details still notify FavoriteSync
after their own writes for the old favorites screen, which remains active. That
old screen is the last FavoriteSync consumer to remove at the UI switch.

The membership use case projects/deduplicates ID sets for catalog/detail/session
consumers without exposing saved-card presentation or metadata-only updates.
No one-call load/remove use-case wrappers are introduced. Cancellation is rethrown;
ordinary persistence failures are Result failures. ViewModel scopes are lifecycle
owned. Removal waits for confirmed storage observation; failure keeps the card
and shows a safe localized error, never raw storage exception text.

## Navigation / UI / platform boundaries

Navigation 3 1.2.0 is used only by the new feature. Its AAR requires compileSdk 37,
so api/impl/di and the Android app compile against 37; target/min SDK remain
unchanged. Root owns
NavDisplay, back stack, entry saveable/ViewModel-store decorators and shop routes;
favorites only registers FavoritesRoute and emits onOpenShop(id)/onBack.
The API does not publish a shop route for another business owner.

The new screen covers loading/empty/error/list, retry, separate remove action and
pending-removal disablement. It uses brand primitives and feature-owned cards.
Distance is a host-injected label by ID: no location permissions/global platform
service is copied. Kamel renders URLs in UI; it is not a favorites HTTP service.
The prepared card is not a pixel-identical feed card: mascot/provider art,
roaster logo overlays, price-bean/rating presentation and distance permissions
remain consumer-integration decisions. All saved fields remain in data/domain.
Default Russian strings match the current feature language; localization/resource
ownership, complete visual parity and long-label/RTL/large-font UI QA remain gates.

Domain/data declare and compile iOS simulator variants with no Android APIs,
Koin or native iOS implementation. Android api/impl/di are the tested UI/composition
boundary at this stage. For native SwiftUI, share domain/data through a future
framework/bridge, adapt suspend/Flow/Result at that boundary and supply native
storage/lifecycle/navigation; do not expose Compose VM/Android NavDisplay to Swift.
Compose iOS UI is a separate choice requiring native variants of its dependencies
(including design-system), testing and platform integration. No speculative
expect/actual declarations, Swift code or framework export are added now.

## Tests, previews, remaining integration

Contract tests cover legacy JSON/default/logo compatibility, all saved fields,
ordering/deduplication, last removal, corruption, concurrent writes, observation,
Result/cancellation, membership projection, VM lifecycle/pending/failure/retry,
route serialization, and an isolated Koin/setting bridge graph.
Legacy-adapter tests cover shared reads/writes, full snapshot/location mapping,
old singular-logo rows, corruption, failure and cancellation propagation.
Android fixtures exercise screen states/callbacks and a real NavDisplay/entryProvider
with saveable and VM-store decorators. They use fake data and no network/real DB.
App-level tests exercise feed/detail membership changes both before and after
their initial shop responses, including reconciliation of late-loaded rows.
The real database migration history is not tested by this feature fixture.
FavoritesPreviews.kt supplies PreviewLightDark for content/loading/empty/error
without DI, network or actual photo URLs. Compile checks are not manual IDE QA.

Android DI now supplies the adapter from the same singleton to existing
consumers, including ShopRepositoryImpl and UserSessionCleaner. Feed/details now
observe membership directly; before replacing the old screen, migrate that
screen's remaining FavoriteSync subscription, wire distance/shop navigation,
validate real saved rows/DB and transitions/Back/process restoration/IME/screens.
Keep UI/nav integration in a dedicated PR, build/smoke-test Android, and remove
legacy code only after every consumer migrates. Native iOS UI remains later.

Reference: [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3).
