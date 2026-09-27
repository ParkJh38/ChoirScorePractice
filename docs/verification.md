# Verification history

## PDF score viewer — 2026-09-28

- Required `./gradlew :core:test :app:assembleDebug :app:lintDebug` checks passed; the final combined run also included `:app:connectedDebugAndroidTest`.
- Core: all 6 tests passed. Emulator: all 10 tests passed on `Medium_Phone`, Android 15 / API 35, ARM64.
- Viewer coverage includes rendered pixel content on a 24-page mixed-geometry PDF, scrolling/recycling, capped allocation dimensions, zoom/horizontal pan, page/zoom retention across recreation, unavailable-file recovery, stored URI/read-grant reopening, revoked access, and returning to unchanged Korean text. Existing input regression tests also passed.
- Real DocumentsUI smoke check: selected a synthetic local three-page score; inspected the rendered score, exercised zoom and touch panning/vertical scrolling, and confirmed persistent access and the selected filename after force-stopping/relaunching the app.
- Lint: zero errors and no functional/style findings. The 24 remaining warnings are existing dependency/plugin version-update advisories. RecyclerView’s pinned dependency and Apache 2.0 POM license were reviewed.
- No OMR or remote service was added. Generated APKs, local SDK settings, screenshots, and synthetic manual-test PDFs are not committed.

Known limits and repeatable checks are in [PDF viewer validation](pdf-viewer-validation.md). Galaxy hardware, API 26, native-memory performance, and exhaustive PDF compatibility were not tested.

## Score and pronunciation input — 2026-09-28

Verified on macOS with SDK Platform 35 / Build Tools 35.0.0 and the running `Medium_Phone` ARM64 Android 15 (API 35) emulator.

- `./gradlew :core:test :app:assembleDebug :app:lintDebug`: passed. All three tasks also passed in the final combined run with `:app:connectedDebugAndroidTest`.
- Core: 6 tests passed (4 text import tests, 2 existing note model tests).
- Emulator instrumentation: 4 tests passed, covering filenames, exact Korean Unicode/BOM/CRLF, direct editing and Android clipboard paste, Activity recreation, cancellation, malformed/oversized TXT, and rejected PDF headers without losing prior selections.
- Real DocumentsUI smoke test: launched the app, selected a synthetic local PDF from Downloads, then selected a UTF-8 TXT. Both filenames and Korean text with decomposed Jamo, leading spaces, and blank lines appeared correctly. MIME filtering enabled only the relevant file type in each picker.
- Lint: 0 errors. Icon, backup configuration, and editor label findings fixed. Remaining 24 warnings are version-update advisories only (`GradleDependency` / `AndroidGradlePluginVersion`); pinned SDK 35-compatible tooling remains intentional.
- `git diff --check`: passed. Local SDK configuration and generated APK/build output remain ignored.

Limitations: API 26 and physical Galaxy devices were not exercised. TalkBack, large-font layouts, and Korean IME composition were not manually validated. State survives rotation but is session-only; process-death recovery/persistence and full PDF validation/preview remain future work. No OMR or playback was added or claimed tested.

## Foundation — 2026-09-26 (historical)

Verified on macOS on 2026-09-26 with OpenJDK 21.0.3.

- Repository initially contained only `README.md` and the Apache 2.0 `LICENSE`; working tree was clean.
- `./gradlew :core:test`: passed (2 tests), compiling the score models and OMR contract.
- Gradle configured both modules successfully with the pinned plugins.
- Official Gradle 8.11.1 wrapper JAR SHA-256 matched the checksum published at https://services.gradle.org/distributions/gradle-8.11.1-wrapper.jar.sha256.
- Wrapper distribution SHA-256 is pinned in `gradle-wrapper.properties`.
- Android manifest/resource XML parsing, version catalog parsing, executable wrapper, module layout, and core Android-independence checks passed.
- Ignore checks passed for local SDK configuration, IDE metadata, build output, Gradle/Kotlin caches, and signing keys.
- Whitespace diff checks passed before commit. No OMR SDK, model, HOMR, or AGPL code was added.

### Environment-dependent verification pending at foundation time

`./gradlew :core:test :app:assembleDebug :app:lintDebug` stopped while determining Android task dependencies because no Android SDK location was configured. Core tests were then run separately and passed. Android APK compilation and lint are **not verified**.

Android Studio and an Android SDK were not found in their standard macOS locations. No emulator launch or Galaxy hardware test was performed. Install/configure the tooling described in the README, rerun the full verification command, and perform the documented emulator smoke checks before treating this foundation as device-validated.
