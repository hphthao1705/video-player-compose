package com.example.playvideo.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.use

object VideoHelper {
    fun String.debugLog(tag: String = "rabbit") {
        Log.d(tag, this)
    }

    fun Exception.printDebugStackTrace() {
        Log.d("exception_error", this.toString())
    }

    suspend fun loadPreviewBitmap(context: Context, uri: Uri): Bitmap? {
        return withContext(Dispatchers.IO.limitedParallelism(2)) {
            runCatching {
                val source = uri.toString()
                val isRemote = source.startsWith("http")

                if (!isRemote && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    return@runCatching context.contentResolver.loadThumbnail(
                        uri, Size(300, 300), null
                    )
                }

                MediaMetadataRetriever().use { retriever ->
                    if (isRemote) {
                        retriever.setDataSource(source, HashMap<String, String>())
                    } else {
                        retriever.setDataSource(context, uri)
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                        /**
                         * timeUs: The time position where the frame will be retrieved.
                         * dstWidth - int: expected output bitmap width. Value is 1 or greater
                         * dstHeight - int: expected output bitmap height. Value is 1 or greater
                         */
                        retriever.getScaledFrameAtTime(
                            1000000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                            300,
                            300
                        )
                    } else {
                        /**
                         * timeUs: The time position where the frame will be retrieved.
                         * option:  a hint on how the frame is found.
                         *  + OPTION_PREVIOUS_SYNC: retrieve a sync frame that has a timestamp earlier than or the same as timeUs
                         *  + OPTION_NEXT_SYNC: retrieve a sync frame that has a timestamp later than or the same as timeUs
                         *  + OPTION_CLOSEST_SYNC: retrieve a sync frame that has a timestamp closest to or the same as timeUs
                         *  + OPTION_CLOSEST: retrieve a frame that may or may not be a sync frame but is closest to or the same as timeUs. Often has larger performance overhead compared to the other options if there is no sync frame located at timeUs.
                         */
                        retriever.getFrameAtTime(
                            1000000,
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                    }
                }
            }.onFailure {
                "Failed to load $uri".debugLog()
            }.getOrNull()
        }
    }

    fun isLikelyVideoSource(source: String): Boolean {
        val type = source.substringBefore('?').lowercase()
        return type.endsWith(".mp4") ||
                type.endsWith(".webm") ||
                type.endsWith(".m3u8") ||
                type.endsWith(".mov") ||
                type.endsWith(".mkv")
    }
}
