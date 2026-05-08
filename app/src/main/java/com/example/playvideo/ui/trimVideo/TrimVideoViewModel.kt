package com.example.playvideo.ui.trimVideo

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playvideo.ui.trimVideo.uiModel.LocalVideoUiModel
import com.example.playvideo.ui.trimVideo.uiState.VideoNameUiState
import com.example.playvideo.util.AppVideoUtil
import com.example.playvideo.util.AppVideoUtil.extractVideoFrames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TrimVideoViewModel @Inject constructor(): ViewModel() {
    private val _videoNameState = MutableStateFlow<VideoNameUiState>(VideoNameUiState.StandBy)
    val videoNameState = _videoNameState.asStateFlow()

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
}