package com.example.playvideo.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
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

    fun getVideoName(context: Context, uri: Uri): String {
        var name: String? = null

        // case 1: uri starts with "content://" (from Gallery/File Picker)
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            } catch (e: Exception) {
                e.printDebugStackTrace()
            } finally {
                cursor?.close()
            }
        }

        // case 2: uri starts with "file://" or not get from ContentResolver
        if (name == null) {
            name = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }

        return name ?: "Unknown"
    }

    fun getVideoDuration(context: Context, uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)

            val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            time?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printDebugStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }
}
