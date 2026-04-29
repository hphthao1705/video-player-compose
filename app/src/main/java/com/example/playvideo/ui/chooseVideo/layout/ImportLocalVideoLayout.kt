package com.example.playvideo.ui.chooseVideo.layout

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.playvideo.R
import com.example.playvideo.VideoPreviewViewModel

@Composable
fun ImportLocalVideoSection() {
    val viewModel: VideoPreviewViewModel = hiltViewModel()
    val pickVideoLauncher: ManagedActivityResultLauncher<String, Uri?> = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.previewLocalVideo(uri)
    }

    Text(
        text = stringResource(R.string.all_built_in_videos_from_videos),
        color = Color(0xFF8EE8A0),
        style = MaterialTheme.typography.bodySmall,
    )

    Spacer(Modifier.height(12.dp))

    ImportLocalVideoSection(
        onImportLocalVideo = { pickVideoLauncher.launch("video/*") },
    )
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
