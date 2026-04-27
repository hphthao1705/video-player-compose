package com.example.playvideo.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.playvideo.data.videos

private data class VideoOption(
    val label: String,
    val uri: Uri,
)

@Composable
fun TrimChooseVideoScreen(
    onBack: () -> Unit,
    onStartTrim: (Uri) -> Unit,
) {
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
            .take(5)
            .mapIndexed { index, url ->
                VideoOption(
                    label = "Sample video ${index + 1}",
                    uri = Uri.parse(url),
                )
            }
    }

    var selected by remember { mutableStateOf<VideoOption?>(builtInVideoOptions.firstOrNull()) }
    var isPlaying by remember { mutableStateOf(false) }

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
        player.setMediaItem(MediaItem.fromUri(selectedUri))
        player.prepare()
        player.pause()
        player.seekTo(0L)
        isPlaying = false
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Choose Video for Trim",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Back",
                color = Color(0xFFB3B3B3),
                modifier = Modifier.clickable(onClick = onBack),
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "All built-in videos from videos.kt are still working.",
            color = Color(0xFF8EE8A0),
            style = MaterialTheme.typography.bodySmall,
        )

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { pickVideoLauncher.launch("video/*") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(text = "Import local video")
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Or choose 1 in 5 built-in videos:",
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
                        .clickable { selected = option },
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

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Preview (tap video to play/pause):",
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
                .clickable {
                    if (player.isPlaying) {
                        player.pause()
                        isPlaying = false
                    } else {
                        player.play()
                        isPlaying = true
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
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

            if (!isPlaying) {
                Text(
                    text = "Tap to play",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0x88000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))

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
                text = "Start to trim your video",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun isLikelyVideoSource(source: String): Boolean {
    val lower = source.substringBefore('?').lowercase()
    return lower.endsWith(".mp4") ||
        lower.endsWith(".webm") ||
        lower.endsWith(".m3u8") ||
        lower.endsWith(".mov") ||
        lower.endsWith(".mkv")
}
