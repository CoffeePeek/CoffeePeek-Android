# CoffeePeek with xtool

Use a device (`iosArm64`) debug framework from the `iOS Compose Framework`
GitHub Actions workflow. Check out the same commit locally before preparing
resources; resource offsets in the framework must match the generated files.

1. Extract the downloaded artifact ZIP. Verify the tar archive against its
   SHA-256 file (the checksum currently refers to `artifacts/<archive>`).
2. From the repository root, extract the tar archive into
   `composeApp/build/bin/iosArm64/debugFramework/`. This directory must contain
   `ComposeApp.framework/ComposeApp`.
3. Prepare resources on Linux:

   ```bash
   bash iosXtool/CoffeePeek/prepare-resources.sh
   ```

4. Build, sign, and install on a connected, trusted iPhone:

   ```bash
   cd iosXtool/CoffeePeek
   xtool dev
   ```

The preparation script stages the resources under
`.build/compose-resources/composeResources/`, matching the static framework's
iOS resource reader. Run it again after resource changes or cleaning `.build`.
`xtool.yml` copies this directory into the app and merges
`Info.plist`, including the API URL and privacy descriptions. Google Sign-In
is not configured in this launcher yet.

An installation ending with `Verifying 100%` does not prove the UI has launched.
Open CoffeePeek on the phone to check it. For command-line launch, obtain the
prefixed identifier using `xtool ds identifiers list`, then run
`xtool launch <identifier>`. If debugserver launch fails, manual launch is
still necessary to verify runtime behavior.

The framework is generated on macOS CI; Kotlin/Native on Linux can compile
KLIBs and prepare resources but cannot link this Apple framework.
