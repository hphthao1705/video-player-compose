package com.example.playvideo

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.ui.chooseVideo.uiState.ChooseVideoUiState
import com.example.playvideo.util.VideoHelper.isLikelyVideoSource
import com.example.playvideo.util.VideoHelper.loadPreviewBitmap
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class VideoPreviewViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val availableVideos = arrayListOf<VideoInfoData>()

    private val _availableVideosUiState = MutableStateFlow<ChooseVideoUiState>(ChooseVideoUiState.StandBy)
    val availableVideosUiState: StateFlow<ChooseVideoUiState> = _availableVideosUiState.asStateFlow()

    private val _selectedVideo = MutableStateFlow<VideoInfoData?>(null)
    val selectedVideo: StateFlow<VideoInfoData?> = _selectedVideo.asStateFlow()

    fun selectVideo(video: VideoInfoData) {
        availableVideos.replaceAll { it.copy(isSelected = it.url == video.url) }
        _selectedVideo.update { video }
        _availableVideosUiState.update {
            ChooseVideoUiState.Success(availableVideos.toList())
        }
    }

    fun previewLocalVideo(uri: Uri) {
        viewModelScope.launch {
            val bitmap = loadPreviewBitmap(getApplication(), uri)
            availableVideos.replaceAll { it.copy(isSelected = false) }
            _availableVideosUiState.update { current ->
                if (current is ChooseVideoUiState.Success) ChooseVideoUiState.Success(availableVideos.toList()) else current
            }
            _selectedVideo.update { VideoInfoData(url = uri.toString(), previewBitmap = bitmap) }
        }
    }

    fun preloadBuiltInPreviews() {
        if (availableVideos.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            _availableVideosUiState.update { ChooseVideoUiState.Loading }

            val sources = videos.filter(::isLikelyVideoSource)
            val processedVideos: List<VideoInfoData> = sources.mapIndexed { index, source ->
                async {
                    val bitmap = loadPreviewBitmap(getApplication(), source.toUri())
                    VideoInfoData(label = "Sample video ${index + 1}", url = source, previewBitmap = bitmap)
                }
            }.awaitAll()

            availableVideos.addAll(processedVideos)

            _availableVideosUiState.update {
                if (availableVideos.isEmpty()) {
                    ChooseVideoUiState.Error("No videos available")
                } else {
                    ChooseVideoUiState.Success(availableVideos.toList())
                }
            }
        }
    }
}
