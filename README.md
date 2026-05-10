# Media3 Video Player

An Android video player app built with Jetpack Compose, ExoPlayer (Media3), and Hilt. Supports video playback, trimming, and compression workflows.

## Features

- **Play Video** — Stream and play videos from remote URLs
- **Trim Video** — Browse, preview, and select videos for trimming (local & built-in); cut to exact keyframe or re-encode with compression
- **Compress Video** — Re-encode video with configurable bitrate using Media3 Transformer
- **Video Preview** — Thumbnail extraction using `MediaMetadataRetriever`
- **Local File Picker** — Import videos from device storage via `ActivityResultContracts.GetContent`
- **Built-in Test Videos** — 3 sample videos from Google ExoPlayer test media
- **Trim Seek Bar** — Draggable dual-thumb seek bar with frame thumbnails and darkened overlay for unselected regions
- **Trim Dialogs** — Option selection dialogs for trim mode (fast keyframe cut vs. slow re-encode), video info, loading progress, and error states

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material3 |
| Media | Media3 ExoPlayer 1.7.1, Media3 Transformer 1.7.1 |
| DI | Hilt 2.56.2 |
| State | ViewModel, StateFlow, Compose State |
| Build | KSP 2.0.21-1.0.28, AGP 8.9.0, Kotlin 2.0.21 |

## Architecture

- **MVVM** — `MainViewModel` (navigation/screen state), `VideoViewModel` (video metadata & bitmap cache), `EditVideoViewModel` (trim/compress operations and dialog state)
- **Jetpack Compose** — Declarative UI; enum-based screen routing (`AppScreen`) in `MainActivity`
- **ExoPlayer wrapper** — `AndroidVideoPlayer.kt` wraps Media3 for composable use; `TrimVideoPlayer.kt` manages the player on the trim screen

## Requirements

- Android 11+ (minSdk 30)
- Target SDK 35 (Android 15)
- Internet permission for remote video streaming

## Project Structure

```
app/src/main/java/com/example/playvideo/
├── MainActivity.kt                   # Entry point, enum-based navigation host
├── MainViewModel.kt                  # Screen routing & VideoOption state
├── VideoViewModel.kt                 # Video metadata, frame bitmaps, selected video
├── PlayVideoApplication.kt           # Hilt application class
├── AndroidVideoPlayer.kt             # ExoPlayer composable wrapper
├── PlayerSurface.kt                  # Custom Media3 PlayerView
├── data/
│   ├── VideoInfoData.kt              # Video metadata + trim state model
│   ├── VideoMetaData.kt              # Raw metadata (resolution, fps, bitrate, size)
│   └── videos.kt                     # Built-in video URL list
├── ui/
│   ├── HomeScreen.kt                 # Home screen with 3 action cards
│   ├── chooseVideo/
│   │   ├── ChooseVideoScreen.kt      # Video selection & preview screen
│   │   ├── layout/                   # Header, import, preview, start-trim sections
│   │   └── uiState/ChooseVideoUiState.kt
│   ├── trimVideo/
│   │   ├── TrimVideoScreen.kt        # Trim screen: player + seek bar + dialogs
│   │   ├── EditVideoViewModel.kt     # Trim/compress operations, dialog state
│   │   ├── layout/
│   │   │   ├── TrimVideoTopBar.kt    # Back / info / trim icon bar
│   │   │   ├── TrimVideoPlayer.kt    # ExoPlayer view with loading indicator
│   │   │   ├── TrimVideoSeekBar.kt   # Dual-thumb frame-strip seek bar
│   │   │   └── TrimVideoDialog.kt    # All dialog states (loading, error, info, options)
│   │   └── uiState/TrimVideoUiState.kt  # DialogState, TrimVideoOption, TrimResultUiState
│   └── trimmedVideo/
│       └── TrimmedVideoScreen.kt     # Playback screen for the trimmed/compressed result
└── util/
    ├── AppVideoUtil.kt               # Frame extraction, trim, compress, metadata helpers
    ├── AppDimension.kt               # Shared dp constants
    ├── MathHelper.kt                 # Timestamp formatting, null-safe math
    ├── ShimmerLoading.kt             # Shimmer loading modifier
    └── VideoHelper.kt                # Thumbnail loading, URL filtering, debug logging
```

## Screens & Navigation

```
HOME
 └─► CHOOSE_TRIM_VIDEO   (pick a local or built-in video)
      └─► TRIM_VIDEO      (seek bar, player, trim/compress)
           └─► PLAY_RESULT_VIDEO  (play the output)
```

Navigation is managed entirely in `MainActivity` via a `when(currentScreen)` block driven by `MainViewModel.currentScreen`.

## Trim Seek Bar

`TrimVideoSeekBar` renders a fixed-height (64 dp) bar with:
- Video frame thumbnails extracted evenly across the clip
- White left/right thumb handles (20 dp wide) draggable to set start/end trim times
- Dark overlay on unselected regions
- Timestamp labels above (start) and below (end) the bar
- `maxTrimTime` cap (default 30 s) enforced on the right thumb

Player seeks are debounced (80 ms) so ExoPlayer is not called on every drag pixel.

## Trim Options

| Option | Behaviour |
|---|---|
| `TrimExactly` | Fast stream-copy via Media3 Transformer — no re-encode |
| `TrimInexactly(start, end)` | Stream-copy snapped to nearest keyframes (shown in `WarnSelectedNonKeyFrame` dialog) |
| `TrimAndCompress` | Full H.264 re-encode with VBR bitrate targeting `width × height × fps × 0.07` bps |

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
