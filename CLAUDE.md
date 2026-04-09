# CLAUDE.md

## Project Overview

**Honor of Kings Recorder** (王者复盘) — Android app for recording Honor of Kings matches with automated scoring, match history, performance statistics, and video review with rule-based coaching.

- **Package**: `com.xiongxianfei.honorkingsrecorder`
- **Min SDK**: 24 (Android 7.0+)
- **Language**: Kotlin 2.3.20

## Build & Test Commands

```bash
# JAVA_HOME must point to Android Studio's bundled JBR
export JAVA_HOME="D:/Software/Android/Android Studio/jbr"

# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedDebugAndroidTest

# Sign APK (use java -jar, not apksigner.bat, to avoid JAVA_HOME issues)
java -jar "D:/Software/Android/Sdk/build-tools/36.0.0/lib/apksigner.jar" sign --ks <keystore> --out <output.apk> <input.apk>
```

## Architecture

**MVVM** with Jetpack Compose, Hilt DI, Room database, Navigation Compose.

```
app/src/main/java/com/xiongxianfei/honorkingsrecorder/
├── HonorApp.kt                    # @HiltAndroidApp Application class
├── MainActivity.kt                # Single-activity host
├── data/
│   ├── db/AppDatabase.kt          # Room database (version 2)
│   ├── db/MatchDao.kt             # Room DAO
│   ├── model/Match.kt             # @Entity data class
│   └── repository/MatchRepository.kt
├── di/AppModule.kt                # Hilt @Module providing DB + DAO
├── ui/
│   ├── navigation/
│   │   ├── Screen.kt              # Sealed class defining routes + bottom nav items
│   │   └── AppNavHost.kt          # NavHost with 5 tabs: Home, Record, History, Stats, Review
│   ├── screens/
│   │   ├── home/                   # Dashboard
│   │   ├── record/                 # Match recording + screenshot OCR import
│   │   ├── history/                # Match list with swipe-to-delete
│   │   ├── stats/                  # Charts (Vico)
│   │   └── review/                 # Video review + rule-based coach
│   └── theme/
└── util/
    ├── ScoreCalculator.kt         # 100-point scoring formula
    ├── ScreenshotParser.kt        # Extracts economy/deaths from OCR text
    └── coach/
        ├── CoachRule.kt           # FrameData, CoachTip data classes + CoachRule interface
        ├── CoachRuleEngine.kt     # Applies all rules to frames, returns sorted tips
        └── CoachRules.kt          # All rule objects — add new rules here
```

## Key Patterns

- **ViewModels** use `StateFlow<UiState>` collected via `collectAsStateWithLifecycle()`
- **ML Kit OCR** uses `suspendCancellableCoroutine` to bridge callback → coroutine
- **Coach rules** implement `CoachRule.evaluate(FrameData): CoachTip?` — add new rules to `CoachRules.all` list
- **Navigation** uses sealed `Screen` class with bottom nav bar (5 tabs)
- **Database migrations** in `AppDatabase.kt` — currently at version 2

## Dependencies (managed in `gradle/libs.versions.toml`)

| Category | Libraries |
|---|---|
| UI | Compose BOM, Material 3, Material Icons Extended |
| Architecture | Lifecycle ViewModel, Navigation Compose |
| DI | Hilt + KSP |
| Database | Room + KSP |
| Charts | Vico 3 (compose + compose-m3) |
| OCR | ML Kit Text Recognition Chinese |
| Testing | JUnit 4, Coroutines Test, Espresso, Room Testing |

## Conventions

- UI text is in **Chinese** (app name: 王者复盘)
- Coach rule tips reference the [wzry-marksman-playbook](https://github.com/xiongxianfei/wzry-marksman-playbook) rule numbers
- Version managed in `app/build.gradle.kts` (`versionCode` / `versionName`)
- Single-module project (`:app` only)
- ProGuard enabled for release builds (`isMinifyEnabled = true`, `isShrinkResources = true`)
