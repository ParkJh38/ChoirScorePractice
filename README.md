# ChoirScorePractice

An Android app foundation for offline choir practice, written in Kotlin and aimed at Samsung Galaxy devices.

**Current state:** select a local PDF score with the system document picker, import UTF-8 pronunciation TXT, or type/paste Korean pronunciation. Both filenames are displayed. Text is editable and kept exactly as decoded, without Unicode normalization or whitespace changes. Pronunciation survives screen rotation but remains session-only; finishing the input screen or process death clears it. TXT import replaces the current text and accepts files up to 64 KiB; invalid UTF-8 and oversized files are rejected without changing existing input.

Choose **View PDF score** after selecting a PDF to read it inside the app. Pages scroll vertically; **Zoom in**, **Zoom out**, and **Fit width** provide 100–300% zoom, with sideways dragging when zoomed. Pages render on demand in the background with capped bitmap sizes.

PDF selection validates that Android can open the document and its first page. The last PDF URI and read permission are retained when the provider supports persistence, allowing it to reopen next session. The UI identifies session-only access, and unavailable/revoked documents can be selected again. No PDF copy is uploaded or created remotely. Password-protected or non-seekable PDFs are not supported; very high zoom may look softer because raster resolution is capped. OMR and audio playback remain future work. Android-independent score models and the replaceable OMR interface remain intact. No HOMR, AGPL engine, or remote service is included.

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

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. Core tests use the host JVM; Android build/lint tasks require the SDK. With an emulator running, also run `./gradlew :app:connectedDebugAndroidTest`. The instrumentation tests launch the real app, stub picker results with a test-only content provider, and check filename display, exact Korean text, editing, rotation, cancellation, import failures, multi-page rendering, zoom/pan, and persisted access. For the actual system picker and clipboard/keyboard checks, follow [input validation](docs/input-validation.md) and [PDF viewer validation](docs/pdf-viewer-validation.md). Emulator results do not establish Galaxy inference performance or audio latency.

## Layout

```text
app/                Android entry point, UI resources, future platform adapters
core/               Pure Kotlin score model and OMR contract, JVM tests
gradle/             Version catalog and checked-in Gradle wrapper
docs/               Architecture, roadmap, and foundation verification notes
AGENTS.md           Repository conventions
```

See [architecture](docs/architecture.md), [roadmap](docs/roadmap.md), [verification](docs/verification.md), and [third-party dependencies](docs/third-party.md). The repository retains its [Apache 2.0 license](LICENSE).
