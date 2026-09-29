# MVI presentation base

`core/presentation` contains a typed base for migrated feature ViewModels:
read-only `StateFlow<State>`, one-off `Flow<Event>`, and `onAction(Action)`.
It extends the multiplatform lifecycle ViewModel but contains no mutable state,
event channel, custom scope, navigation, DI, global loading/error policy,
Android API or Compose UI. The legacy app BaseViewModel remains untouched.

- Owner: cross-feature presentation ViewModel contract only.
- Allowed dependencies: Kotlin, kotlinx.coroutines Flow and the multiplatform
  lifecycle ViewModel.
- Allowed consumers: feature `impl` modules and application composition when it
  intentionally needs the contract. Domain, data and API modules do not need it.
- Hidden implementation: each feature keeps its mutable state, event delivery
  and error handling private; consumers see typed read-only streams and an
  action entry point. The standard lifecycle ViewModel owns scope cancellation.
- Gradle boundary: independent feature modules can opt in without depending on
  `composeApp`, another feature implementation, or the visual design-system.
  This is a shared ABI, not a package inside the first feature.

The first subclass is favorites. Event-less screens can use `Nothing` as the
event type and inherit the empty event stream; do not invent event classes.
For atomic state changes, use the existing `kotlinx.coroutines.flow.update`
extension on the feature's private
`MutableStateFlow`; core does not duplicate it.
