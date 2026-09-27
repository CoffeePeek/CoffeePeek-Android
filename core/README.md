# Core preparation roadmap

Prepare and verify independent infrastructure before integrating each boundary
into the application. Do not pre-create every example module in ARCHITECTURE.md.

## Prepared foundations

- `coroutines`: injectable IO dispatcher and caller-owned supervised scope.
- `network`: shared transport settings, engine-injected factory and Kotlin
  `Result` request boundary. Cancellation is rethrown, not turned into failure.
- `database`: Room builder configuration with bundled SQLite; no application
  schema, feature entities or migrations are moved into core.

The first network slice already delegates legacy transport settings to core.
The new client factory, Result boundary, coroutine and database foundations
are not wired into the application yet.

## Remaining preparation, in order

1. Network: inspect existing auth/refresh and caching contracts; isolate only
   reusable transport policies with injected token access. Preserve upload
   signed URLs and avoid credential logging. Test refresh concurrency, failures,
   cancellation and retry boundaries before replacing the legacy client.
2. Database: validate driver setup against a small test-only Room schema and
   Android instrumentation. Keep actual schema/migration ownership above feature
   persistence; decide composition when the first feature is extracted.
3. Design system: inventory existing themes, fonts, resources and shared UI
   primitives. Separate shared tokens from feature-specific styling. Prepare
   Android-compatible theme/components without switching screens or redesigning
   them. Document resource and platform dependencies before adding the module.
4. Navigation: add infrastructure only if the first feature's entry-point
   contract demonstrates a shared need. Root graph and Koin assembly stay in
   application composition; no generic core DI module is required.

## Integration gate

Compile each affected module and run its contract tests first. Then integrate
one infrastructure boundary at a time, build the Android application and verify
existing behaviour. Finally choose one business feature and migrate its domain,
data and UI boundaries without exposing DTOs or DAOs. Remove legacy code only
after all consumers move. Native iOS implementation is outside this stage.
