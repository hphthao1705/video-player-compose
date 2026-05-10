package com.example.playvideo.ui.trimVideo.layout

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.playvideo.R
import com.example.playvideo.ui.trimVideo.TrimVideoViewModel
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoDialogState
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoOption
import com.example.playvideo.ui.trimVideo.uiState.VideoNameUiState
import com.example.playvideo.util.AppDimension.DIMENSION_4
import com.example.playvideo.util.MathHelper.orZero
import com.example.playvideo.util.MathHelper.toTimestamp
import com.example.playvideo.util.shimmerLoading

private val TrimColorPrimary = Color(0xFFF9A825)
private val TrimColorError = Color(0xFFFF5252)
private val TrimColorDialogBg = Color(0xFF1A1A1A)
private val TrimColorDeselected = Color(0xFF666666)
private val TrimColorText = Color.White

@Composable
fun TrimDialog(
    state: TrimVideoDialogState,
    videoUri: Uri?,
    onDismiss: () -> Unit,
    onTrimConfirm: (TrimVideoOption) -> Unit,
) {
    when (state) {
        TrimVideoDialogState.StandBy -> Unit

        TrimVideoDialogState.Loading -> {
            DialogWrapper(onDismissRequest = {}) {
                AppLoadingDialog(message = "Processing video…")
            }
        }

        is TrimVideoDialogState.Error -> {
            DialogWrapper(onDismissRequest = onDismiss) {
                ErrorDialog(
                    title = state.title,
                    message = state.message,
                    onClose = onDismiss,
                )
            }
        }

        TrimVideoDialogState.Information -> {
            DialogWrapper(onDismissRequest = onDismiss) {
                VideoInformationDialog(
                    videoUri = videoUri,
                    onClose = onDismiss,
                )
            }
        }

        TrimVideoDialogState.AskSelectOptionToTrimVideo -> {
            DialogWrapper(onDismissRequest = onDismiss) {
                AskSelectOptionToTrimVideoDialog(
                    onConfirm = onTrimConfirm,
                    onDismiss = onDismiss,
                )
            }
        }

        is TrimVideoDialogState.WarnSelectedNonKeyFrame -> {
            DialogWrapper(onDismissRequest = onDismiss) {
                WarnSelectedNonKeyFrameDialog(
                    nearestBeforeKeyFrame = state.nearestBeforeKeyFrame,
                    nearestAfterKeyFrame = state.nearestAfterKeyFrame,
                    onConfirm = onTrimConfirm,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun DialogWrapper(
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.2f),
            exit = fadeOut(tween(300)) + scaleOut(tween(300)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(TrimColorDialogBg)
                    .padding(16.dp),
            ) {
                content()
            }
        }
    }
}


@Composable
private fun AppLoadingDialog(message: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CircularProgressIndicator(
            color = TrimColorPrimary,
            strokeWidth = 2.dp,
        )
        Text(
            text = message,
            color = TrimColorText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorDialog(
    title: String,
    message: String,
    onClose: () -> Unit,
) {
    val maxMessageHeight = LocalConfiguration.current.screenHeightDp.dp * 0.6f

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = TrimColorText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxMessageHeight)
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = message,
                color = TrimColorText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DialogButton(text = stringResource(R.string.close), onClick = onClose, containerColor = TrimColorError)
        }
    }
}

@Composable
private fun VideoInformationDialog(
    videoUri: Uri?,
    onClose: () -> Unit,
) {
    val appContext = LocalContext.current.applicationContext
    val viewModel: TrimVideoViewModel = viewModel()
    val uiState = viewModel.videoNameState.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.getVideoInfo(context = appContext, uri = videoUri)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.video_info),
            color = TrimColorText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        when (uiState) {
            is VideoNameUiState.Loading -> {
                VideoInfoLoading()
            }

            is VideoNameUiState.Success -> {
                InfoRow(label = stringResource(R.string.file_name), value = uiState.video.name.orEmpty())
                InfoRow(label = stringResource(R.string.video_duration), value = uiState.video.duration.orZero().toTimestamp())
            }

            is VideoNameUiState.Error -> {
                InfoRow(label = stringResource(R.string.file_name), value = stringResource(R.string.unknown))
                InfoRow(label = stringResource(R.string.video_duration), value = "00:00")
            }

            else -> Unit
        }
        Spacer(Modifier.height(16.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DialogButton(text = stringResource(R.string.close), onClick = onClose)
        }
    }
}


@Composable
private fun VideoInfoLoading() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(DIMENSION_4),
    ) {
        repeat(2) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = DIMENSION_4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerLoading(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerLoading(),
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DIMENSION_4),
    ) {
        Text(
            text = "$label: ",
            color = TrimColorDeselected,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = TrimColorText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis // handle for long video name
        )
    }
}

@Composable
private fun AskSelectOptionToTrimVideoDialog(
    onConfirm: (TrimVideoOption) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedOption by remember { mutableStateOf<TrimVideoOption>(TrimVideoOption.TrimExactly) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.trim_video),
            color = TrimColorText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Please select an option to trim your video.",
            color = TrimColorText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Spacer(Modifier.height(16.dp))

        OptionLine(
            selected = selectedOption is TrimVideoOption.TrimExactly,
            label = stringResource(R.string.very_fast_but_no_compression),
            description = stringResource(R.string.very_fast_but_no_compression_description),
            onClick = { selectedOption = TrimVideoOption.TrimExactly },
        )
        Spacer(Modifier.height(16.dp))
        OptionLine(
            selected = selectedOption is TrimVideoOption.TrimAndCompress,
            label = stringResource(R.string.slow_but_compress_well),
            description = stringResource(R.string.slow_but_compress_well_description),
            onClick = { selectedOption = TrimVideoOption.TrimAndCompress },
        )
        Spacer(Modifier.height(24.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            DialogButton(text = stringResource(R.string.trim), onClick = { onConfirm(selectedOption) })
        }
    }
}

@Composable
private fun WarnSelectedNonKeyFrameDialog(
    nearestBeforeKeyFrame: Long,
    nearestAfterKeyFrame: Long,
    onConfirm: (TrimVideoOption) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedOption by remember {
        mutableStateOf<TrimVideoOption>(
            TrimVideoOption.TrimInexactly(nearestBeforeKeyFrame, nearestAfterKeyFrame)
        )
    }
    var showDetail by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.trim_video),
            color = TrimColorText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.warning_description),
            color = TrimColorText,
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        )
        Spacer(Modifier.height(16.dp))

        OptionLine(
            selected = selectedOption is TrimVideoOption.TrimInexactly,
            label = "Very fast but inexactly",
            description = if (showDetail)
                "Cut will snap to nearest keyframe.\nStart: ${nearestBeforeKeyFrame.toTimestamp()}, End: ${nearestAfterKeyFrame.toTimestamp()}"
            else null,
            onClick = {
                selectedOption = TrimVideoOption.TrimInexactly(nearestBeforeKeyFrame, nearestAfterKeyFrame)
                showDetail = !showDetail
            },
        )
        Spacer(Modifier.height(16.dp))
        OptionLine(
            selected = selectedOption is TrimVideoOption.TrimAndCompress,
            label = stringResource(R.string.slow_but_compress_well),
            description = null,
            onClick = { selectedOption = TrimVideoOption.TrimAndCompress },
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            DialogButton(
                text = stringResource(R.string.close),
                onClick = onDismiss
            )
            DialogButton(
                text = stringResource(R.string.trim),
                onClick = { onConfirm(selectedOption) }
            )
        }
    }
}

// ---- Shared dialog components ----

@Composable
private fun OptionLine(
    selected: Boolean,
    label: String,
    description: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (selected) TrimColorPrimary else Color.Transparent)
                .border(2.dp, if (selected) TrimColorPrimary else TrimColorDeselected, CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.clickable(onClick = onClick)) {
            Text(
                text = label,
                color = TrimColorText,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            AnimatedVisibility(visible = description != null) {
                description?.let {
                    Text(
                        text = it,
                        color = TrimColorDeselected,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

// Aligned to CenterEnd inside a Box
@Composable
private fun BoxScope.DialogButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = TrimColorPrimary,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.align(Alignment.CenterEnd),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Standalone (used in Row / side-by-side layouts)
@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = TrimColorPrimary,
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
