# Roadmap

## 0 — Foundation (completed)
- Kotlin Android app and pure Kotlin core module, pinned build tooling and wrapper.
- Placeholder home screen, choir role/note models, engine-neutral OMR contract.
- Android ignore rules, project conventions, architecture, and verification instructions.
- No OMR integration or audio implementation.

## 1a — Score and pronunciation input (implemented)
- Local PDF/TXT system picker with MIME filters and displayed filenames.
- Bounded strict UTF-8 import and direct editable Korean text, preserving Unicode and whitespace.
- Retained session state across rotation, background I/O, and non-destructive import failures.
- JVM Unicode tests and emulator instrumentation coverage.
- No OMR or playback. PDF opening validation and viewing were added in milestone 1b.

## 1b — PDF score viewer (implemented)
- In-app multi-page vertical viewer using platform PdfRenderer and recycled page rows.
- Discrete zoom and horizontal pan; background serialized rendering with bounded bitmaps.
- Preserve selection, page, and zoom across Activity recreation.
- Persist the last PDF URI/read permission when supported; handle revoked/unavailable access.
- Emulator rendering, scrolling, zoom, recreation, permission, and input regression tests.
- No OMR, upload, or remote services.

## 1c — Durable local practice workspace (next)
- Add app-private copies if needed for provider independence and eventual OMR.
- Evaluate non-seekable provider support and higher-resolution tiled rendering.
- Import UTF-8 Korean TXT or accept pasted text with editing and local persistence.
- Verify malformed/large PDFs, revoked URI access, Korean text/BOM handling, rotation, and process recreation.
- Exit: a user can reopen a local score and its pronunciation without network access.

## 2 — Engine evaluation and score review
- Assemble small, legally usable choral fixtures and expected part/note data.
- Evaluate on-device engines through adapters; document code/model licenses before integration. HOMR and AGPL integration remain excluded unless scope is explicitly changed.
- Compare note/rhythm accuracy, role identification, divisi, resource use, packaging size, and Galaxy runtime.
- Extend the core notation model based on fixture needs; provide part correction and recognition warning review.
- Exit: a documented engine decision and reproducible evaluation; no false-success stub output.

## 3 — Piano playback and practice controls
- Start with deterministic synthetic score fixtures independently of OMR readiness.
- Add a licensed piano synthesizer/sound source and accurate note scheduling.
- Implement whole choir, solo, mute, focus, tempo, and repeat controls with defined interactions.
- Verify polyphony, overlapping voices, note release on stop/loop, tempo changes, and audio interruptions.
- Exit: reviewed parts play independently and together with repeatable timing tests.

## 4 — End-to-end Galaxy validation
- Join PDF → local recognition → review → pronunciation → piano practice.
- Test API 26 and API 35 emulators on macOS, then representative Samsung Galaxy hardware.
- Measure inference duration, peak memory, thermal behavior, audio latency, and offline cold start.
- Check large fonts, TalkBack, Korean display, dark theme, rotation, and lifecycle recovery.
- Reassess target SDK/tooling and release requirements before distribution.
- Exit: documented device results and no network dependency for the practice workflow.
