# Favorites: independent contracts and storage

This foundation is stacked on feature/core-design-system-adaptive-layout (#49).
No old screen, DI, navigation, repository, JSON key or database schema is changed.
Screen implementation and the Koin/Room composition bridge follow in a child PR.

## Module boundaries and creation gate

| Module | Responsibility / public surface | Dependencies | Consumers / isolation reason |
|---|---|---|---|
| api | Serializable FavoritesRoute: NavKey and Composable FavoritesEntry contract | Navigation 3 runtime, Compose runtime, serialization | Navigation/application composition; minimal ABI without screen/VM |
| domain | FavoriteShop saved snapshot, FavoritesRepository Result/Flow, ObserveFavoriteIdsUseCase | Kotlin + coroutines | Own UI/data and deliberately supported cross-feature consumers; independent business ABI/tests |
| data | Internal JSON DTO/mapping/repository; FavoritesStorage construction port and repository factory | domain + serialization/coroutines | Manual composition only; prevents UI importing persistence implementation |

Domain/data use manual constructor/factory injection, no Koin, Android, Room or
legacy dependencies. Data requires an injected CoroutineDispatcher; parsing,
observation and persistence run there. Production composition will supply the
core IO dispatcher. Storage is a raw keyed-string port, not a feature DTO export.

## Compatibility and behaviour

Legacy FavoritesViewModel/Screen, FavoriteRepositoryImpl and LocalFavoriteShopDto
remain unchanged. This feature has no favorites HTTP endpoint; no artificial
remote service is introduced. Future feature HTTP code belongs in data.

The key remains local_favorite_shops. Internal StoredFavorite field names/defaults
match legacy JSON, including singular roasterPhotoUrl fallback and plural logos.
Duplicate persisted IDs keep their first (newest) snapshot. Save replaces by ID
and moves to the front; last removal deletes the key; clear deletes only this key.
Corrupt JSON returns Result.failure, never silent empty data or a read-driven
overwrite. Explicit clear can delete corrupt data. Unknown fields are ignored;
preservation of unknown future fields on writes is not promised.

One repository instance serializes read-modify-write operations. Storage
observation covers initial/own/external changes. Independent instances or legacy
writers are NOT protected by this mutex. Before integration unify all writers or
provide transactional storage; JSON compatibility alone does not prevent races.
Ordinary failures use Kotlin Result; CancellationException is always rethrown.
ObserveFavoriteIdsUseCase projects distinct membership sets, preserving failures
and suppressing metadata-only updates for catalog/detail/session consumers.
No meaningless one-call load/remove use cases are added.

## Navigation and KMP

Navigation 3 1.2.0 runtime owns the typed route contract only. Its AAR requires
compileSdk 37, applied to api only here; root app SDK/target/min stay unchanged.
Root will need compileSdk 37 before integration. API implements no NavDisplay,
root graph, shop route or screen. Root owns navigation and lifecycle decorators.

Domain/data declare Android and iOS variants; simulator compilation verifies
portable Kotlin, not native UI or a Swift framework. Future SwiftUI integration
needs a framework/bridge adapting suspend/Flow/Result and native storage,
lifecycle and navigation. No Swift code or speculative expect/actual is added.

## Verification and next slices

13 unit tests cover JSON compatibility/all fields, duplicates/order/delete,
corruption safety, failures/cancellation, concurrent writes, initial/external
observation, membership projection and route serialization. Android foundation
modules, legacy app and iOS-simulator domain/data compile.

Next child prepares impl/ui and optional feature-owned di with manual entry
construction, isolated Koin bridge, PreviewLightDark and Android navigation tests.
Application integration remains separate: map catalog models, unify writers and
session cleanup, replace global events, wire distance/shop routes, verify real
stored rows/DB and full UI/Back/process restoration before deleting legacy code.

Reference: [Navigation 3 releases](https://developer.android.com/jetpack/androidx/releases/navigation3).
