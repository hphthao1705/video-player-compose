package com.example.playvideo.ui.trimVideo

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playvideo.R
import com.example.playvideo.ui.trimVideo.layout.TrimDialog
import com.example.playvideo.ui.trimVideo.layout.TrimVideoPlayer
import com.example.playvideo.ui.trimVideo.layout.TrimVideoSeekBar
import com.example.playvideo.ui.trimVideo.layout.TrimVideoTopBar
import com.example.playvideo.ui.trimVideo.uiModel.TrimVideoUiModel
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoDialogState
import com.example.playvideo.util.VideoHelper.debugLog
import kotlinx.coroutines.launch

private val TrimColorBackground = Color(0xFF0D0D0D)

@Composable
fun TrimVideoScreen(
    videoUri: Uri,
    maxAllowedTrimTime: Long = 45_000L,
    onBack: () -> Unit,
) {
    val viewModel: TrimVideoViewModel = viewModel()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val title = stringResource(R.string.trim_video)
    val playbackError = stringResource(R.string.playback_error)


    var uiModel by remember {
        mutableStateOf(
            TrimVideoUiModel(
                title = title,
                videoUri = videoUri,
                maxAllowedTrimTime = maxAllowedTrimTime,
                endSeekTime = maxAllowedTrimTime,
            )
        )
    }
    var dialogState by remember { mutableStateOf<TrimVideoDialogState>(TrimVideoDialogState.StandBy) }
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    LaunchedEffect(videoUri) {
        // prepare for player first (faster)
        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()

        // get bitmaps
        val bitmaps = viewModel.getFrameBitmaps(context, videoUri)
        uiModel = uiModel.copy(frameBitmaps = bitmaps)
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.duration > 0) {
                    uiModel = uiModel.copy(
                        isReadyToTrim = true,
                        videoDurationInMilSecond = player.duration,
                        endSeekTime = minOf(maxAllowedTrimTime, player.duration),
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                dialogState = TrimVideoDialogState.Error(
                    title = playbackError,
                    message = error.message ?: "An unknown error occurred.",
                )
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            "DisposableEffect videoUri".debugLog()
            // when user exit the screen or change video > clear bitmaps memory (RAM)
            uiModel.frameBitmaps.forEach { bitmap -> bitmap.recycle() }
        }
    }

    LaunchedEffect(uiModel.seekTo) {
        val seekTo = uiModel.seekTo
        if (seekTo >= 0L) {
            player.seekTo(seekTo)
            uiModel = uiModel.copy(seekTo = -1L)
        }
    }

    LaunchedEffect(uiModel.isStopPlayVideo) {
        if (uiModel.isStopPlayVideo) {
            player.stop()
            uiModel = uiModel.copy(isStopPlayVideo = false)
        }
    }

    Scaffold(containerColor = TrimColorBackground) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TrimColorBackground),
            ) {
                TrimVideoTopBar(
                    title = uiModel.title,
                    isReadyToTrim = uiModel.isReadyToTrim,
                    onBackClick = onBack,
                    onInfoClick = { dialogState = TrimVideoDialogState.Information },
                    onTrimClick = { dialogState = TrimVideoDialogState.AskSelectOptionToTrimVideo },
                )

                Spacer(Modifier.height(16.dp))

                TrimVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    player = player,
                    isReady = uiModel.isReadyToTrim,
                )

                TrimVideoSeekBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp),
                    frameBitmaps = uiModel.frameBitmaps,
                    startSeekTime = uiModel.startSeekTime,
                    endSeekTime = uiModel.endSeekTime,
                    videoDuration = uiModel.videoDurationInMilSecond,
                    maxTrimTime = uiModel.maxAllowedTrimTime,
                    onStartTimeChange = { newStart ->
                        uiModel = uiModel.copy(startSeekTime = newStart, seekTo = newStart)
                    },
                    onEndTimeChange = { newEnd ->
                        uiModel = uiModel.copy(endSeekTime = newEnd, seekTo = newEnd)
                    },
                )
            }

            TrimDialog(
                state = dialogState,
                videoUri = uiModel.videoUri,
                onDismiss = { dialogState = TrimVideoDialogState.StandBy },
                onTrimConfirm = { option ->
                    dialogState = TrimVideoDialogState.Loading
                    scope.launch {
                        // TODO: implement trim via Media3 Transformer
                        dialogState = TrimVideoDialogState.StandBy
                    }
                },
            )
        }
    }
}