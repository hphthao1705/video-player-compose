# TrimVideoSeekBar — How It Works

A full breakdown of the `TrimVideoSeekBar` Compose component so you can port it to another project.

---

## Overview

`TrimVideoSeekBar` renders a scrollable horizontal seekbar for video trimming. It shows video frame thumbnails and two draggable thumb handles — left (start) and right (end). Dragging either thumb updates the trim range. A `maxTrimTime` cap enforces the maximum allowed clip duration.

---

## Constants

```kotlin
internal const val TRIM_SEEK_BAR_HEIGHT = 64           // dp — total seekbar height
internal const val TRIM_THUMB_WIDTH = 20f              // dp — width of each thumb handle
internal const val SEEK_BAR_PADDING_HORIZONTAL = 24    // dp — outer horizontal padding
```

---

## Data Classes

### `SeekBarUiData`
Computed once (on orientation change) from the frame list and screen dimensions.

| Field | Type | Meaning |
|---|---|---|
| `widthInDp` | `Float` | Total widget width in dp, including both thumb widths |
| `videoDurationInPx` | `Float` | Total pixel span of all frames laid out horizontally |
| `frameSize` | `Size` | Width/height of a single frame in dp |
| `thumbWidthInPx` | `Float` | `TRIM_THUMB_WIDTH * density` |

### `SeekThumbUiData`
Live state for each thumb, updated on every drag and every scroll.

| Field | Type | Meaning |
|---|---|---|
| `seekTime` | `Float` | Current trim time in ms (float for precision) |
| `seekTimeInPx` | `Float` | `seekTime` mapped to pixel offset in the full frame strip |
| `scrolledSeekbarInPx` | `Float` | How many px the LazyRow has scrolled (used to convert pixel → padding) |
| `paddingStartInDp` | `Float` | `padding(start = X.dp)` applied to the thumb Image — drives visible position |

---

## Layout Structure

```
Box (fixed size = seekBarUiData.widthInDp × TRIM_SEEK_BAR_HEIGHT)
 ├── LazyRow (scrollable frame strip, clips to rounded corners)
 │    ├── item(SLIDE_LEFT_SIDE_KEY)  → SeekBarSide (blank spacer = TRIM_THUMB_WIDTH wide)
 │    ├── itemsIndexed(frameBitmaps) → GlideImage per frame
 │    └── item(SLIDE_RIGHT_SIDE_KEY) → SeekBarSide (blank spacer = TRIM_THUMB_WIDTH wide)
 ├── Image (left thumb, aligned CenterStart, positioned via padding(start))
 └── Image (right thumb, aligned CenterStart, positioned via padding(start), rotated 180°)
```

Both thumbs use `Alignment.CenterStart` and are repositioned purely through `Modifier.padding(start = paddingStartInDp.dp)` — not absolute offset.

---

## Size Calculation (`remember(orientation)`)

```
framesLongInPx  = frameWidth(px) × frameCount
durationInDp    = framesLongInPx / density

widthInDp = min(
    durationInDp,                                              // can't be wider than all frames
    screenWidthDp - SEEK_BAR_PADDING_HORIZONTAL×2
        - 2×TRIM_THUMB_WIDTH - horizontalScreenPadding        // can't exceed available screen
) + TRIM_THUMB_WIDTH×2                                        // add back the two thumb widths
```

The seekbar is at most as wide as the screen, but also at most as wide as all frames combined. The two `SeekBarSide` spacer items ensure the left and right ends of the frame strip are reachable when scrolled.

---

## Scroll Tracking

```kotlin
val scrolledInPx: Float by remember {
    derivedStateOf {
        if (firstVisibleItemIndex == 0)
            firstVisibleItemScrollOffset.toFloat()
        else
            TRIM_THUMB_WIDTH×density + (firstVisibleItemIndex - 1)×frameWidth×density
                + firstVisibleItemScrollOffset
    }
}
```

`TRIM_THUMB_WIDTH×density` accounts for the left spacer item. `scrolledInPx` is used to convert an absolute pixel time-position into a visible padding value:

```kotlin
paddingStartInDp = (seekTimeInPx - scrolledSeekbarInPx) / density
```

When the user scrolls, a `LaunchedEffect(scrolledInPx)` fires and recalculates `paddingStartInDp` for both thumbs so they stay visually locked to their time positions.

---

## Thumb State Initialization

Both states are created with `remember(startSeekTime)` / `remember(endSeekTime)`, so they reset when the parent passes new values.

**Left thumb:**
```kotlin
seekTimeInPx = startSeekTime × videoDurationInPx / videoDurationInMilSecond
paddingStartInDp = seekTimeInPx / density
```

**Right thumb:**
```kotlin
seekTimeInPx = endSeekTime × videoDurationInPx / videoDurationInMilSecond
paddingStartInDp = seekTimeInPx / density + TRIM_THUMB_WIDTH
// +TRIM_THUMB_WIDTH because right thumb sits at the END of the selection
```

---

## Drag Logic

Both thumbs use `detectHorizontalDragGestures`. On each drag event:

```
newSeekTimeInPx = prevSeekTimeInPx + dragAmount
newStartPadding = (newSeekTimeInPx - scrolledSeekbarInPx) / density   [+ TRIM_THUMB_WIDTH for right]
newSeekTime     = newSeekTimeInPx × videoDurationInMs / videoDurationInPx
```

### Left thumb drag — three cases

| Condition | Action |
|---|---|
| `newStartPadding < 0` | Ignore (thumb can't go before start of strip) |
| `newSeekTime < current` AND `rightTime − newSeekTime > maxTrimTime` | Accept drag, then **push right thumb forward** to `newSeekTime + maxTrimTime` |
| `newStartPadding ≤ rightThumb.paddingStart − TRIM_THUMB_WIDTH` | Accept drag normally |
| else | Ignore (would overlap right thumb or exceed maxTrimTime from left direction) |

### Right thumb drag — three cases

| Condition | Action |
|---|---|
| `newStartPadding > widthInDp − TRIM_THUMB_WIDTH` | Ignore (thumb can't go past end) |
| `newSeekTime > current` AND `newSeekTime − leftTime > maxTrimTime` | Accept drag, then **push left thumb forward** to `newSeekTime − maxTrimTime` |
| `newStartPadding ≥ leftThumb.paddingStart + TRIM_THUMB_WIDTH` | Accept drag normally |
| else | Ignore (would overlap left thumb) |

### Helper functions

`updateRightThumbUiState` — called when left thumb drag would exceed `maxTrimTime`:
```kotlin
newSeekTime = leftThumb.seekTime + maxTrimTime
newSeekTimeInPx = newSeekTime × videoDurationInPx / videoDurationInMs
paddingStartInDp = (newSeekTimeInPx - leftThumb.scrolledSeekbarInPx) / density + TRIM_THUMB_WIDTH
```

`updateLeftThumbUiState` — called when right thumb drag would exceed `maxTrimTime`:
```kotlin
newSeekTime = rightThumb.seekTime - maxTrimTime
newSeekTimeInPx = newSeekTime × videoDurationInPx / videoDurationInMs
paddingStartInDp = (newSeekTimeInPx - leftThumb.scrolledSeekbarInPx) / density
```

---

## Overlay Drawing (darkened regions)

Applied via `drawWithContent` on the `LazyRow`.

### Left overlay (frames before start trim)
```
overlayWidth = if scrolledInPx ≤ thumbWidthInPx:
                   leftThumb.paddingStartInDp.dp.toPx() − (thumbWidthInPx − scrolledInPx)
               else:
                   leftThumb.paddingStartInDp.dp.toPx()

drawRect from x = max(thumbWidthInPx − scrolledInPx, 0)
           width = overlayWidth + 4.dp.toPx()   ← slight overlap for clean join
```

### Right overlay (frames after end trim)
```
drawRect from x = rightThumb.paddingStartInDp.dp.toPx()
           width = size.width − paddingStart − visibleLastItemWidth
```
`lastItemOffset` tracks whether the right spacer (`SLIDE_RIGHT_SIDE_KEY`) is visible; if it is, its width is subtracted so the overlay doesn't draw over it.

---

## `drawSeekThumb` Modifier

A `Modifier.composed { }` extension that:

1. Returns `Modifier.alpha(0f)` if `paddingStartInDp < 0` (thumb is off-screen to the left).
2. Measures a time-label string (e.g. `"0:12"`) with `TextMeasurer`.
3. Applies `padding(start = paddingStartInDp.dp)` to position the thumb.
4. Attaches `pointerInput` → `detectHorizontalDragGestures`.
5. Sets `size(TRIM_THUMB_WIDTH × TRIM_SEEK_BAR_HEIGHT)`.
6. Draws a white rounded background (left corners for LEFT thumb, right corners for RIGHT thumb).
7. Draws the time label **above** the left thumb or **below** the right thumb via `drawText`.

---

## `SeekBarSide` Composable

A `Spacer` of size `TRIM_THUMB_WIDTH × TRIM_SEEK_BAR_HEIGHT` with three horizontal lines (drag-handle icon) drawn via `drawBehind`. Placed at both ends of the `LazyRow` so the frame strip can be scrolled to reveal the full video range underneath the thumbs.

---

## Theming / Dark Mode

- Overlay rects: `Color.DarkGray.copy(alpha = 0.8f)`
- Thumb background: `Color.White` (always)
- Thumb icon tint: `DarkColorScheme.primary` / `LightColorScheme.primary`
- Frame background (while loading): `DarkGanbaruColors.deselected` / `LightGanbaruColors.deselected`
- Seekbar border: `DarkGanbaruColors.divider` / `LightGanbaruColors.divider`
- Time label color: `DarkGanbaruColors.primaryTextColor` / `LightGanbaruColors.primaryTextColor`

---

## Inputs / Outputs

```kotlin
fun TrimVideoSeekBar(
    modifier: Modifier,
    screenPaddingValues: PaddingValues,   // used to subtract safe area from available width
    videoDurationInMilSecond: Long,       // total video length
    maxTrimTime: Float = 45_000f,         // max clip length in ms
    frameBitmaps: List<Bitmap>,           // pre-decoded frame thumbnails
    startSeekTime: Long,                  // initial left thumb position in ms
    endSeekTime: Long,                    // initial right thumb position in ms
    isDarkMode: Boolean,
    onStartTrimChanged: (Long) -> Unit,   // fires on every left thumb drag step
    onEndTrimChanged: (Long) -> Unit,     // fires on every right thumb drag step
)
```

The caller is responsible for:
- Decoding `frameBitmaps` (evenly spaced frames across the video).
- Holding `startSeekTime` / `endSeekTime` in a ViewModel and updating them in response to callbacks.
- Applying the seekbar's reported times to video playback / export.

---

## Porting Checklist

- [ ] Replace `GlideImage` with your image loading library (Coil, Picasso, etc.).
- [ ] Replace `APP_SF_PRO_TEXT_FONT_FAMILY` with your font family.
- [ ] Replace `DarkGanbaruColors` / `LightGanbaruColors` / `DarkColorScheme` / `LightColorScheme` with your theme tokens.
- [ ] Replace `AppTrimUtils.convertToVideoDuration(floatTime)` with your own time formatter (e.g. `mm:ss`).
- [ ] Replace `R.drawable.ic_left_arrow_with_circle_border` with your thumb icon (the right thumb re-uses the same icon, rotated 180°).
- [ ] Replace `R.` resource references with your own drawables.
- [ ] Provide frame bitmaps decoded at the correct size — frame width drives the entire pixel math.

---

## Risks & Performance Issues (current implementation)

Audit of [`app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt) and [`TrimVideoScreen.kt`](app/src/main/java/com/example/playvideo/ui/trimVideo/TrimVideoScreen.kt) against this doc. The implementation is a simpler, non-scrolling variant (plain `Row`, no `LazyRow`, no scroll tracking), so several items below diverge from the porting reference above.

### 🔴 Critical — drag gesture restarts mid-drag

[`TrimVideoSeekBar.kt:186`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:186) and [`:213`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:213):

```kotlin
.pointerInput(scale, containerWidthPx, rightThumbX) { ... }   // left thumb
.pointerInput(scale, containerWidthPx, leftThumbX)  { ... }   // right thumb
```

`leftThumbX` / `rightThumbX` change on **every** drag tick, so `pointerInput` is torn down and recreated mid-gesture. The active `detectDragGestures` coroutine cancels and a fresh one starts, which drops pointer events and produces stutter/jumps.

**Fix:** key on stable values only, and read the moving thumb position via `rememberUpdatedState`:

```kotlin
val leftXState  = rememberUpdatedState(leftThumbX)
val rightXState = rememberUpdatedState(rightThumbX)

.pointerInput(Unit) {
    detectDragGestures { _, drag ->
        if (scale <= 0f) return@detectDragGestures
        val newPx = (leftXState.value + drag.x)
            .coerceIn(0f, rightXState.value - thumbWidthPx)
        onStartTimeChange((newPx / scale).toLong())
    }
}
```

### 🔴 Critical — bitmap recycle race

[`TrimVideoScreen.kt:107-113`](app/src/main/java/com/example/playvideo/ui/trimVideo/TrimVideoScreen.kt:107):

```kotlin
DisposableEffect(videoUri) {
    onDispose { uiModel.frameBitmaps.forEach { it.recycle() } }
}
```

Two hazards:
1. The `onDispose` lambda captures `uiModel` once at dispose-time, but Compose may still hold the bitmaps in flight (Image in a recycled tree, in-flight draw). Recycling produces `Canvas: trying to use a recycled bitmap`.
2. If `videoUri` changes before `getFrameBitmaps` finishes, the LaunchedEffect can post freshly decoded bitmaps **after** the old ones have already been recycled — but if the user navigates back quickly the not-yet-decoded bitmaps from the prior run can also slip through.

**Fix:** capture the list explicitly so recycle happens against the snapshot at the time the effect was set up, and gate it on `Lifecycle.onStop` rather than `videoUri` change:

```kotlin
val toRecycle = uiModel.frameBitmaps
DisposableEffect(toRecycle) {
    onDispose { toRecycle.forEach { if (!it.isRecycled) it.recycle() } }
}
```

### 🟠 High — `bitmap.asImageBitmap()` allocated every recomposition

[`TrimVideoSeekBar.kt:144`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:144):

```kotlin
frameBitmaps.forEach { bitmap ->
    Image(bitmap = bitmap.asImageBitmap(), ...)
}
```

Each drag triggers a recomposition; each recomposition wraps every frame bitmap in a new `ImageBitmap`. With ~20 frames this is ~20 wrapper allocations per drag tick.

**Fix:** memoize once.

```kotlin
val imageBitmaps = remember(frameBitmaps) { frameBitmaps.map { it.asImageBitmap() } }
```

### 🟠 High — `maxTrimTime` not enforced

`MAX_ALLOWED_TRIM_TIME = 30_000L` is declared in [`AppVideoUtil.kt:12`](app/src/main/java/com/example/playvideo/util/AppVideoUtil.kt:12), but the param is commented out everywhere ([`TrimVideoSeekBar.kt:61`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:61), [`TrimVideoScreen.kt:167`](app/src/main/java/com/example/playvideo/ui/trimVideo/TrimVideoScreen.kt:167), [`TrimVideoUiModel.kt:12`](app/src/main/java/com/example/playvideo/ui/trimVideo/uiModel/TrimVideoUiModel.kt:12)). The user can drag thumbs to select an arbitrary range up to the full video duration, breaking the 30s contract that the screen initializes with.

**Fix:** restore the param and apply the "push the other thumb" logic from the *Drag Logic* section above. At minimum, clamp the new time so `endSeekTime - startSeekTime ≤ maxTrimTime`.

### 🟠 High — ExoPlayer hammered with one seek per pixel

[`TrimVideoScreen.kt:168-173`](app/src/main/java/com/example/playvideo/ui/trimVideo/TrimVideoScreen.kt:168) sets `seekTo = newStart` on every drag callback, and [`:115`](app/src/main/java/com/example/playvideo/ui/trimVideo/TrimVideoScreen.kt:115) immediately calls `player.seekTo(...)`. ExoPlayer seeks aren't free — at 60 drag ticks/sec this saturates the player and the UI lags behind the finger.

**Fix:** either debounce (e.g. `snapshotFlow + debounce(50.ms)`) or seek only on drag end via `onDragEnd` from `detectDragGestures`. Optionally fall back to lightweight UI-only preview during the drag and a real `seekTo` on release.

### 🟡 Medium — right timestamp label clips off-screen at the end

[`TrimVideoSeekBar.kt:241-255`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:241): the right label is positioned by `rightThumbX.toInt()` with no compensation for its own measured width, so as the right thumb approaches `containerWidth - thumbWidth` the trailing characters slide past the right edge.

**Fix:** measure the label (or use `onSizeChanged`) and offset by `rightThumbX - labelWidth + thumbWidth` clamped to the container.

### 🟡 Medium — first-frame jump from `containerWidthPx = 0`

`scale` is `0` until [`onGloballyPositioned`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:109) fires. On the first composition the right thumb is drawn at `0` and snaps to its real position one frame later. Cosmetic, but visible.

**Fix:** wrap thumb rendering in `if (containerWidthPx > 0f)` or use `SubcomposeLayout` / `BoxWithConstraints` so width is known synchronously.

### 🟡 Medium — drag callback fires even when value didn't change

After `coerceIn`, `newTime` can equal the previous value (thumb at boundary). The callback still propagates upward and triggers a recomposition + seek.

**Fix:** early-return if `newTime == startSeekTime` (or `endSeekTime`).

### 🟢 Low — minor inconsistencies

- Inconsistent thumb-bound math: left uses `containerWidthPx - 2*thumbWidthPx` while right uses `containerWidthPx - thumbWidthPx`. Functionally equivalent given the relative `coerceIn` against the other thumb, but a constant `maxThumbX` would be clearer.
- `frameBitmaps.forEach` inside a `Row` has no key — fine for now, but if frames ever become reorderable, switch to keyed items.
- `kotlin.collections.forEach` import on [`:48`](app/src/main/java/com/example/playvideo/ui/trimVideo/layout/TrimVideoSeekBar.kt:48) is redundant.

### Fix priority order

1. `pointerInput` keys (drag stability) — **immediate**
2. Bitmap recycle race (crash risk) — **immediate**
3. Memoize `asImageBitmap()` — quick win
4. Re-enable `maxTrimTime` — feature-correctness
5. Debounce player seeks — UX
6. Right-label clipping & first-frame jump — polish
