package com.example.playvideo.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.playvideo.util.MathHelper.toLongOrZero
import com.example.playvideo.util.VideoHelper.printDebugStackTrace

object AppVideoUtil {
    fun extractVideoFrames(
        context: Context,
        uri: Uri,
        frameCount: Int,
    ): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        var frameList: List<Bitmap> = emptyList()

        try {
            retriever.setDataSource(context, uri) // set data source

            val durationMs = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) // to get the milliseconds of video
                ?.toLongOrNull() ?: 0L

            if (durationMs < 0L) return frameList

            frameList = (0 until frameCount).mapNotNull { i ->
                val timeUs = (i.toLongOrZero() * durationMs / frameCount) * 1_000L
                // get frame closest to (in time) or at the given time
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            }
        } catch (e: Exception) {
            e.printDebugStackTrace()
        } finally {
            retriever.release()
        }

        return frameList
    }
}
