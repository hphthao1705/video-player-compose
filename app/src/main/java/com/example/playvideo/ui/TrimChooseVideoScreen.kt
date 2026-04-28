package com.example.playvideo.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.util.VideoHelper.isLikelyVideoSource
import com.example.playvideo.R
import androidx.core.net.toUri
import com.example.playvideo.util.VideoHelper.debugLog
import com.example.playvideo.util.VideoHelper.loadPreviewBitmap

private data class VideoOption(
    val label: String,
    val uri: Uri,
)

@Composable
fun TrimChooseVideoScreen(
    onBack: () -> Unit,
    availableVideos: List<VideoInfoData> = emptyList(),
    onPreviewLoaded: (Uri, Bitmap?) -> Unit = { _, _ -> },
    onStartTrim: (Uri) -> Unit,
) {
    "availableVideos: $availableVideos".debugLog()
    val context = LocalContext.current
    val player = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    val builtInVideoOptions = remember {
        videos
            .filter(::isLikelyVideoSource)
            
            .mapIndexed { index, url ->
                VideoOption(
                    label = "Sample video ${index + 1}",
                    uri = url.toUri(),
                )
            }
    }

    var selected by remember { mutableStateOf<VideoOption?>(builtInVideoOptions.firstOrNull()) }
    var isPlaying by remember { mutableStateOf(false) }
    var hasStartedPlayback by remember { mutableStateOf(false) }

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        selected = VideoOption(
            label = "Local imported video",
            uri = uri,
        )
    }

    LaunchedEffect(selected?.uri) {
        val selectedUri = selected?.uri ?: return@LaunchedEffect
        // Ensure selected source URL is tracked for playback/preview cache.
        onPreviewLoaded(selectedUri, null)
        player.setMediaItem(MediaItem.fromUri(selectedUri))
        player.prepare()
        player.pause()
        player.seekTo(0L)
        isPlaying = false
        hasStartedPlayback = false
    }

    val previewBitmap by produceState<Bitmap?>(initialValue = null, key1 = selected?.uri, key2 = availableVideos) {
        val selectedUri = selected?.uri
        "selectedUri: $selectedUri".debugLog()
        value = if (selectedUri == null) {
            null
        } else {
            val cachedBitmap = availableVideos.firstOrNull { it.url == selectedUri.toString() }?.previewBitmap
            var bitmap = cachedBitmap ?: loadPreviewBitmap(context, selectedUri)
            if (bitmap == null) {
                // Retry once if first frame extraction returns null.
                bitmap = loadPreviewBitmap(context, selectedUri)
            }
            if (cachedBitmap !== bitmap) {
                onPreviewLoaded(selectedUri, bitmap)
            }
            "bitmap: $bitmap".debugLog()
            bitmap
        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "< ${stringResource(R.string.back)}",
                color = Color(0xFFB3B3B3),
                modifier = Modifier.clickable(onClick = onBack),
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(R.string.choose_video_for_trim),
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.all_built_in_videos_from_videos),
            color = Color(0xFF8EE8A0),
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(12.dp))

        ImportLocalVideoSection(
            onImportLocalVideo = { pickVideoLauncher.launch("video/*") },
        )

        Spacer(Modifier.height(12.dp))

        ChooseAvailableVideosSection(
            builtInVideoOptions = builtInVideoOptions,
            selected = selected,
            onSelectVideo = { option -> selected = option },
        )

        Spacer(Modifier.height(12.dp))

        PreviewSection(
            player = player,
            previewBitmap = previewBitmap,
            isPlaying = isPlaying,
            hasStartedPlayback = hasStartedPlayback,
            onTogglePlayPause = {
                if (player.isPlaying) {
                    player.pause()
                    isPlaying = false
                } else {
                    player.play()
                    isPlaying = true
                    hasStartedPlayback = true
                }
            },
        )

        Spacer(Modifier.weight(1f))

        StartTrimVideoSection(
            selected = selected,
            onStartTrim = onStartTrim,
        )
    }
}

@Composable
private fun ImportLocalVideoSection(
    onImportLocalVideo: () -> Unit,
) {
    OutlinedButton(
        onClick = onImportLocalVideo,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text = stringResource(R.string.import_local_video))
    }
}

@Composable
private fun ChooseAvailableVideosSection(
    builtInVideoOptions: List<VideoOption>,
    selected: VideoOption?,
    onSelectVideo: (VideoOption) -> Unit,
) {
    Text(
        text = stringResource(R.string.or_choose_1_in_5_built_in_videos),
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(Modifier.height(8.dp))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(builtInVideoOptions) { _, option ->
            val isSelected = selected?.uri == option.uri
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVideo(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF24452B) else Color(0xFF1A1A1A)
                ),
            ) {
                Text(
                    text = option.label,
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
private fun PreviewSection(
    player: ExoPlayer,
    previewBitmap: Bitmap?,
    isPlaying: Boolean,
    hasStartedPlayback: Boolean,
    onTogglePlayPause: () -> Unit,
) {
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black)
            .clickable(onClick = onTogglePlayPause),
        contentAlignment = Alignment.Center,
    ) {
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

        if (!hasStartedPlayback && previewBitmap != null) {
            Image(
                bitmap = previewBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        if (!isPlaying) {
            Text(
                text = stringResource(R.string.tap_to_play),
                color = Color.White,
                modifier = Modifier
                    .background(Color(0x88000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun StartTrimVideoSection(
    selected: VideoOption?,
    onStartTrim: (Uri) -> Unit,
) {
    Button(
        onClick = {
            val selectedUri = selected?.uri ?: return@Button
            onStartTrim(selectedUri)
        },
        enabled = selected != null,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF9A825),
            contentColor = Color.Black,
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = stringResource(R.string.start_to_trim_your_video),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
