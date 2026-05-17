package com.example.playvideo.ui.trimVideo.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.playvideo.util.AppDimension.DIMENSION_16
import com.example.playvideo.util.AppDimension.DIMENSION_8
import com.example.playvideo.util.shimmerLoading

@Composable
fun TrimVideoPlayer(
    modifier: Modifier = Modifier,
    player: ExoPlayer,
    isReady: Boolean,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        if (!isReady) {
            // shimmer skeleton while the player/metadata is not yet ready
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DIMENSION_16)
                    .clip(RoundedCornerShape(DIMENSION_8))
                    .shimmerLoading(),
            )
        } else {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = DIMENSION_16),
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false
                        this.player = player
                    }
                },
                update = { view -> view.player = player },
            )
        }
    }
}
