package com.example.playvideo.data

import android.graphics.Bitmap

data class AvailableVideoInfoData(
    val label: String? = null,
    val url: String? = null,
    val previewBitmap: Bitmap? = null,
    var isSelected: Boolean = false,
)
