# Score and pronunciation input validation

## Automated checks

```sh
./gradlew :core:test :app:assembleDebug :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```

Core tests cover exact Unicode (precomposed Hangul, decomposed Jamo, supplementary characters, BOM), whitespace/CRLF preservation, empty input, the 64 KiB boundary, malformed UTF-8, and oversized input. Existing score timing tests remain part of the suite.

Instrumentation launches the real Activity and uses intercepted SAF results plus a test-only provider with synthetic data. It covers filenames, imported and directly entered/pasted Korean text, subsequent edits, recreation, picker cancellation, malformed UTF-8, oversized TXT, and PDF header failures. The provider is included only in the test APK. Its PDF fixture checks selection/header handling, not PDF rendering.

## Manual system picker smoke test

1. Place a permitted PDF and a UTF-8 TXT containing Korean/Jamo, blank lines, and leading/trailing spaces in the emulator Downloads directory.
2. Launch the app and select the PDF using **Select PDF score**. Verify its filename.
3. Import the TXT using **Import pronunciation TXT**. Verify its filename and exact text.
4. Edit and paste Korean text using the keyboard/clipboard; rotate and verify the text and both filenames remain.
5. Open either picker and cancel. Verify existing input remains.
6. Import malformed UTF-8 or a TXT larger than 64 KiB. Verify an actionable error and unchanged previous text/filename.
7. Check large font sizes, scrolling, TalkBack labels, and keyboard/system bar spacing.

The UI explicitly states that TXT import replaces current text and that data is session-only. Process-death recovery, reopening saved documents, PDF preview, OMR, audio playback, and Galaxy hardware behavior are outside this milestone.
