# Third-party dependencies

The input milestone adds AndroidX components maintained by the Android Open Source Project. The published Maven POM license declarations for the pinned versions were inspected in the Gradle dependency cache on 2026-09-28. Each lists the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

| Dependency | Version | Purpose |
| --- | --- | --- |
| androidx.activity:activity-ktx | 1.10.1 | Lifecycle-aware Activity and document picker result contracts |
| androidx.lifecycle:lifecycle-viewmodel-ktx | 2.8.7 | Retained input state and scoped background work |
| androidx.lifecycle:lifecycle-runtime-ktx | 2.8.7 | Lifecycle-aware UI collection |
| androidx.test:core | 1.6.1 | Test-only Activity recreation |
| androidx.test:runner | 1.6.2 | Instrumentation runner |
| androidx.test.espresso:espresso-core | 3.6.1 | Test-only UI interaction |
| androidx.test.espresso:espresso-intents | 3.6.1 | Test-only picker result interception |

Versions remain pinned to the SDK 35 foundation. This inventory records the new direct dependencies, not a complete release attribution audit of all transitives. Retain dependency notices and perform the full distribution audit before release. No OMR engine, inference runtime, model weights, datasets, or piano samples were introduced.

The original Kotlin/JUnit/Gradle foundation and repository Apache 2.0 license are unchanged. AndroidX documentation: [Activity](https://developer.android.com/jetpack/androidx/releases/activity), [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle), and [Storage Access Framework](https://developer.android.com/training/data-storage/shared/documents-files).
