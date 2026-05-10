# TrimScreen UI Documentation

Full UI breakdown of the `TrimScreen` Composable from `TrimVideoActivity`, covering layout structure, each component, all dialog states, and the data model.

---

## Screen Overview

A full-screen video trimming screen implemented as an `Activity` (not a Fragment/NavGraph destination). Uses `Scaffold` with `WindowInsets.safeContent`. Background is always **dark** (`DarkColorScheme.background`).

### Top-level layout (vertical `Column`, dark background)

```
┌──────────────────────────────────┐
│         TrimVideoTopBar          │  fixed height, fillMaxWidth
├──────────────────────────────────┤
│  16dp Spacer                     │
├──────────────────────────────────┤
│         TrimVideoPlayer          │  fillMaxWidth, weight(1f) — takes remaining vertical space
├──────────────────────────────────┤
│         TrimVideoSeekBar         │  paddingTop=24dp, paddingBottom=32dp
└──────────────────────────────────┘
```

A `TrimDialog` overlay is always composed above the column and renders conditionally based on dialog state.

---

## Component: `TrimVideoTopBar`

**File:** `components/TrimVideoTopBar.kt`

A `Box` (fillMaxWidth) acting as a custom top app bar. Three items positioned inside it:

| Position | Element | Behavior |
|---|---|---|
| CenterStart | Back arrow icon (`ic_back_arrow`) | 28dp, circle clip, 12dp start padding, calls `onBackClick` |
| Center | Title `Text` | 15sp Medium, centered, horizontalPadding=36dp, truncates if too long |
| CenterEnd (outer) | Information icon (`ic_information`) | 28dp, 56dp end padding, enabled only when `isReadyToTrim=true` |
| CenterEnd (inner) | Trim scissors icon (`ic_trim`) | 28dp, 12dp end padding, enabled only when `isReadyToTrim=true` |

**Color logic (icons):**
- `isReadyToTrim = true` → `DarkColorScheme.primary` / `LightColorScheme.primary`
- `isReadyToTrim = false` → `DarkGanbaruColors.deselected` / `LightGanbaruColors.deselected`

**Typography (title):**
- FontFamily: `APP_SF_PRO_TEXT_FONT_FAMILY`
- Weight: Medium, Size: 15sp, LineHeight: 20sp, LetterSpacing: -0.41sp
- Color: `primaryTextColor` (dark/light)

---

## Component: `TrimVideoPlayer`

**File:** `components/TrimVideoPlayer.kt`

A `Box` (fillMaxWidth + weight(1f)) containing an ExoPlayer video view.

### States

| State | UI |
|---|---|
| Loading (player not yet ready) | `CircularProgressIndicator` centered, 32dp, strokeWidth=2dp, StrokeCap.Round |
| Player ready | `AndroidView` wrapping `PlayerView` (Media3), `RESIZE_MODE_FIT`, horizontal padding=16dp, fills the box |

### Behavior
- Auto-plays on load (`playWhenReady = true`)
- Responds to `seekTo: Long` — seeks player when value is in `[0, duration]`
- Responds to `isStopVideo: Boolean` — stops playback, then calls `onResetStateStopPlayVideo()`
- On init failure calls `onInitPlayerFailed()`
- Player released in `DisposableEffect`

---

## Component: `TrimVideoSeekBar`

**File:** `components/TrimVideoSeekBar.kt`

A horizontally scrollable seek bar for selecting the trim range. Only renders if `frameBitmaps` is non-empty.

### Visual structure

```
┌─────────────────────────────────────────────────────┐  height=64dp
│[L]│▓▓▓│ frame0 │ frame1 │ ... │ frameN │▓▓▓│[R]│
└─────────────────────────────────────────────────────┘
```

- **White** rounded background with 1dp divider border (`RoundedCornerShape(8dp)`)
- 24dp horizontal padding from screen edges
- Total width: min(all-frames width, screen width - padding - thumbs)

### Elements

| Element | Description |
|---|---|
| `LazyRow` | Horizontally scrollable, clips to `RoundedCornerShape(8dp)` |
| Left spacer (`SeekBarSide`) | 20dp wide, draws 3 horizontal dark-gray lines (grip indicator) |
| Frame images | `GlideImage` per bitmap, size = `(bitmapWidth/density) × 64dp`, `ContentScale.Crop` |
| Right spacer (`SeekBarSide`) | Same as left side spacer |
| Dark overlay (left) | `Color.DarkGray, alpha=0.8f` rect drawn over frames left of left thumb |
| Dark overlay (right) | `Color.DarkGray, alpha=0.8f` rect drawn over frames right of right thumb |
| Left thumb | Arrow icon (`ic_left_arrow_with_circle_border`), 20×64dp, white `RoundedCornerShape(topStart=8dp, bottomStart=8dp)` bg |
| Right thumb | Same icon rotated 180°, white `RoundedCornerShape(topEnd=8dp, bottomEnd=8dp)` bg |

### Thumb labels
- Left thumb: timestamp label drawn **above** thumb (y = -labelHeight - 4dp)
- Right thumb: timestamp label drawn **below** thumb (y = thumbHeight + 4dp)
- Font: 12sp Normal, LetterSpacing=0.2sp, lineHeight=15sp

### Interaction
- Both thumbs respond to **horizontal drag gestures**
- Constraints: left thumb cannot exceed right thumb position; range is capped at `maxTrimTime` (default 45,000ms)
- When max trim time is exceeded, the opposite thumb is auto-shifted to maintain the max window
- Scroll position is tracked and used to recalculate thumb padding offsets

---

## Dialogs (`TrimDialog`)

**File:** `components/TrimVideoDialog.kt`

A single entry-point `TrimDialog` Composable that switches on `TrimVideoDialogState`. All dialogs use `DialogWrapper` — a custom full-screen `Dialog` with animated scale+fade in/out (300ms).

### DialogWrapper visual
- `Dialog` with `usePlatformDefaultWidth = false`, dismissible on back press and outside click
- Content card: Portrait = 85% width, Landscape = 75% width
- Shape: `RoundedCornerShape(24dp)`
- Background: `DarkColorScheme.background` / `LightColorScheme.background`
- Padding: 16dp inside
- Animated entry: `fadeIn + scaleIn(initialScale=0.2f)` / exit: `fadeOut + scaleOut`
- On Android 15+ (VanillaIceCream): dialog shifts up by status bar height to avoid overlap

---

### Dialog State: `StandBy`
No dialog rendered.

---

### Dialog State: `Loading`
**Composable:** `AppLoadingDialog` (shared component)
- Shows a loading indicator with a message string

---

### Dialog State: `Error`
**Composable:** `ErrorDialog`

```
┌─────────────────────┐
│  [Title — 22sp Bold]│
│  16dp spacer        │
│  [message text      │  scrollable, max 60% screen height
│   14sp SemiBold]    │
│  24dp spacer        │
│  [Close] button →   │  red background (error color)
└─────────────────────┘
```

---

### Dialog State: `Information`
**Composable:** `VideoInformationDialog`

Orientation-aware (Portrait vs Landscape):

```
┌─────────────────────┐
│ "Video Information" │  22sp SemiBold, centered
│  16dp spacer        │
│  File name: ...     │  14sp SemiBold, scrollable list
│  File size: ...     │
│  Video duration:... │
│  Max allowed time:..│
│  [debug-only rows]  │  frameRate, bitRate, width, height, rotation
│  [☐] Export to Movies external storage  │  debug-only checkbox
│  [Close] button →   │
└─────────────────────┘
```

Debug-only fields shown when `videoData is DebugVideoInformationUiData`.

---

### Dialog State: `AskSelectOptionToTrimVideo`
**Composable:** `AskSelectOptionToTrimVideoDialog`

User selects one of two trim options (radio-style):

```
┌─────────────────────────────┐
│  "Trim Video" (22sp Bold)   │
│  "Please select an option…" │  14sp Normal, centered
│  16dp spacer                │
│  ◉ Very fast but no         │  Option 1 (default selected)
│    compression…             │
│  16dp spacer                │
│  ○ Slow but compresses      │  Option 2
│    well…                    │
│  24dp spacer                │
│              [Trim] →       │
└─────────────────────────────┘
```

- Default selected: "Very fast" (maps to `TrimVideoOption.TrimExactly`)
- "Slow" option maps to `TrimVideoOption.TrimAndCompress`
- `OptionLine` uses `AnimatedChecker` (animated vector `anim_vector_ic_check_white`) + text

---

### Dialog State: `WarnSelectedNonKeyFrame`
**Composable:** `WarnSelectedNonKeyFrameDialog`

Similar to `AskSelectOptionToTrimVideo` but option 1 warns about inexact trimming, with an expandable detail section:

```
┌─────────────────────────────┐
│  "Trim Video" (22sp Bold)   │
│  "Please select an option…" │  tap to show technical details
│                             │
│  ◉ Very fast but inexactly… │  tap to expand detail
│    [expandable detail text] │  AnimatedVisibility, tap to collapse
│                             │
│  ○ Slow but compresses…     │
│                             │
│  [Close]        [Trim] →    │  side-by-side buttons
└─────────────────────────────┘
```

- Option 1 maps to `TrimVideoOption.TrimInexactly(nearestBeforeKeyFrame, nearestAfterKeyFrame)`
- Option 2 maps to `TrimVideoOption.TrimAndCompress`
- Detail text uses `<b>...</b>` pseudo-markup parsed inline to `AnnotatedString` (SemiBold spans)
- Landscape mode: options in a scrollable `Column` with `weight(1f)`

---

## `DialogButton` component

Used in all dialogs. Two overloads:

| Overload | Alignment | Shape |
|---|---|---|
| `BoxScope.DialogButton` | `Alignment.CenterEnd` inside a `Box` | `RoundedCornerShape(16dp)` |
| Regular `DialogButton` | no alignment constraint | `RoundedCornerShape(16dp)` |

- Background: primary color by default (error color for error dialog close button)
- Text: white, 14sp SemiBold
- Padding: 12dp horizontal, 6dp vertical

---

## Data Model

### `TrimVideoUiModel` (screen state)

| Field | Type | Default | Purpose |
|---|---|---|---|
| `title` | `String` | `""` | Top bar title |
| `isReadyToTrim` | `Boolean` | `false` | Enables info + trim buttons |
| `videoUri` | `Uri?` | `null` | Source for ExoPlayer |
| `seekTo` | `Long` | `-1L` | Seek command to player (ms) |
| `videoDurationInMilSecond` | `Long` | `0L` | Total video duration |
| `maxAllowedTrimTime` | `Long` | `45L` | Max trim window (used as ms in seek bar) |
| `frameBitmaps` | `List<Bitmap>` | `emptyList()` | Thumbnails shown in seek bar |
| `isStopPlayVideo` | `Boolean` | `false` | Triggers player stop |
| `startSeekTime` | `Long` | `0L` | Left thumb position (ms) |
| `endSeekTime` | `Long` | `45_000L` | Right thumb position (ms) |

### `TrimVideoOption` (sealed)

| Option | Meaning |
|---|---|
| `TrimExactly` | Fast stream-copy trim to nearest keyframe |
| `TrimInexactly(start, end)` | Stream-copy, user warned about inexact cut, snaps to nearest keyframes |
| `TrimAndCompress` | Full re-encode + compression via Media3 |

### `AppTrimVideoData` (input intent data)

| Field | Type | Default |
|---|---|---|
| `videoFilePath` | `String?` | `null` |
| `title` | `String` | `"Trim Video"` |
| `sourceFileName` | `String` | `""` |
| `outputFilePath` | `String?` | `null` |
| `maxAllowedTrimTime` | `Long` | `45_000L` ms |
| `compressOption` | `AppCompressVideoData` | see below |

### `AppCompressVideoData`

| Field | Default |
|---|---|
| `frameRate` | 30 fps |
| `crf` | 23 (H.264 quality factor) |
| `width` | 640px min dimension |
| `height` | 640px min dimension |
| `targetBitrateBps` | 0 (no cap) |

---

## Navigation / Result

The screen is launched as an `Activity` and returns results via `setResult`:

| Result | Data |
|---|---|
| `RESULT_OK` | `intent.extras[INTENT_TRIMMED_VIDEO_PATH]` = output path, `intent.extras[INTENT_SOURCE_VIDEO_NAME]` = source filename |
| `RESULT_CANCELED` | No extras — user backed out without trimming |

---

## Theme / Colors

| Usage | Dark token | Light token |
|---|---|---|
| Screen + dialog background | `DarkColorScheme.background` | `LightColorScheme.background` |
| Primary accent (icons, buttons, thumbs, progress) | `DarkColorScheme.primary` | `LightColorScheme.primary` |
| Disabled/deselected icons | `DarkGanbaruColors.deselected` | `LightGanbaruColors.deselected` |
| Primary text | `DarkGanbaruColors.primaryTextColor` | `LightGanbaruColors.primaryTextColor` |
| Seek bar border | `DarkGanbaruColors.divider` | `LightGanbaruColors.divider` |
| Error dialog close button | `DarkColorScheme.error` | `LightColorScheme.error` |
| Seek bar background | `Color.White` | `Color.White` |
| Trim overlay | `Color.DarkGray, alpha=0.8f` | same |

> Note: The main screen **always** uses dark mode colors for the column background regardless of system theme. Only dialogs and some icons adapt to system dark/light mode.

---

## Dependencies / Libraries

| Library | Usage |
|---|---|
| `androidx.media3.exoplayer` | Video playback in `TrimVideoPlayer` |
| `androidx.media3.ui.PlayerView` | Render ExoPlayer output |
| `com.bumptech.glide` (Compose integration) | Frame thumbnail images in seek bar |
| Hilt | DI (`@AndroidEntryPoint`, `@HiltViewModel`) |
| Jetpack Compose + Material3 | All UI |
| `animatedVectorResource` | Animated checkmark in option selectors |
