package com.example.playvideo.data

data class VideoMetadata(
    val width: Int,
    val height: Int,
    val fps: Float,
    val bitrateKbps: Long,
    val sizeMb: Double,
)
