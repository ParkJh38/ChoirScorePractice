# Foundation verification

Verified on macOS on 2026-09-26 with OpenJDK 21.0.3.

- Repository initially contained only `README.md` and the Apache 2.0 `LICENSE`; working tree was clean.
- `./gradlew :core:test`: passed (2 tests), compiling the score models and OMR contract.
- Gradle configured both modules successfully with the pinned plugins.
- Official Gradle 8.11.1 wrapper JAR SHA-256 matched the checksum published at https://services.gradle.org/distributions/gradle-8.11.1-wrapper.jar.sha256.
- Wrapper distribution SHA-256 is pinned in `gradle-wrapper.properties`.
- Android manifest/resource XML parsing, version catalog parsing, executable wrapper, module layout, and core Android-independence checks passed.
- Ignore checks passed for local SDK configuration, IDE metadata, build output, Gradle/Kotlin caches, and signing keys.
- Whitespace diff checks passed before commit. No OMR SDK, model, HOMR, or AGPL code was added.

## Pending environment-dependent verification

`./gradlew :core:test :app:assembleDebug :app:lintDebug` stopped while determining Android task dependencies because no Android SDK location was configured. Core tests were then run separately and passed. Android APK compilation and lint are **not verified**.

Android Studio and an Android SDK were not found in their standard macOS locations. No emulator launch or Galaxy hardware test was performed. Install/configure the tooling described in the README, rerun the full verification command, and perform the documented emulator smoke checks before treating this foundation as device-validated.
