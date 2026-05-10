package com.example.playvideo.data

import android.graphics.Bitmap
import android.net.Uri

data class VideoInfoData(
    val isReadyToTrim: Boolean? = false,
    val isStopPlayVideo: Boolean? = null,
    val name: String? = null,
    val uri: Uri? = null,
    val duration: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Float? = null,
    val bitrateKbps: Long? = null,
    val sizeMb: Double? = null,
    val previewBitmaps: List<Bitmap>? = null,
    val startTrimMs: Long? = null,
    val endTrimMs: Long? = null,
    val seekTo: Long? = null
)

data class AvailableVideoInfoData(
    val label: String? = null,
    val url: String? = null,
    val previewBitmap: Bitmap? = null,
    var isSelected: Boolean = false,
)

