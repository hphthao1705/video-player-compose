package com.example.playvideo.ui.chooseVideo.layout

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.playvideo.R
import com.example.playvideo.util.AppDimension.DIMENSION_16
import com.example.playvideo.util.AppDimension.DIMENSION_3
import com.example.playvideo.util.VideoHelper.debugLog

@Composable
fun PreviewSection(
    player: ExoPlayer,
    previewBitmap: Bitmap?,
) {
    var isPlaying by remember { mutableStateOf(false) }
    var hasStartedPlayback by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                "VideoPlayer: onIsPlayingChanged=$isPlayingNow".debugLog()
                isPlaying = isPlayingNow
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN($playbackState)"
                }
                "VideoPlayer: onPlaybackStateChanged=$stateName playWhenReady=${player.playWhenReady}".debugLog()
                isPlayerReady = playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED
                isBuffering = playbackState == Player.STATE_BUFFERING && player.playWhenReady
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                "VideoPlayer: onPlayWhenReadyChanged=$playWhenReady reason=$reason".debugLog()
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    Text(
        text = stringResource(R.string.preview),
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(DIMENSION_16))
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        // Video player (shown once playback has started)
        if (hasStartedPlayback) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { viewContext ->
                    PlayerView(viewContext).apply {
                        useController = false
                        this.player = player
                    }
                },
                update = { playerView ->
                    playerView.player = player
                },
            )
        }

        // Static thumbnail before playback starts
        if (!hasStartedPlayback && previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Loading spinner — shown only after a video is selected and player is still preparing
        if (previewBitmap != null && !isPlayerReady) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = DIMENSION_3,
            )
        }

        // Buffering spinner — shown while playing and buffering mid-stream
        if (isPlayerReady && isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.White,
                strokeWidth = DIMENSION_3,
            )
        }

        // "Tap to play" hint — only shown when ready and not playing
        if (isPlayerReady && !isPlaying && !isBuffering) {
            Text(
                text = stringResource(R.string.tap_to_play),
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        // Tap overlay — only active once player is ready
        if (isPlayerReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        val playWhenReady = player.playWhenReady
                        val playbackState = player.playbackState
                        "VideoPlayer: tap — playWhenReady=$playWhenReady playbackState=$playbackState isPlaying=$isPlaying".debugLog()
                        if (playWhenReady && playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                            player.pause()
                            isPlaying = false
                        } else {
                            player.play()
                            hasStartedPlayback = true
                            isBuffering = player.playbackState == Player.STATE_BUFFERING
                        }
                    },
            )
        }
    }
}
