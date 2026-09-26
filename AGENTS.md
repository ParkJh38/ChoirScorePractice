# Project conventions

## Scope and boundaries
- Build an offline Android choir practice app in Kotlin for Samsung Galaxy devices.
- Keep OMR on device. Do not add a remote recognition service or upload score/pronunciation data.
- Do not integrate HOMR or AGPL code, models, binaries, or assets in this foundation.
- Evaluate code, model weights, datasets, and piano sample licenses separately before adding an engine or sound library. Preserve the existing Apache 2.0 license.
- All recognition integrations implement `core/omr/OmrEngine`; engine-specific types stay in adapters. The default reports unavailable, never fabricated recognition.

## Structure and implementation
- `app`: Android lifecycle, resources, document access, UI, and future platform adapters.
- `core`: Android-independent Kotlin score models and contracts. No Android imports or engine SDK dependencies.
- Use Kotlin, Gradle Kotlin DSL, and pinned versions in `gradle/libs.versions.toml`. Keep the Gradle wrapper checked in.
- Use four-space Kotlin indentation, descriptive names, immutable data where practical, and string resources for visible UI text.
- Prefer small focused changes; add modules and frameworks only when needed.
- Run PDF rendering, inference, and audio preparation off the main thread. Support cancellation and bounded memory; propagate cancellation rather than converting it to a recognition failure.
- Use the Storage Access Framework for imports. Keep user documents and pronunciation private; never commit them, SDK paths, signing keys, or generated output.
- Preserve Korean text as Unicode. Do not infer syllable-to-note alignment without review.
- Keep part identity separate from role so divisi and unknown staves remain editable.

## Verification and collaboration
- Inspect existing files and git status before editing; preserve unrelated work.
- Use `./gradlew :core:test :app:assembleDebug :app:lintDebug` for relevant changes.
- Add meaningful tests for recognition mapping, timing, part mixing, and text handling as implemented.
- Smoke test on macOS Android Studio Emulator; verify performance, audio latency, and Samsung-specific behavior on Galaxy hardware later.
- Record checks that could not run and why. Do not describe planned features as implemented.
- Update architecture/roadmap when boundaries or milestones change.
- Review `git diff --check`, staged diff, and status before commits. Push only when explicitly requested.
