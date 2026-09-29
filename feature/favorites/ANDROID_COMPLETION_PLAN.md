# Favorites: Android completion plan

This plan finishes and verifies the Android favorites migration without changing
the iOS screen, binding, writer, or shared legacy code still needed by iOS. It
continues from the current migration tip. Related steps can share one checkpoint
PR; the working split point is about 50 changed files in its diff. Each step
must be verified before moving on even when it does not get its own PR.

## Progress

- [x] Synchronize local `main`, all seven open checkpoint branches, and the
  current work branch from `origin/main` without changing PR diffs or merging
  anything into `main`.
- [x] Verify the synchronized Android baseline with
  `./gradlew :composeApp:assembleDebug :composeApp:testDebugUnitTest
  :feature:favorites:data:testDebugUnitTest
  :feature:favorites:impl:testDebugUnitTest --no-daemon` (passed).
- [ ] Put Android favorites completion before the already prepared shop-report
  PR in the merge order, without rewriting published PR history.
- [ ] Complete the Android consumer/writer audit in step 1.
- [ ] Prove historical Room persistence compatibility in step 2.
- [ ] Verify the complete Android user journey in step 3.
- [ ] Finish UI, accessibility and locale QA in step 4.
- [ ] Perform Android-only cleanup and the final gate in step 5.

## Baseline and boundaries

- The Android root still uses Navigation 2. Its Favorites destination already
  displays the new feature screen through `FavoritesEntry`; the Navigation 3
  feature entry is prepared but is not the application root.
- One Android `FavoritesRepository` singleton backs the new screen and the
  temporary old-contract adapter. Its stored JSON key and Room settings table
  must remain compatible with existing installations.
- The old iOS screen and writer continue to use the shared legacy contracts.
  Do not delete `FavoriteSync`, legacy models/repository code, or shared routes
  merely because Android no longer renders the old favorites screen.
- No new favorites HTTP API, generic DI module, second storage writer, or new
  application module is required.

## 0. Put the work in the intended PR order

The current `feature/favorites-android-completion` branch descends from
`feature/shop-report-domain-data` (#68). If left as-is, favorites completion
cannot be merged before the next feature. Create a new favorites work branch
from `feature/design-system-colocated-previews` (#67) and copy only the
documentation changes from the current branch. Keep #68 open but pause further
shop-report work. When favorites is ready, put #68 after the favorites checkpoint
by merging the completed favorites branch into #68 and retargeting its PR;
verify both PR diffs and tests before any merge. Do not force-push or delete the
existing work branch until all content is accounted for.

Exit: favorites can be reviewed and merged before shop-report; #68 still has
only its intended shop-report diff against its updated base.

## 1. Establish a reproducible Android baseline

1. Check the current Android app build and the affected feature, bridge, and
   app tests. Record the exact commands and results in the checkpoint PR.
2. Audit every Android consumer of favorite reads, writes, membership and
   session cleanup: the new screen, feed, shop detail, `ShopRepositoryImpl`,
   `UserSessionCleaner`, explicit logout and forced logout. Verify that Android
   DI resolves a single new repository plus its old-contract adapter, that the
   old Android writer binding is disabled, and that no direct writes bypass the
   adapter. Record which shared consumers remain for iOS before cleanup.
3. Capture the current navigation and data behaviour with existing tests
   before changing it; avoid a root navigation rewrite in this step.
4. Define the failure policy for session cleanup. Today the cleaner clears the
   session before clearing favorites; test what happens if storage clear fails
   during explicit or forced logout, then choose a safe Android behaviour
   without silently losing the error or altering iOS semantics accidentally.

Exit: a documented consumer/writer map, a green baseline or explicit failures,
no unaccounted second Android writer, and an explicit logout failure policy.

## 2. Prove persisted-data compatibility

1. Add Android instrumentation coverage for real, historically accurate Room
   database fixtures at versions 1 and 2, migrated to the current version 3.
   Verify that `local_favorite_shops` survives, the new repository can read it,
   and the legacy adapter sees the same rows. Version 1's exported schema is
   present under `modules/room/schemas`; version 2's exported schema is in
   repository history at `e440150`. Use those actual schemas rather than
   fabricating version fixtures.
2. Exercise save, remove and session clear after migration, including reopening
   the database. Include old singular-logo JSON, unknown fields, duplicate IDs,
   ordering and removal of the last item. Keep tests on uniquely named
   disposable databases, never a developer or user database.
3. Retain coverage for malformed JSON and cancellation: read failures must not
   silently overwrite saved rows, ordinary failures stay in `Result`, and
   cancellation must not become `Result`. Test that clear is only an explicit
   session/user operation, not a response to a failed read.

Exit: both historical migration paths use their exported schemas and pass,
and the shared-writer behaviour remains verified after database reopen.

## 3. Verify the complete Android journey

1. Test a signed-in account with existing favorites: feed and shop-detail heart
   state, opening the favorites list, opening a shop, removing a card, returning
   to feed/detail, and clearing favorites on both explicit and forced logout.
2. Cover loading, empty, read error, write error, retry, repeated remove taps,
   quick heart taps in feed/detail, Back, process recreation and return to the
   screen. Reconcile favorite membership after late or refreshed shop responses.
   Automate stable behaviour at the appropriate feature/app layer and record a
   device smoke-test checklist for what cannot be reliably automated.
3. Verify distance with permission already granted and without permission;
   opening favorites must not request a new location permission.
4. Use test accounts/fixtures for destructive scenarios; do not clear a real
   user's saved rows to exercise logout or migration behaviour.

Exit: Android user journeys pass without a stale heart, duplicate write, lost
saved row, crash, or navigation regression. Record the device/API used.

## 4. Finish Android UI and resource quality

1. Inspect light/dark previews and the running screen with real photos/logos,
   missing images, long titles, large system font and RTL layout. Check touch
   targets, accessibility labels and screen-reader order. In particular, the
   saved-card remove control is currently drawn at 36dp and needs a measured
   accessible touch target. Fix only issues owned by favorites; shared component
   defects belong to the design system.
2. Audit actual supported app locales. The feature currently has Russian
   strings only; add another translation only when that locale is confirmed as
   supported, and keep all feature strings/accessibility text in resources.
3. Compare the saved card with the legacy Android behaviour using only fields
   persisted in favorites. Document unavoidable differences such as unavailable
   `isNew`/shop type instead of inventing missing data.

Exit: visual/accessibility findings are fixed or explicitly accepted, previews
remain colocated and paired light/dark, and locale coverage is truthful.

## 5. Android-only cleanup and final gate

1. Remove only obsolete Android-specific adapters or branches whose consumers
   are proven migrated. Keep shared legacy code used by iOS; document every
   retained bridge and its removal condition. Do not force a platform split
   solely to delete a small transitional call.
2. Re-run affected unit/instrumented tests and `:composeApp:assembleDebug` after
   integration changes; also verify a release build. Check dependencies, source
   sets, `Result`/cancellation, DTO containment and the absence of feature data
   imports in UI. If shared `commonMain` code changes, compile the existing iOS
   target as a regression guard without implementing new iOS behaviour.
3. Review the checkpoint diff against its actual parent branch, list remaining
   QA limitations, and mark draft PRs #49, #53 and #57 ready only when their own
   gates are met. Confirm local tests, device smoke tests and required CI checks
   before the next feature proceeds.

Exit: Android favorites is verified and merge-ready as a feature. This does
**not** mean that iOS, shared legacy deletion or the application-wide root
Navigation 3 migration is complete.

## Deferred, separate decisions

- iOS favorites presentation/storage integration and deletion of legacy paths
  still used by iOS.
- A root Navigation 3 migration. The current Android favorites entry works
  under Navigation 2; changing the shared root affects more than favorites and
  must have its own platform-aware plan and regression suite.
