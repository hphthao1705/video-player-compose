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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
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
import com.example.playvideo.VideoOption
import com.example.playvideo.VideoViewModel
import com.example.playvideo.ui.trimVideo.layout.TrimDialog
import com.example.playvideo.ui.trimVideo.layout.TrimVideoPlayer
import com.example.playvideo.ui.trimVideo.layout.TrimVideoSeekBar
import com.example.playvideo.ui.trimVideo.layout.TrimVideoTopBar
import com.example.playvideo.ui.trimVideo.uiState.TrimResultUiState
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoDialogState
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoOption
import com.example.playvideo.util.AppVideoUtil.MAX_ALLOWED_TRIM_TIME
import com.example.playvideo.util.MathHelper.orZero
import com.example.playvideo.util.VideoHelper.debugLog

private val TrimColorBackground = Color(0xFF0D0D0D)

@Composable
fun TrimVideoScreen(
    mode: VideoOption = VideoOption.Compress,
    onBack: () -> Unit,
    onTrimSuccess: (Uri) -> Unit = {},
) {
    val viewModel: TrimVideoViewModel = viewModel()
    val videoViewModel: VideoViewModel = viewModel()
    val context = LocalContext.current
    val playbackError = stringResource(R.string.playback_error)
    val trimErrorTitle = stringResource(R.string.trim_video)
    val trimResultState by viewModel.trimResultState.collectAsState()

    val selectedVideo = videoViewModel.selectedVideo.collectAsState().value

    var dialogState by remember { mutableStateOf<TrimVideoDialogState>(TrimVideoDialogState.StandBy) }
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply { playWhenReady = true }
    }

    LaunchedEffect(selectedVideo) {
        selectedVideo?.uri?.let { uri ->
            // prepare for player first (faster)
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()

            // get bitmaps
            val bitmaps = viewModel.getFrameBitmaps(context, uri)
            videoViewModel.updateSelectedVideo { currentVideo ->
                currentVideo.copy(previewBitmaps = bitmaps)
            }
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && player.duration > 0) {
                    videoViewModel.updateSelectedVideo { currentVideo ->
                        currentVideo.copy(
                            isReadyToTrim = true,
                            duration = player.duration,
                            endTrimMs = minOf(MAX_ALLOWED_TRIM_TIME, player.duration),
                        )
                    }
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

    // Debounce seeks so ExoPlayer isn't called on every drag pixel (~60/sec).
    LaunchedEffect(Unit) {
        snapshotFlow { selectedVideo?.seekTo.orZero() }
            .filter { it >= 0L }
            .debounce(80L)
            .collect { seekTo ->
                player.seekTo(seekTo)
                videoViewModel.updateSelectedVideo { currentVideo ->
                    currentVideo.copy(seekTo = -1L)
                }
            }
    }

    LaunchedEffect(selectedVideo?.isStopPlayVideo) {
        if (selectedVideo?.isStopPlayVideo == true) {
            player.stop()
            videoViewModel.updateSelectedVideo { currentVideo ->
                currentVideo.copy(isStopPlayVideo = false)
            }
        }
    }

    LaunchedEffect(trimResultState) {
        when (val state = trimResultState) {
            is TrimResultUiState.Loading -> {
                dialogState = TrimVideoDialogState.Loading(state.progress)
            }
            is TrimResultUiState.Success -> {
                dialogState = TrimVideoDialogState.StandBy
                viewModel.resetTrimResult()
                onTrimSuccess(state.uri)
            }
            is TrimResultUiState.Error -> {
                dialogState = TrimVideoDialogState.Error(
                    title = trimErrorTitle,
                    message = state.message,
                )
                viewModel.resetTrimResult()
            }
            TrimResultUiState.StandBy -> Unit
        }
    }

    val performTrim: (TrimVideoOption) -> Unit = { option ->
        when (option) {
            TrimVideoOption.TrimExactly -> viewModel.trimVideo(
                context = context,
                startMs = selectedVideo?.startTrimMs.orZero(),
                endMs = selectedVideo?.endTrimMs.orZero(),
                inputUri = selectedVideo?.uri!!,
            )
            is TrimVideoOption.TrimInexactly -> viewModel.trimVideo(
                context = context,
                startMs = option.nearestBeforeKeyFrame,
                endMs = option.nearestAfterKeyFrame,
                inputUri = selectedVideo?.uri!!,
            )
            TrimVideoOption.TrimAndCompress -> viewModel.compressVideo(
                context = context,
                startMs = selectedVideo?.startTrimMs.orZero(),
                endMs = selectedVideo?.endTrimMs.orZero(),
                inputUri = selectedVideo?.uri!!
            )
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
                    isReadyToTrim = selectedVideo?.isReadyToTrim == true,
                    onBackClick = onBack,
                    onInfoClick = { dialogState = TrimVideoDialogState.Information },
                    onTrimClick = {
                        when (mode) {
                            VideoOption.Trim -> {
                                if (selectedVideo?.uri == null) return@TrimVideoTopBar
                                else {
                                    viewModel.trimVideo(
                                        context = context,
                                        startMs = selectedVideo.startTrimMs.orZero(),
                                        endMs = selectedVideo.endTrimMs.orZero(),
                                        inputUri = selectedVideo.uri
                                    )
                                }
                            }
                            VideoOption.Compress ->
                                dialogState = TrimVideoDialogState.AskSelectOptionToTrimVideo

                           else -> Unit
                        }
                    },
                )

                Spacer(Modifier.height(16.dp))

                TrimVideoPlayer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    player = player,
                    isReady = selectedVideo?.isReadyToTrim == true,
                )

                TrimVideoSeekBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp),
                    frameBitmaps = selectedVideo?.previewBitmaps ?: emptyList(),
                    startSeekTime = selectedVideo?.startTrimMs.orZero(),
                    endSeekTime = selectedVideo?.endTrimMs.orZero(),
                    videoDuration = selectedVideo?.duration.orZero(),
                    onStartTimeChange = { newStart ->
                        videoViewModel.updateSelectedVideo { currentVideo ->
                            currentVideo.copy(startTrimMs = newStart, seekTo = newStart)
                        }
                    },
                    onEndTimeChange = { newEnd ->
                        videoViewModel.updateSelectedVideo { currentVideo ->
                            currentVideo.copy(endTrimMs = newEnd, seekTo = newEnd)
                        }
                    },
                )
            }

            TrimDialog(
                state = dialogState,
                selectedVideoData = selectedVideo,
                onDismiss = { dialogState = TrimVideoDialogState.StandBy },
                onTrimConfirm = performTrim,
            )
        }
    }
}
