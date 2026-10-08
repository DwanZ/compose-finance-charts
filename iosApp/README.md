# iosApp (Mac-only verify)

Thin SwiftUI host for the shared `Charts` framework produced by `:charts`.

## Prerequisites

- macOS with Xcode 15+
- Kotlin Multiplatform plugin / Xcode command-line tools

## Steps

1. From the repo root on a Mac:

   ```bash
   ./gradlew :charts:linkDebugFrameworkIosSimulatorArm64
   ```

2. Open `iosApp/iosApp.xcodeproj` in Xcode (create the project once if missing — see stub sources below).

3. Embed the framework from:

   `charts/build/bin/iosSimulatorArm64/debugFramework/Charts.framework`

4. Run on an iOS Simulator. The entry point calls `MainViewController()` from the shared module.

Stub Swift entry is in `iosApp/ContentView.swift` and `iosApp/iOSApp.swift`.
