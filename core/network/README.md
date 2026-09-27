# Network infrastructure

Owns shared API JSON configuration, base URL setup and upload timeouts.
Also provides an engine-independent client factory and `requestResult`, which
returns Kotlin `Result` and rethrows coroutine cancellation. The caller owns
client/engine lifecycle. These additions are prepared for later integration.
New factory clients validate HTTP status (`expectSuccess = true`), so 4xx/5xx
responses become failures when wrapped in `requestResult`. Callers may override
validation explicitly. Legacy client response handling is not changed.
Consumers: legacy `modules:network` now, feature data modules during migration.
Dependencies: Ktor, Kotlin serialization and coroutines; no feature or legacy modules.
Public surface: `HttpClientFactory`, `requestResult`, `configureApiTransport`,
`configureUploadTransport`, and `networkJson` for transport contract serialization.
Ktor core is exported because the public extensions expose `HttpClientConfig`.
Serialization options are defined once in `networkJson`.

The Gradle boundary prevents infrastructure from depending on business DTOs or
services. Engines, caching and auth integration remain in the legacy client in
this first slice. Existing iOS target variants are declared for consumer
compatibility; this module contains no iOS-specific implementation.
