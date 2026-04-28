# Video Player Compose

An Android video player app built with Jetpack Compose, ExoPlayer (Media3), and Hilt. Supports video playback, trimming, and compression workflows.

## Features

- **Play Video** — Stream and play videos from remote URLs
- **Trim Video** — Browse, preview, and select videos for trimming (local & built-in)
- **Compress Video** — Video compression workflow (in progress)
- **Video Preview** — Thumbnail extraction at 1s using `MediaMetadataRetriever`
- **Local File Picker** — Import videos from device storage
- **Built-in Test Videos** — 3 sample videos from Google ExoPlayer test media

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material3 |
| Media | Media3 ExoPlayer 1.7.1, Media3 Transformer |
| DI | Hilt 2.56.2 |
| State | ViewModel, StateFlow, Compose State |
| Build | KSP, AGP 8.9.0, Kotlin 2.0.21 |

## Architecture

- **MVVM** — `MainViewModel` (Hilt-injected) manages video state via `StateFlow`
- **Jetpack Compose** — Declarative UI with `rememberSaveable` and `mutableStateOf`
- **Navigation** — Enum-based screen routing (`AppScreen`) in `MainActivity`
- **ExoPlayer wrapper** — `AndroidVideoPlayer.kt` wraps Media3 for composable use

## Requirements

- Android 11+ (minSdk 30)
- Target SDK 35 (Android 15)
- Internet permission for remote video streaming

## Project Structure

```
app/src/main/java/com/example/playvideo/
├── MainActivity.kt              # Entry point, navigation host
├── MainViewModel.kt             # Video state & preview bitmap cache
├── PlayVideoApplication.kt      # Hilt application class
├── AndroidVideoPlayer.kt        # ExoPlayer composable wrapper
├── PlayerSurface.kt             # Custom Media3 PlayerView
├── data/
│   ├── VideoInfoData.kt         # Video metadata model
│   └── videos.kt                # Built-in video URL list
├── ui/
│   ├── HomeScreen.kt            # Home screen with 3 action cards
│   └── TrimChooseVideoScreen.kt # Video selection & preview screen
└── util/
    └── VideoHelper.kt           # Thumbnail extraction, URL filtering
```

## Getting Started

1. Clone the repository
2. Open in Android Studio Hedgehog or later
3. Sync Gradle and run on a device/emulator running Android 11+

## Dependencies

See [`gradle/libs.versions.toml`](gradle/libs.versions.toml) for the full version catalog.

Key dependencies:
- `androidx.media3:media3-exoplayer` 1.7.1
- `androidx.media3:media3-transformer` 1.7.1
- `androidx.media3:media3-exoplayer-hls` 1.7.1
- `com.google.dagger:hilt-android` 2.56.2
- Compose BOM 2024.09.00
