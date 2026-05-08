package com.example.playvideo.ui.trimVideo.uiModel

import android.graphics.Bitmap
import android.net.Uri
import java.util.Collections

data class TrimVideoUiModel(
    val title: String = "",
    val isReadyToTrim: Boolean = false,
    val videoUri: Uri? = null,
    val seekTo: Long = -1L,
    val videoDurationInMilSecond: Long = 0L,
    val maxAllowedTrimTime: Long = 45_000L,
    val frameBitmaps: List<Bitmap> = Collections.emptyList(),
    val isStopPlayVideo: Boolean = false,
    val startSeekTime: Long = 0L,
    val endSeekTime: Long = 45_000L,
)

class LocalVideoUiModel(
    val name: String? = null,
    val uri: Uri? = null,
    val duration: Long? = 0L,
)