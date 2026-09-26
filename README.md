# ChoirScorePractice

An Android app foundation for offline choir practice, written in Kotlin and aimed at Samsung Galaxy devices.

**Current state:** a launchable placeholder screen, Android-independent score models, and a replaceable on-device OMR interface. PDF import, Korean pronunciation input, recognition, and audio playback are not implemented. No HOMR or AGPL engine is included.

## Open on macOS

1. Install Android Studio with support for Android Gradle Plugin 8.9.2 (Meerkat or newer compatible release).
2. Install Android SDK Platform 35, Build Tools 35.0.0, Platform Tools, and Android Emulator through SDK Manager.
3. Open this repository root, select JDK 17 or 21 as the Gradle JDK, and sync. Android Studio can create the ignored `local.properties` SDK path.
4. Create an API 35 phone emulator (ARM64 on Apple Silicon, x86_64 on Intel), then run the `app` configuration.

The pinned baseline is AGP 8.9.2, Gradle 8.11.1, Kotlin 2.1.20, Java bytecode 17, compile/target SDK 35, and minimum SDK 26. This is a reproducible foundation, not a claim of current Play Store submission readiness. See the [AGP compatibility table](https://developer.android.com/build/releases/agp-8-9-0-release-notes).

Initial Gradle sync needs internet to download build dependencies; the app itself requests no internet permission.

## Verify

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Core tests use the host JVM; Android build/lint tasks require the SDK. On the emulator, verify launch, scrolling at large font sizes, rotation, system bar spacing, and that the screen accurately states that features are planned. Emulator results do not establish Galaxy inference performance or audio latency.

## Layout

```text
app/                Android entry point, UI resources, future platform adapters
core/               Pure Kotlin score model and OMR contract, JVM tests
gradle/             Version catalog and checked-in Gradle wrapper
docs/               Architecture, roadmap, and foundation verification notes
AGENTS.md           Repository conventions
```

See [architecture](docs/architecture.md), [roadmap](docs/roadmap.md), and [verification](docs/verification.md). The repository retains its [Apache 2.0 license](LICENSE).
