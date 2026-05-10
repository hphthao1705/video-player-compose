package com.example.playvideo.ui.trimVideo

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playvideo.ui.trimVideo.uiModel.LocalVideoUiModel
import com.example.playvideo.ui.trimVideo.uiState.TrimResultUiState
import com.example.playvideo.ui.trimVideo.uiState.VideoNameUiState
import com.example.playvideo.util.AppVideoUtil
import com.example.playvideo.util.AppVideoUtil.extractVideoFrames
import com.example.playvideo.util.AppVideoUtil.getDefaultOutputFolder
import com.example.playvideo.util.VideoHelper.debugLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrimVideoViewModel @Inject constructor(): ViewModel() {
    private val _videoNameState = MutableStateFlow<VideoNameUiState>(VideoNameUiState.StandBy)
    val videoNameState = _videoNameState.asStateFlow()

    private val _trimResultState = MutableStateFlow<TrimResultUiState>(TrimResultUiState.StandBy)
    val trimResultState = _trimResultState.asStateFlow()

    fun resetTrimResult() {
        _trimResultState.update { TrimResultUiState.StandBy }
    }

    suspend fun getFrameBitmaps(
        context: Context,
        uri: Uri,
        frameCount: Int = 8,
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        extractVideoFrames(context, uri, frameCount)
    }

    fun getVideoInfo(context: Context, uri: Uri?) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uri == null) {
                _videoNameState.update { VideoNameUiState.Error }
                return@launch
            }

            _videoNameState.update { VideoNameUiState.Loading }

            val videoName: String = AppVideoUtil.getVideoName(context, uri)
            val videoDuration: Long = AppVideoUtil.getVideoDuration(context, uri)
            _videoNameState.update { VideoNameUiState.Success(LocalVideoUiModel(
                name = videoName,
                duration = videoDuration
            )) }
        }
    }

    fun compressVideo(
        context: Context,
        startMs: Long,
        endMs: Long,
        inputUri: Uri,
    ) {
        viewModelScope.launch {
            _trimResultState.update { TrimResultUiState.Loading() }
            val outputFile = try {
                withContext(Dispatchers.IO) {
                    File(getDefaultOutputFolder(context = context), "video_compressed_${System.currentTimeMillis()}.mp4")
                }
            } catch (e: Exception) {
                "error when create file: ${e.message}".debugLog()
                _trimResultState.update { TrimResultUiState.Error(e.message ?: "Failed to create output file") }
                return@launch
            }
            val result = AppVideoUtil.compressVideo(
                context = context,
                inputUri = inputUri,
                startMs = startMs,
                endMs = endMs,
                outputFile = outputFile,
                onProgress = { progress ->
                    _trimResultState.update { TrimResultUiState.Loading(progress) }
                },
            )
            result
                .onSuccess { uri ->
                    "compressed Uri: $uri".debugLog()
                    _trimResultState.update { TrimResultUiState.Success(uri) }
                }
                .onFailure { e ->
                    "error compress: ${e.message}".debugLog()
                    _trimResultState.update { TrimResultUiState.Error(e.message ?: "Failed to compress video") }
                }
        }
    }

    fun trimVideo(
        context: Context,
        startMs: Long,
        endMs: Long,
        inputUri: Uri,
    ) {
        viewModelScope.launch {
            _trimResultState.update { TrimResultUiState.Loading() }
            val outputFile = try {
                withContext(Dispatchers.IO) {
                    File(getDefaultOutputFolder(context = context), "video_output_${System.currentTimeMillis()}.mp4")
                }
            } catch (e: Exception) {
                "error when create file: ${e.message}".debugLog()
                _trimResultState.update { TrimResultUiState.Error(e.message ?: "Failed to create output file") }
                return@launch
            }
            val result = AppVideoUtil.trimVideo(
                context = context,
                startMs = startMs,
                endMs = endMs,
                inputUri = inputUri,
                outputFile = outputFile,
            )
            result
                .onSuccess { uri ->
                    "new Uri: $uri".debugLog()
                    _trimResultState.update { TrimResultUiState.Success(uri) }
                }
                .onFailure { e ->
                    "error trim: ${e.message}".debugLog()
                    _trimResultState.update { TrimResultUiState.Error(e.message ?: "Failed to trim video") }
                }
        }
    }
}
