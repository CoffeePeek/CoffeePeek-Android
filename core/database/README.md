# Database infrastructure foundation

Owns the common SQLite driver configuration. Consumers will be application
database composition and feature persistence. Depends on Room and bundled
SQLite only. Room is exported because the public builder extension exposes it.
The driver implementation is hidden behind the configuration function.

This module is registered and compiled independently. The existing Room module
continues to own the application database, schema and migrations until the
integration stage. It does not depend on this module yet.

A module boundary prevents driver infrastructure from acquiring feature
entities/DAOs. The concrete Room database must reference its schema types;
when feature DAOs move, database composition must be arranged above those
features instead of introducing core-to-feature dependencies.

## Runtime contract tests

`androidInstrumentedTest` contains isolated V1/V2 Room schemas and DAOs, generated
by KSP only for the Android test compilation. They are not shipped in the library
and do not depend on application/feature schemas. No Room schema plugin or
production compiler dependency is needed for this infrastructure-only module.

Tests exercise the real bundled SQLite driver through Room:

- builder identity and in-memory read/write;
- persistent rows after close/reopen;
- caller-supplied V1-to-V2 migration and default column value;
- missing migration fails without destructive fallback or loss of V1 rows.

Persistent fixtures use unique `core-database-test-<UUID>.db` names in the test
APK sandbox and delete only those databases after closing them. The application
database and existing Room module remain untouched. Fixture schema export is
disabled: these are synthetic contracts, not the application's migration history.

Run with an attached Android device/emulator:

```shell
./gradlew :core:database:connectedDebugAndroidTest
```

To verify compilation without a device:

```shell
./gradlew :core:database:assembleDebug :core:database:assembleDebugAndroidTest
```

This does not validate production migrations. Their schema history and tests must
remain with application database composition when features migrate. No iOS
implementation or application integration is added by this stage.
