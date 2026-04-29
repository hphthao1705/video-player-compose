package com.example.playvideo.ui.chooseVideo

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.playvideo.VideoPreviewViewModel
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.ui.chooseVideo.layout.AvailableVideosLoading
import com.example.playvideo.ui.chooseVideo.layout.AvailableVideosSection
import com.example.playvideo.ui.chooseVideo.layout.ChooseVideoHeaderSection
import com.example.playvideo.ui.chooseVideo.layout.ImportLocalVideoSection
import com.example.playvideo.ui.chooseVideo.layout.PreviewSection
import com.example.playvideo.ui.chooseVideo.layout.StartTrimVideoSection
import com.example.playvideo.ui.chooseVideo.uiState.ChooseVideoUiState
import com.example.playvideo.util.VideoHelper.debugLog

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

    val pickVideoLauncher: ManagedActivityResultLauncher<String, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(16.dp),
    ) {
        ChooseVideoHeaderSection(onBack = onBack)

        Spacer(Modifier.height(8.dp))

        ImportLocalVideoSection(pickVideoLauncher = pickVideoLauncher)

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
        )
    }
}

@Composable
private fun ChooseAvailableVideosSection(
    selected: VideoInfoData?,
    onSelectVideo: (VideoInfoData) -> Unit,
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
