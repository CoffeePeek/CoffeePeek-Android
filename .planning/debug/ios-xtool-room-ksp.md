---
status: blocked
trigger: "Довести запуск iOS-версии CoffeePeek на Linux через xtool до установки и запуска на физическом iPhone; текущий blocker — отсутствующий actual CoffeePeekDatabaseConstructor при linkDebugFrameworkIosArm64."
created: 2026-09-26
updated: 2026-09-26T13:44:00+03:00
---

# Symptoms

- expected: `./gradlew :composeApp:linkDebugFrameworkIosArm64` собирает ComposeApp framework, SwiftPM launcher связывает его, а `xtool dev` устанавливает и запускает CoffeePeek UI на физическом iPhone.
- actual: iOS Kotlin compilation fails in `:modules:room:compileKotlinIosArm64` because `CoffeePeekDatabaseConstructor` has no Native actual declaration.
- error: `Expected CoffeePeekDatabaseConstructor has no actual declaration in module <commonMain> for Native`.
- timeline: Android works; minimal xtool SwiftPM iOS app already builds, signs, installs, and launches; real Compose framework has not yet built successfully.
- reproduction: run `./gradlew :composeApp:linkDebugFrameworkIosArm64` from the repository root.

# Current Focus

- hypothesis: Stock Kotlin/Native 2.2.21 cannot produce final Apple binaries on a Linux host; xtool's Darwin Swift SDK does not change Kotlin/Native's compiler-level host support matrix.
- test: explicitly enable the otherwise host-disabled link task in a temporary init script and observe whether the Kotlin/Native compiler accepts FRAMEWORK output for ios_arm64.
- expecting: compiler rejects FRAMEWORK for ios_arm64 on linux_x64 before invoking any Apple linker, proving this is not solvable by a Gradle task-enable or SDK-path tweak.
- next_action: checkpoint with the exact compiler rejection and request a macOS/CI-produced ComposeApp framework artifact before xtool build/device deployment can continue.
- reasoning_checkpoint:
    hypothesis: "The current ShareHelper error is caused solely by the absent iosMain actual; a UIKit UIActivityViewController implementation satisfies the expect contract and intended share action."
    confirming_evidence:
      - "The complete source tree contains ShareHelper.android.kt but no ShareHelper.ios.kt."
      - "Both common call sites require only shareText(String), which maps directly to UIActivityViewController activityItems."
    falsification_test: "compileKotlinIosArm64 still reports no corresponding actual or the UIKit implementation cannot compile against the configured iOS SDK."
    fix_rationale: "Adds the missing platform implementation at the correct iosMain boundary without changing common or Android APIs."
    blind_spots: "Compilation cannot prove presentation behavior on device; physical-device launch and invoking share remain necessary runtime checks."
- tdd_checkpoint: disabled

# Evidence

- timestamp: 2026-09-26T00:00:00+03:00
  checked: repository worktree and project-defined skills
  found: iosXtool and .planning are untracked; unrelated Compose UI files are modified. Project clean-code rules require a minimal scoped change.
  implication: preserve all unrelated edits and restrict changes to Room/KSP iOS build integration plus iosXtool launcher files.
- timestamp: 2026-09-26T00:05:00+03:00
  checked: modules/room/build.gradle.kts, CoffeePeekDatabase.kt, generated outputs
  found: kspIosArm64 and kspIosSimulatorArm64 dependencies are declared, but build/generated contains only Android Room output/caches; no Native generated constructor exists.
  implication: the declared dependency alone has not produced Native sources; task registration and compile dependency must be observed directly.
- timestamp: 2026-09-26T00:10:00+03:00
  checked: :modules:room:tasks --all
  found: Gradle registers kspKotlinIosArm64 and kspKotlinIosSimulatorArm64 tasks.
  implication: missing task registration is eliminated; next distinguish task dependency/source wiring from processor execution failure.
- timestamp: 2026-09-26T00:20:00+03:00
  checked: compileKotlinIosArm64 dry-run and standalone kspKotlinIosArm64 --info
  found: compileKotlinIosArm64 depends on kspKotlinIosArm64, but kspKotlinIosArm64 is SKIPPED because its onlyIf predicate is false; no processor error occurs.
  implication: compile wiring is present. The root cause is the KSP task's disabled/no-work predicate, not a missing dependsOn edge.
- timestamp: 2026-09-26T00:30:00+03:00
  checked: cached KSP 2.3.2 Gradle plugin source, KspAATask.registerKspAATask
  found: Native KSP adds an onlyIf predicate requiring kotlin.native.enableKlibsCrossCompilation=true OR a target enabled by HostManager. The property defaults false, and iosArm64 is not host-enabled on Linux.
  implication: Linux skips Room Native code generation by design unless KLIB cross-compilation is explicitly enabled; this precisely explains the absent actual declaration.
- timestamp: 2026-09-26T12:28:05+03:00
  checked: counterfactual :modules:room:kspKotlinIosArm64 with only kotlin.native.enableKlibsCrossCompilation=true
  found: task executed, loaded androidx.room.RoomKspProcessor, succeeded, and generated CoffeePeekDatabaseConstructor.kt containing public actual object CoffeePeekDatabaseConstructor.
  implication: the property is causally sufficient to resolve the reported missing Native actual; root cause is confirmed.
- timestamp: 2026-09-26T12:35:00+03:00
  checked: original ./gradlew :composeApp:linkDebugFrameworkIosArm64 after persisting the property
  found: kspKotlinIosArm64 ran/up-to-date, modules/room:compileKotlinIosArm64 succeeded with the generated actual, and the build advanced to modules/data:compileKotlinIosArm64 before failing at ScheduleTimeZone.ios.kt:6:17 unresolved reference localTimeZone.
  implication: the reported Room missing-actual symptom is verified fixed. Full framework output remains blocked by a distinct data-module source compatibility issue outside this agent's ownership.
- timestamp: 2026-09-26T12:48:00+03:00
  checked: SwiftPM manifest validation and xtool dev build after configuring the independent launcher
  found: Package.swift validates and xtool invokes swiftc with the exact absolute -F /home/arseny/StudioProjects/CoffeePeek-Android/composeApp/build/bin/iosArm64/debugFramework flag. It currently fails only at unable to resolve module dependency ComposeApp because Gradle has not emitted that directory.
  implication: launcher path propagation is verified; its remaining import failure is the expected consequence of the distinct upstream framework build blocker.
- timestamp: 2026-09-26T12:56:00+03:00
  checked: adjacent regression builds
  found: :modules:room:compileDebugKotlinAndroid and :modules:room:kspKotlinIosSimulatorArm64 both completed successfully with the persisted property.
  implication: the fix works for both Apple targets and does not regress Android Room compilation.
- timestamp: 2026-09-26T13:08:00+03:00
  checked: resumed :composeApp:linkDebugFrameworkIosArm64 after external timezone fix
  found: modules/room and modules/data iosArm64 compilation pass. composeApp:compileKotlinIosArm64 fails on Dispatchers.IO visibility (CityPreference, ReviewDraftStore), JVM-only format calls (ReviewContent, MapScreen, ShopDetail), stale CoffeeMap.ios actual signature, invalid DateTimeUtils.ios Foundation calls, and a missing actual modifier in ImagePicker.ios.
  implication: the framework cannot yet be emitted, so the already-prepared xtool import/link and physical-device deployment cannot advance. These failures are outside the assigned Room/KSP and launcher ownership, including overlap with user-modified UI files.
- timestamp: 2026-09-26T13:20:00+03:00
  checked: parent checkpoint after scoped compose common/iOS fixes
  found: prior composeApp diagnostics are fixed; compilation now stops only at commonMain/utils/ShareHelper.kt:6 because expected ShareHelper has no Native actual.
  implication: add the minimal platform actual authorized by the parent, then resume the framework boundary.
- timestamp: 2026-09-26T13:28:00+03:00
  checked: compileKotlinIosArm64 with initial ShareHelper.ios.kt
  found: the expect/actual and UIActivityViewController initializer compile; only popoverPresentationController/sourceView/sourceRect accesses are unresolved in the current Kotlin/Native UIKit bindings.
  implication: the missing-actual root is fixed; optional iPad popover anchoring must use an available binding or be omitted for the requested iPhone path.
- timestamp: 2026-09-26T13:32:00+03:00
  checked: :composeApp:compileKotlinIosArm64 after correcting ShareHelper.ios.kt
  found: compile completed successfully; the missing actual and all earlier source errors are gone.
  implication: proceed to the Kotlin/Native framework linker, the first remaining unverified boundary.
- timestamp: 2026-09-26T13:36:00+03:00
  checked: :composeApp:linkDebugFrameworkIosArm64 and --info diagnostics
  found: Gradle reports BUILD SUCCESSFUL but linkDebugFrameworkIosArm64 is SKIPPED because onlyIf 'Task is enabled' is false; no framework directory exists.
  implication: KLIB cross-compilation enables compilation/KSP but not Apple binary linking on Linux. The host-disabled link task is now the exact boundary.
- timestamp: 2026-09-26T13:44:00+03:00
  checked: Kotlin Gradle plugin 2.2.21 source and forced execution of linkDebugFrameworkIosArm64 via a temporary init script
  found: KGP sets task.enabled from konanTarget.enabledOnCurrentHostForBinariesCompilation. When forcibly enabled, Kotlin/Native fails immediately with "Target ios_arm64 is not available for output kind 'FRAMEWORK' on the linux_x64 host" from KonanConfig, before framework creation.
  implication: final Apple framework linking is unsupported by stock Kotlin/Native on Linux. The xtool Darwin SDK cannot be reached because the compiler rejects the host/target/output combination first; a framework built on macOS (locally or CI) is required for the prepared Linux xtool launcher.

# Eliminated

- hypothesis: Native KSP task is not registered or not wired before compileKotlinIosArm64.
  evidence: task discovery lists kspKotlinIosArm64 and the compile dry-run schedules it immediately before compileKotlinIosArm64.
  timestamp: 2026-09-26T00:20:00+03:00

# Resolution

- root_cause: KSP 2.3.2 host-gates Native symbol processing. On Linux iosArm64 is not HostManager-enabled and kotlin.native.enableKlibsCrossCompilation defaulted false, so Room's KSP task was skipped and its actual database constructor was never generated.
- fix: Added kotlin.native.enableKlibsCrossCompilation=true to gradle.properties so KSP executes Apple-target KLIB processing on Linux.
- verification: The exact original framework command no longer reports a missing CoffeePeekDatabaseConstructor actual, and :modules:room:compileKotlinIosArm64 succeeds. End-to-end framework/xtool verification is pending later build blockers.
- blocker: Stock Kotlin/Native 2.2.21 rejects FRAMEWORK output for ios_arm64 on linux_x64. The configured iosXtool launcher expects the macOS-built artifact at composeApp/build/bin/iosArm64/debugFramework/ComposeApp.framework; no external CI or macOS workflow was added without user authorization.
- files_changed: [gradle.properties, composeApp/src/iosMain/kotlin/com/coffeepeek/admin/utils/ShareHelper.ios.kt, iosXtool/CoffeePeek/Package.swift, iosXtool/CoffeePeek/Sources/CoffeePeek/CoffeePeekApp.swift]
