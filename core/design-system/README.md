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

## Fields and top bars slice

CompactOutlinedTextField exposes the basic decoration contract. AppTextField adds
the brand label/error style, accessible field label/error, enabled/read-only state
and caller-configured keyboard actions. Ordinary fields default to a text keyboard,
not email. Password visibility is caller-owned; callers supply the localized toggle
description matching that state and handle onPasswordVisibilityChange.

CpSearchField keeps query state outside the component, emits search/clear actions
and requires a localized clear description. Disabled/read-only search cannot clear
the query. Fields use minimum rather than fixed heights; search has a 48.dp clear
target. CpTopBar has no default back action or Navigator dependency. Its caller
supplies the back callback/description; the back glyph mirrors in RTL.

These APIs prepare existing visual families, not search/auth business rules.
Focus handling on the search IME action remains local UI behaviour.

## Migration and verification

Presentation slice: IconBadge/its palette are neutral visual primitives, reused
by settings and contribution UI. SettingsSection/Row/Divider exclude AppVersionFooter:
version lookup and localization stay in composition. Section titles are rendered
as supplied (callers decide uppercase); row callbacks and enabled state are explicit.
Directional row chevrons mirror in RTL. CapsuleSegmentedControl exposes tab
selection, accepts enabled state and requires nonempty unique options with a valid
selection. Its minimum-height targets can grow with typography.

CoffeePeekLoader retains the existing animation and exposes localized indeterminate
progress semantics; it does not start work. ErrorDialog requires all user-facing
strings and emits dismiss. LoadingDialog visibility is caller-owned and uses a
non-dismissible modal instead of the legacy touch-consuming full-screen overlay.
This is an intentional new contract, not a drop-in visual/interaction replacement.
At integration check back handling, modal sizing/focus and cancellation UX explicitly.
There are no timers, global loading/error state or Navigator dependencies.

Existing components/resources/screens remain unchanged and have no dependency on
this module. New independent copies are a temporary migration boundary, not a
second long-term source of truth. Integrate one consumer family at a time, supply
the brand font, check light/dark rendering, accessibility, RTL and font scaling,
then remove legacy implementations when unused. See AUDIT.md for remaining slices.

Unit tests cover tokens, font injection and icon aliases. Android instrumentation
tests render buttons, a grouped checkbox and the glass fallback, verifying labels,
enabled state and callbacks. These are behavioural contracts, not pixel-perfect
parity tests or verification of Haze backdrop rendering and every icon.
Input tests additionally cover hoisted editing, error semantics, disabled fields,
password action labels/state, search IME/clear/read-only behaviour and explicit
top-bar back actions. Large-font/RTL screen layouts require integration QA.

Presentation tests cover enabled/selected actions, settings callbacks, progress
semantics, error dismissal and modal back/visibility behaviour. They do not
prove pixel parity, all animation frames or outside-touch geometry.

```shell
./gradlew :core:design-system:testDebugUnitTest :core:design-system:assembleDebug
./gradlew :core:design-system:connectedDebugAndroidTest
```

Future consumers use `implementation(project(module.core.designSystem))`.
