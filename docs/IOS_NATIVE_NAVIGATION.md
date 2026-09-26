# iOS navigation bar

## Scope and ownership

The application composition module owns the root navigation. Both iOS and
Android use the shared Compose `FloatingBottomNavBar`, including its backdrop
blur, translucent tint, rim highlight, selected pill, and tab transitions.

This is the first native UI migration step. Other glass components, detail
transitions, sheets, and buttons remain Compose UI. No Gradle modules,
business contracts, repositories, or dependencies are added.

## Integration

- `Navigator` continues to own detail/authentication routes. Its Main route
  uses the same Compose implementation on both platforms.
- Each tab remains inside the Compose navigation graph, including the map.
- `Navigator.pendingTabSelection` selects native tabs for actions such as
  showing a shop on the map; pending map focus remains owned by `Navigator`.
- `AppContent` supplies each Compose controller with the shared theme and
  image configuration. It does not create another root navigator or DI container.
- Tab selection is saved in the Compose navigation entry.
- Tab content receives the existing Compose bottom clearance.

## Appearance and limits

The bar uses `LiquidGlass.kt`: Haze backdrop blur when the current screen can
be sampled, a translucent fallback over the native map, a gradient rim, and a
soft shadow. This is the same visual contract on iOS and Android. It is a
Compose recreation of the Liquid Glass language rather than a system
`UITabBarController`, so it keeps CoffeePeek's floating shape and selected
state from the Android design.

Automatic scroll-driven tab minimization is not implemented: Compose scroll
containers do not expose a native UIScrollView to UIKit. This change does not
claim to provide every scroll-edge effect or native detail navigation gesture.

## Verification and deployment

Local checks passed:

```bash
./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileDebugKotlinAndroid
```

A new macOS CI framework from this source revision is required before device
verification. The previously downloaded framework contains the old navigation.
After downloading the new artifact, prepare resources from the same revision
and install using the steps in `iosXtool/CoffeePeek/README.md`.

Device checks still required:

1. Switch all four tabs; check native glass, selection animation, and touch response.
2. Return to feed/map and confirm their state survives tab switches.
3. Open a shop, go back, and use "show on map"; confirm the selected tab and focus.
4. Open authentication, sign in/out, and verify the root navigation resets safely.
5. Change theme, rotate, and check content clearance, VoiceOver, and keyboard behavior.
