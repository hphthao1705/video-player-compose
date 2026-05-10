package com.example.playvideo.ui.chooseVideo

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playvideo.R
import com.example.playvideo.VideoPreviewViewModel
import com.example.playvideo.data.AvailableVideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.ui.chooseVideo.layout.AvailableVideosLoading
import com.example.playvideo.ui.chooseVideo.layout.AvailableVideosSection
import com.example.playvideo.ui.chooseVideo.layout.ChooseVideoHeaderSection
import com.example.playvideo.ui.chooseVideo.layout.ImportLocalVideoSection
import com.example.playvideo.ui.chooseVideo.layout.PreviewSection
import com.example.playvideo.ui.chooseVideo.layout.StartTrimVideoSection
import com.example.playvideo.ui.chooseVideo.uiState.ChooseVideoUiState
import com.example.playvideo.util.VideoHelper.debugLog
import kotlinx.coroutines.launch

@Composable
fun ChooseVideoScreen(
    onBack: () -> Unit,
    onStartTrim: (Uri) -> Unit,
) {
    val viewModel: VideoPreviewViewModel = hiltViewModel()
    val selectedVideo by viewModel.selectedVideo.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val player: ExoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    LaunchedEffect(selectedVideo?.url) {
        val url = selectedVideo?.url ?: return@LaunchedEffect
        "VideoPlayer: preparing url=$url".debugLog()
        player.setMediaItem(MediaItem.fromUri(url.toUri()))
        player.prepare()
        player.pause()
        player.seekTo(0L)
        "VideoPlayer: prepared and paused".debugLog()
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val noVideoMessage = stringResource(R.string.please_select_a_video_first)

    Scaffold(
        containerColor = Color(0xFF0D0D0D),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF323232),
                    contentColor = Color.White,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            ChooseVideoHeaderSection(onBack = onBack)

            Spacer(Modifier.height(8.dp))

            ImportLocalVideoSection()

            Spacer(Modifier.height(12.dp))

            ChooseAvailableVideosSection(
                selected = selectedVideo,
                onSelectVideo = viewModel::selectVideo,
            )

            Spacer(Modifier.height(12.dp))

            key(selectedVideo?.url) {
                PreviewSection(
                    player = player,
                    previewBitmap = selectedVideo?.previewBitmap,
                )
            }

            Spacer(Modifier.weight(1f))

            StartTrimVideoSection(
                selected = selectedVideo,
                onStartTrim = onStartTrim,
                onNoVideoSelected = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(noVideoMessage)
                    }
                },
            )
        }
    }
}

@Composable
private fun ChooseAvailableVideosSection(
    selected: AvailableVideoInfoData?,
    onSelectVideo: (AvailableVideoInfoData) -> Unit,
) {
    val viewModel: VideoPreviewViewModel = hiltViewModel()
    val availableVideos by viewModel.availableVideosUiState.collectAsStateWithLifecycle()

    when (availableVideos) {
        is ChooseVideoUiState.Loading -> {
            AvailableVideosLoading(videos.size)
        }

        is ChooseVideoUiState.Success -> {
            AvailableVideosSection(
                videos = (availableVideos as ChooseVideoUiState.Success).videos,
                selectedVideo = selected,
                onSelectVideo = onSelectVideo,
            )
        }

        else -> Unit
    }
}
