# Honor of Kings Recorder

An Android app for recording [Honor of Kings](https://www.honorofkings.com/) matches with an automated 100-point scoring system, match history, and performance statistics.

## Features

- **Record matches** — log hero, economy, deaths, and 6 performance criteria
- **Auto-scoring** — 100-point formula evaluating economy, survival, objectives, teamwork, and mental game
- **Match history** — sortable list with swipe-to-delete
- **Stats** — 4 charts: score trend, win rate by hero, score distribution, category breakdown

## Scoring Formula

| Criterion | Points |
|---|---|
| Economy ≥ 6500 | +15 |
| Deaths ≤ 2 | +10 |
| Baron killed/secured | +10 |
| 3-question check passed | +10 |
| Relied on team | +10 |
| Pushed tower | +10 |
| Engaged strongest enemy | +10 |
| Mental stability maintained | +15 |
| Has post-match notes | +10 |
| **Total** | **100** |

## Heroes Supported

后羿 · 莱西奥 · 艾琳 · 戈娅 · 孙尚香 · 公孙离

## Tech Stack

- **Language**: Kotlin 2.3.20
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM
- **DI**: Hilt
- **Database**: Room
- **Navigation**: Navigation Compose
- **Charts**: Vico 3

## Requirements

- Android 7.0+ (API 24+)
- Android Studio Meerkat or later

## Build

```bash
./gradlew assembleDebug
```
