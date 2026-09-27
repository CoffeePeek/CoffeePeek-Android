# Design-system preparation

Responsibility: reusable visual tokens, themed primitives, icon facade and shared
visual modifiers. Allowed consumers: application composition and feature UI.
Dependencies: Compose runtime/UI/foundation/Material3, Haze and Phosphor icons.
No application, feature, domain, data, navigation, Koin, Room or Ktor dependencies.
Compose/Haze types are exported because public APIs expose them; the icon vendor
is hidden behind `CpIcons`. An independent Gradle boundary allows future features
to reuse UI without depending on the application or accidentally accessing its
business models. No umbrella utility or base-screen abstraction is introduced.

## First slice

`theme/`: shared palette/dimensions, light/dark Material theme and typography.
Auth-specific decoration and application header/bottom-navigation metrics remain
outside core. `CoffeePeekTheme` requires a FontFamily: existing Manrope resources
remain in composeApp, supplied at integration; there is no generated app-resource
dependency or silently substituted brand font in core.

`icons/`: existing Phosphor-backed `CpIcons` facade, including intentional aliases.
Feature mappings (brew methods, prices and ratings) stay with feature presentation.

`component/`: AppButton and generic grouped section/checkmark/action/switch/stepper
rows. The CatalogItem-based CheckmarkSection is deliberately excluded. Stepper
accessibility descriptions must be supplied by the caller, not hardcoded in core.
AppButton uses a minimum height instead of a fixed height so text can grow with
font scale. Brand styles are otherwise retained; screens are not redesigned.

`modifier/`: liquidGlass, its optional Haze composition local and GlassIconButton.
No backdrop source means the same translucent fallback as the legacy component.
Haze ownership belongs to the screen; no global renderer or platform abstraction
is created. Shared UI lives in commonMain with Android as the initial build target.
Native iOS integration is outside this stage.

## Migration and verification

Existing components/resources/screens remain unchanged and have no dependency on
this module. New independent copies are a temporary migration boundary, not a
second long-term source of truth. Integrate one consumer family at a time, supply
the brand font, check light/dark rendering, accessibility, RTL and font scaling,
then remove legacy implementations when unused. See AUDIT.md for remaining slices.

Unit tests cover tokens, font injection and icon aliases. Android instrumentation
tests render buttons, a grouped checkbox and the glass fallback, verifying labels,
enabled state and callbacks. These are behavioural contracts, not pixel-perfect
parity tests or verification of Haze backdrop rendering and every icon.

```shell
./gradlew :core:design-system:testDebugUnitTest :core:design-system:assembleDebug
./gradlew :core:design-system:connectedDebugAndroidTest
```

Future consumers use `implementation(project(module.core.designSystem))`.
