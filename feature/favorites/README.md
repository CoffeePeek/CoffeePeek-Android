# Favorites: independent preparation

No old screen, repository, DI binding, navigation graph, JSON key or database
 schema is replaced. New modules are registered through module.feature.favorites.*
and callable independently. Branch is stacked on core-design-system-adaptive-layout
(#49). Foundation and UI/DI are separate stacked PRs; application switching/removal is a separate future slice.

## Boundaries and module gate

| Module | Owns / public surface | Dependencies | Consumers / why a module |
|---|---|---|---|
| api | Serializable FavoritesRoute: NavKey and minimal Composable FavoritesEntry interface | Navigation 3 runtime, Compose runtime, serialization | Root/feature navigation; small screen-construction ABI without VM/data |
| domain | FavoriteShop snapshot, FavoritesRepository Result/Flow contracts, ObserveFavoriteIdsUseCase | Kotlin + coroutines only | Own UI/data, explicitly supported cross-feature membership consumers; independent business ABI/tests |
| data | Internal StoredFavorite/mapping/repository; public storage construction port and factory | domain + serialization | di/manual composition only; UI cannot import DTOs or repository implementation |
| impl | Internal ViewModel/state/screen/cards; entry factory and Navigation 3 registration | api/domain, design-system, lifecycle, Kamel | di/root composition; public API consumers do not acquire screen or VM implementation |
| di | favoritesModule and favoritesRoomModule; internal settings bridge; temporary legacy repository adapter factory | api/domain/data, impl factory, Koin, temporary legacy Room and domain contracts | Application composition only; isolates legacy/Koin from both data and UI |

```text
root (later) → di → data → domain ← impl/ui
                  └────→ impl entry factory → api
               bridge → existing SettingRepository
               adapter → existing FavoriteRepository contract
root Navigation 3 entryProvider → favoritesEntry → FavoritesEntry.Content
```

Domain/data are manually constructed; no Koin annotations, contexts or service
lookups. Data requires an injected dispatcher for storage and parsing; production
DI supplies the core IO dispatcher. The only Koin graph is explicitly loaded by composition, not started as
a global singleton. The feature DI module is justified by its concrete legacy
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
comes from storage, covering external writes without FavoriteSync. Sharing one
Koin instance is mandatory; independent repository instances/old writers are not
protected by its mutex. Before integration, switch all writes to one owner or
provide transactional storage. Do not run parallel old/new writers and assume
that shared JSON format prevents races.

For the transition, createLegacyFavoritesRepositoryBridge(newRepository) adapts
the existing repository contract for feed/detail/session consumers. It maps
CoffeeShop snapshots to FavoriteShop and saved snapshots back to the existing
CoffeeShopDetails shape without putting legacy models in domain/data. Both the
adapter and new screen MUST receive the same new repository singleton. The
factory does not register or replace the old Koin binding: application composition
must explicitly replace it later, rather than load two independent writers.
The old getFavoriteIds/isFavorite/clearAll methods return no Result, so failures
propagate as exceptions; getFavorites/add/remove retain Result. Corrupt data
cannot silently appear empty. Cancellation is never converted to a Result.
The current FavoriteSync app event bus is not replaced by this adapter; feed and
details observation must migrate to the new membership flow (or a temporary
app-owned notification bridge) when the screen is switched.

The membership use case projects/deduplicates ID sets for catalog/detail/session
consumers without exposing saved-card presentation or metadata-only updates.
No one-call load/remove use-case wrappers are introduced. Cancellation is rethrown;
ordinary persistence failures are Result failures. ViewModel scopes are lifecycle
owned. Removal waits for confirmed storage observation; failure keeps the card
and shows a safe localized error, never raw storage exception text.

## Navigation / UI / platform boundaries

Navigation 3 1.2.0 is used only by the new feature. Its AAR requires compileSdk 37,
so api/impl/di compile against 37; the old app and its target/min SDK are unchanged.
Root must update its compile SDK before consuming these modules. Root owns
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
The real database migration history is not tested by this feature fixture.
FavoritesPreviews.kt supplies PreviewLightDark for content/loading/empty/error
without DI, network or actual photo URLs. Compile checks are not manual IDE QA.

Before switching: bind the adapter from the SAME new repository singleton for
all existing consumers, including ShopRepositoryImpl and UserSessionCleaner;
replace the old Koin favorite binding rather than adding a second writer.
Then replace global event consumers, wire distance/shop navigation,
validate real saved rows/DB and transitions/Back/process restoration/IME/screens.
Then integrate through root in a dedicated PR, build/smoke-test Android, and remove
legacy code only after every consumer migrates. Native iOS UI remains later.

Reference: [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3).
