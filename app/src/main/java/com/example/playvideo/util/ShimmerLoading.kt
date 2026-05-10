package com.example.playvideo.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val ShimmerBase      = Color(0xFF2A2A2A)   // dark resting colour
private val ShimmerHighlight = Color(0xFF4A4A4A)   // lighter sweep band

fun Modifier.shimmerLoading(
    durationMillis: Int = 1200,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")

    // Animate from -1f to 2f so the highlight band fully enters and exits
    // the composable regardless of its width.
    val translateX by transition.animateFloat(
        initialValue = -1f,
        targetValue  =  2f,
        animationSpec = infiniteRepeatable(
            animation  = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslateX",
    )

    drawWithContent {
        // Fill with the base colour first so the composable's own content
        // is replaced by the skeleton (not drawn underneath the shimmer).
        drawRect(color = ShimmerBase)

        // Sweep band: width is 60% of the composable so it looks like a
        // moving light source rather than a hard edge.
        val bandWidth = size.width * 0.6f
        val startX   = translateX * size.width - bandWidth / 2f

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    ShimmerHighlight,
                    Color.Transparent,
                ),
                start = Offset(startX, 0f),
                end   = Offset(startX + bandWidth, 0f),
            ),
        )
    }
}
