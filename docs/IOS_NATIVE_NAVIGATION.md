# Native iOS tabs

## Scope and ownership

The application composition module owns the root navigation and the platform
implementation of `MainScreen`. The iOS implementation is a UIKit
`UITabBarController` embedded through `UIKitViewController`. Each of its four
children hosts an existing Compose screen. Android delegates to the existing
`ComposeMainScreen` and floating Compose navigation bar.

This is the first native UI migration step. Other glass components, detail
transitions, sheets, and buttons remain Compose UI. No Gradle modules,
business contracts, repositories, or dependencies are added.

## Integration

- `Navigator` continues to own detail/authentication routes. Its Main route
  selects the platform implementation, so both Xcode and xtool launchers use
  the same implementation without a new Swift bridge or public framework API.
- UIKit retains each tab's controller while switching tabs, including the map.
- `Navigator.pendingTabSelection` selects native tabs for actions such as
  showing a shop on the map; pending map focus remains owned by `Navigator`.
- `AppContent` supplies each Compose controller with the shared theme and
  image configuration. It does not create another root navigator or DI container.
- Native tab selection is saved in the parent Compose navigation entry. UIKit
  appearance follows the application's system/light/dark preference.
- Tab content receives bottom clearance derived from actual UIKit geometry.
- Touches are forwarded immediately to UIKit, and native accessibility is enabled.

## Appearance and limits

The tab bar uses system appearance without a custom blur or glass overlay.
With an iOS 26+ SDK and iOS 26+ device, UIKit supplies Liquid Glass. Earlier
systems retain their native tab appearance. See Apple's
[UIKit design guidance](https://developer.apple.com/videos/play/wwdc2025/284/).

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
