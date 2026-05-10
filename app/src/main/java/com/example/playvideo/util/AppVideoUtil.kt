package com.example.playvideo.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodecInfo
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.VideoEncoderSettings
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import com.example.playvideo.util.MathHelper.toLongOrZero
import com.example.playvideo.util.VideoHelper.debugLog
import com.example.playvideo.util.VideoHelper.printDebugStackTrace
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

const val FOLDER_TRIM_VIDEO = "videos/trim"
object AppVideoUtil {
    const val MAX_ALLOWED_TRIM_TIME = 30_000L // 30 seconds

    fun getDefaultOutputFolder(context: Context) : File {
        val file = File(context.cacheDir, FOLDER_TRIM_VIDEO)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file
    }

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

    @OptIn(UnstableApi::class)
    suspend fun trimVideo(
        context: Context,
        startMs: Long,
        endMs: Long,
        inputUri: Uri,
        outputFile: File,
    ): Result<Uri> = suspendCancellableCoroutine { continuation ->
        /**
         * If the input media format already matches the transformation request for audio or video,
         * Transformer automatically switches to transmuxing
         * copying the compressed samples from the input container to the output container without modification
         */
        val transformer = Transformer.Builder(context)
            // TODO: we should ask that is it necessary when trim video only
//            .setVideoMimeType(MimeTypes.VIDEO_H265)
//            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .build()

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                if (continuation.isActive) {
                    continuation.resume(Result.success(Uri.fromFile(outputFile)))
                }
            }

            override fun onError(
                composition: Composition,
                exportResult: ExportResult,
                exportException: ExportException,
            ) {
                exportException.printDebugStackTrace()
                if (continuation.isActive) {
                    continuation.resume(Result.failure(exportException))
                }
            }

            override fun onFallbackApplied(
                composition: Composition,
                originalTransformationRequest: TransformationRequest,
                fallbackTransformationRequest: TransformationRequest,
            ) {
                "Transformer fallback applied: $originalTransformationRequest -> $fallbackTransformationRequest".debugLog()
            }
        }
        transformer.addListener(listener)

        // TODO - TH: I see the HDR recommendation, please take a look when have time

        val clippingConfiguration = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(startMs)
            .setEndPositionMs(endMs)
            .build()

        val mediaItem: MediaItem = MediaItem.Builder()
            .setUri(inputUri)
            .setClippingConfiguration(clippingConfiguration)
            .build()

        continuation.invokeOnCancellation {
            transformer.removeListener(listener)
            transformer.cancel()
        }

        try {
            transformer.start(mediaItem, outputFile.absolutePath)
        } catch (e: Exception) {
            e.printDebugStackTrace()
            if (continuation.isActive) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    private fun getBitrate(width: Int, height: Int, frameRate: Float): Int {
        val bpp = 0.07f // safe for H.264
        /**
         * Bitrate = Width x Height x FPS x BPP
         * BPP from 0.07 -> 0.1: give very good quality (equivalent to high compression standards)
         * BPP from 0.1 -> 0.15: give high quality (usually used for the original videos)
         */
        val calculated: Int = (width * height * frameRate * bpp).toInt()
        return when {
            calculated < 1_000_000 -> 1_000_000
            calculated > 8_000_000 -> 8_000_000
            else -> calculated
        }
    }

    @OptIn(UnstableApi::class)
    fun compressVideo(
        context: Context,
        inputUri: Uri,
        outputFile: File
    ) {
        val retriever = MediaMetadataRetriever()
        try {
            // delete after check
            val originalSize = File(inputUri.path ?: "").length().takeIf { it > 0 }
                ?: context.contentResolver.openAssetFileDescriptor(inputUri, "r")?.length ?: 0L
            val originalBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLong() ?: 0L

            val currentWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
            val currentHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
            val currentFPS = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloat() ?: 30f

            val targetBitrate = getBitrate(currentWidth, currentHeight, currentFPS)
            val outputFilePath = outputFile.absolutePath

            val videoEncoderSettings = VideoEncoderSettings.Builder()
                .setBitrate(targetBitrate)
                .setBitrateMode(MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR)
                .build()

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setRequestedVideoEncoderSettings(videoEncoderSettings)
                .build()

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setEncoderFactory(encoderFactory)
                .build()

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    val compressedSize = outputFile.length()
                    val reductionPercent = ((originalSize - compressedSize).toDouble() / originalSize * 100).toInt()

                    // Xuất Log so sánh
                    val logOutput = """
                    
                    ========= KẾT QUẢ NÉN VIDEO =========
                    📌 THÔNG SỐ GỐC:
                    - Độ phân giải: ${currentWidth}x${currentHeight}
                    - Bitrate: ${originalBitrate / 1000} Kbps
                    - Dung lượng: ${originalSize / 1024 / 1024} MB
                    
                    🚀 THÔNG SỐ SAU NÉN:
                    - Target Bitrate set: ${targetBitrate / 1000} Kbps
                    - Dung lượng mới: ${compressedSize / 1024 / 1024} MB
                    
                    📉 HIỆU QUẢ: Giảm $reductionPercent% dung lượng!
                    =====================================
                """.trimIndent()

                    logOutput.debugLog() // Dùng hàm debugLog của bạn
                }

                override fun onError(composition: Composition, exportResult: ExportResult, e: ExportException) {
                    "Lỗi khi nén: ${e.message}".debugLog()
                }
            })

            val mediaItem = MediaItem.fromUri(inputUri)
            transformer.start(mediaItem, outputFilePath)
        } catch (e: Exception) {
            e.printDebugStackTrace()
        } finally {
            retriever.release()
        }
    }
}
