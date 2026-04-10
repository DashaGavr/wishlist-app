# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Android
./gradlew :composeApp:assembleDebug

# Desktop (JVM)
./gradlew :composeApp:run

# Web (Wasm) — faster, modern browsers only
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Web (JS) — broader browser support
./gradlew :composeApp:jsBrowserDevelopmentRun

# iOS — open iosApp/iosApp.xcodeproj in Xcode and build from there

# Tests (all platforms)
./gradlew :composeApp:allTests

# Single test class
./gradlew :composeApp:jvmTest --tests "org.example.project.ComposeAppCommonTest"
```

## Architecture

This is a **Kotlin Multiplatform + Compose Multiplatform** project targeting Android, iOS, Desktop (JVM), Web (Wasm), and Web (JS). All targets are declared in a single module: `:composeApp`.

### Source Sets

| Source set | Purpose |
|---|---|
| `commonMain` | Shared UI (`App.kt`), business logic, `Platform` interface |
| `androidMain` | `MainActivity`, Android `Platform` impl |
| `jvmMain` | Swing window entry point (`main.kt`), JVM `Platform` impl |
| `iosMain` | `MainViewController` (SwiftUI bridge), iOS `Platform` impl |
| `jsMain` / `wasmJsMain` | Web entry points and `Platform` impls |
| `commonTest` | Shared tests using `kotlin.test` |

### Expect/Actual Pattern

Platform-specific behavior is isolated via `expect`/`actual`. `Platform.kt` in `commonMain` declares:

```kotlin
interface Platform { val name: String }
expect fun getPlatform(): Platform
```

Each platform source set provides `actual fun getPlatform()`. `Greeting.kt` uses this to produce platform-aware output. Follow this pattern for any new platform-specific code.

### Key Files

- `composeApp/src/commonMain/kotlin/org/example/project/App.kt` — root `@Composable`, the shared UI entry point
- `composeApp/build.gradle.kts` — all multiplatform target/dependency configuration
- `gradle/libs.versions.toml` — centralized version catalog (Kotlin 2.3.20, Compose 1.10.3, AGP 8.11.2)

### Dependency Management

All dependency versions live in `gradle/libs.versions.toml`. Reference them via type-safe accessors (`libs.kotlin.test`, etc.) — do not hardcode version strings in build files.