package com.example.playvideo.ui.trimVideo.layout

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playvideo.util.AppDimension.DIMENSION_20
import com.example.playvideo.util.AppDimension.DIMENSION_24
import com.example.playvideo.util.AppDimension.DIMENSION_8
import com.example.playvideo.util.AppVideoUtil.MAX_ALLOWED_TRIM_TIME
import com.example.playvideo.util.MathHelper.toTimestamp
import com.example.playvideo.util.shimmerLoading

private val TrimColorBackground = Color(0xFF0D0D0D)
private val TrimColorDivider = Color(0xFF333333)
private val TrimColorText = Color.White

@Composable
fun TrimVideoSeekBar(
    modifier: Modifier = Modifier,
    frameBitmaps: List<Bitmap>?,        // null = still loading; emptyList = loaded but no frames
    startSeekTime: Long,
    endSeekTime: Long,
    videoDuration: Long,
    maxTrimTime: Long = MAX_ALLOWED_TRIM_TIME,
    onStartTimeChange: (Long) -> Unit,
    onEndTimeChange: (Long) -> Unit,
) {
    val seekBarHeight = 64.dp
    val labelHeight = DIMENSION_20

    // ── Still loading — show shimmer skeleton ────────────────────────────────
    if (frameBitmaps == null) {
        Box(
            modifier = modifier
                .padding(horizontal = DIMENSION_24)
                .height(labelHeight + 4.dp + seekBarHeight + 4.dp + labelHeight),
        ) {
            // Top timestamp label shimmer
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(labelHeight)
                    .align(Alignment.TopStart)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerLoading(),
            )
            // Seek bar shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(seekBarHeight)
                    .align(Alignment.TopStart)
                    .offset(y = labelHeight + 4.dp)
                    .clip(RoundedCornerShape(DIMENSION_8))
                    .shimmerLoading(),
            )
            // Bottom timestamp label shimmer
            Box(
                modifier = Modifier
                    .width(DIMENSION_36)
                    .height(labelHeight)
                    .align(Alignment.TopEnd)
                    .offset(y = labelHeight + 4.dp + seekBarHeight + 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerLoading(),
            )
        }
        return
    }

    // ── Loaded but no frames extracted ───────────────────────────────────────
    if (frameBitmaps.isEmpty()) {
        Box(
            modifier = modifier
                .padding(horizontal = DIMENSION_24)
                .height(labelHeight + 4.dp + seekBarHeight),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(seekBarHeight)
                    .align(Alignment.BottomStart)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TrimColorDivider),
            )
        }
        return
    }

    // ── Normal render ─────────────────────────────────────────────────────────
    val density = LocalDensity.current
    val thumbWidth = 20.dp
    val thumbWidthPx = with(density) { thumbWidth.toPx() }

    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    var rightLabelWidth by remember { mutableIntStateOf(0) }

    // Memoize ImageBitmap wrappers — created once per unique bitmap list, not per recomposition
    val imageBitmaps = remember(frameBitmaps) { frameBitmaps.map { it.asImageBitmap() } }

    val scale = if (videoDuration > 0 && containerWidthPx > thumbWidthPx)
        (containerWidthPx - thumbWidthPx) / videoDuration.toFloat()
    else 0f

    val leftThumbX = if (scale > 0f)
        (startSeekTime * scale).coerceIn(0f, containerWidthPx - 2f * thumbWidthPx)
    else 0f

    val rightThumbX = if (scale > 0f)
        (endSeekTime * scale).coerceIn(thumbWidthPx, containerWidthPx - thumbWidthPx)
    else (containerWidthPx - thumbWidthPx).coerceAtLeast(0f)

    // Stable state refs so pointerInput(Unit) always reads the latest values without restarting
    val leftXState = rememberUpdatedState(leftThumbX)
    val rightXState = rememberUpdatedState(rightThumbX)
    val scaleState = rememberUpdatedState(scale)
    val startSeekTimeState = rememberUpdatedState(startSeekTime)
    val endSeekTimeState = rememberUpdatedState(endSeekTime)
    val videoDurationState = rememberUpdatedState(videoDuration)
    val maxTrimTimeState = rememberUpdatedState(maxTrimTime)

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .onGloballyPositioned { containerWidthPx = it.size.width.toFloat() },
    ) {
        // Left timestamp label (above seek bar)
        Text(
            text = startSeekTime.toTimestamp(),
            color = TrimColorText,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.2.sp,
            modifier = Modifier
                .offset { IntOffset(leftThumbX.toInt(), 0) }
                .align(Alignment.TopStart),
        )

        // Seek bar area (frames + overlays + thumbs)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(seekBarHeight)
                .align(Alignment.TopStart)
                .offset(y = labelHeight + 4.dp),
        ) {
            // Frame thumbnails row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, TrimColorDivider, RoundedCornerShape(8.dp))
                    .background(Color.White),
            ) {
                SeekBarSide(
                    modifier = Modifier
                        .width(thumbWidth)
                        .fillMaxHeight()
                )
                imageBitmaps.forEach { imageBitmap ->
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
                SeekBarSide(
                    modifier = Modifier
                        .width(thumbWidth)
                        .fillMaxHeight()
                )
            }

            // Skip overlays and thumbs until container size is known to avoid a first-frame jump
            if (containerWidthPx > 0f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (leftThumbX > 0f) {
                        drawRect(
                            color = Color.DarkGray.copy(alpha = 0.8f),
                            topLeft = Offset.Zero,
                            size = Size(leftThumbX, size.height),
                        )
                    }
                    val rightEnd = rightThumbX + thumbWidthPx
                    if (rightEnd < size.width) {
                        drawRect(
                            color = Color.DarkGray.copy(alpha = 0.8f),
                            topLeft = Offset(rightEnd, 0f),
                            size = Size(size.width - rightEnd, size.height),
                        )
                    }
                }

                // Left thumb
                Box(
                    modifier = Modifier
                        .offset { IntOffset(leftThumbX.toInt(), 0) }
                        .width(thumbWidth)
                        .height(seekBarHeight)
                        .background(
                            Color.White,
                            RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        )
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                val s = scaleState.value
                                if (s <= 0f) return@detectDragGestures
                                val dur = videoDurationState.value
                                val curStart = startSeekTimeState.value
                                val curEnd = endSeekTimeState.value
                                val window = (curEnd - curStart).coerceAtMost(dur)
                                val maxStart = (dur - window).coerceAtLeast(0L)
                                val deltaTime = (dragAmount.x / s).toLong()
                                val newStart = (curStart + deltaTime).coerceIn(0L, maxStart)
                                val newEnd = (newStart + window).coerceAtMost(dur)
                                if (newStart != curStart) onStartTimeChange(newStart)
                                if (newEnd != curEnd) onEndTimeChange(newEnd)
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = TrimColorBackground,
                        modifier = Modifier.size(16.dp),
                    )
                }

                // Right thumb
                Box(
                    modifier = Modifier
                        .offset { IntOffset(rightThumbX.toInt(), 0) }
                        .width(thumbWidth)
                        .height(seekBarHeight)
                        .background(
                            Color.White,
                            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        )
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                val s = scaleState.value
                                if (s <= 0f) return@detectDragGestures
                                val start = startSeekTimeState.value
                                val dur = videoDurationState.value
                                val newPx = (rightXState.value + dragAmount.x)
                                    .coerceIn(
                                        leftXState.value + thumbWidthPx,
                                        containerWidthPx - thumbWidthPx,
                                    )
                                val newTime = (newPx / s).toLong().coerceIn(
                                    (start + 1L).coerceAtMost(dur),
                                    minOf(dur, start + maxTrimTimeState.value),
                                )
                                if (newTime != endSeekTimeState.value) {
                                    onEndTimeChange(newTime)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TrimColorBackground,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Right timestamp label (below seek bar), clamped so it never overflows the container
        Text(
            text = endSeekTime.toTimestamp(),
            color = TrimColorText,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            letterSpacing = 0.2.sp,
            modifier = Modifier
                .onSizeChanged { rightLabelWidth = it.width }
                .offset {
                    val clampedX = rightThumbX.toInt()
                        .coerceAtMost(
                            (containerWidthPx - rightLabelWidth).toInt().coerceAtLeast(0)
                        )
                    IntOffset(
                        clampedX,
                        with(density) { (labelHeight + 4.dp + seekBarHeight + 4.dp).toPx() }.toInt(),
                    )
                }
                .align(Alignment.TopStart),
        )
    }
}

@Composable
private fun SeekBarSide(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .height(2.dp)
                        .background(Color.DarkGray),
                )
            }
        }
    }
}
