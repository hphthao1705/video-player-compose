package com.example.playvideo

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playvideo.data.AvailableVideoInfoData
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.ui.chooseVideo.uiState.ChooseVideoUiState
import com.example.playvideo.util.AppVideoUtil.getVideoDuration
import com.example.playvideo.util.AppVideoUtil.getVideoMetaData
import com.example.playvideo.util.AppVideoUtil.getVideoName
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
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    private val availableVideos = arrayListOf<AvailableVideoInfoData>()

    private val _availableVideosUiState = MutableStateFlow<ChooseVideoUiState>(ChooseVideoUiState.StandBy)
    val availableVideosUiState: StateFlow<ChooseVideoUiState> = _availableVideosUiState.asStateFlow()

    private val _selectedVideo = MutableStateFlow<VideoInfoData?>(null)
    val selectedVideo: StateFlow<VideoInfoData?> = _selectedVideo.asStateFlow()

    fun updateSelectedVideo(transformer: (VideoInfoData) -> VideoInfoData) {
        _selectedVideo.update { currentVideo ->
            // If currentVideo is null, we can't transform it,
            // so you might need a null-safe check here depending on your logic
            currentVideo?.let(transformer)
        }
    }

    fun setSelectedVideo(video: VideoInfoData) {
        _selectedVideo.update { video }
    }

    fun changeSelectedVideo(context: Context, uri: Uri?) {
        // TODO: Handle error here
        uri ?: return

        viewModelScope.launch(Dispatchers.IO) {
            val info = getVideoInfo(context = context, uri = uri)
            info?.let { videoData ->
                setSelectedVideo(video = videoData)
            }
        }
    }

    private suspend fun getVideoInfo(context: Context, uri: Uri): VideoInfoData? = withContext(Dispatchers.IO) {
        val videoTitle = getVideoName(context = context, uri = uri)
        val videoDuration = getVideoDuration(context = context, uri = uri)
        val videoMetadata = getVideoMetaData(context = context, inputUri = uri)

        return@withContext VideoInfoData(
            name = videoTitle,
            uri = uri,
            duration = videoDuration,
            width = videoMetadata.width,
            height = videoMetadata.height,
            fps = videoMetadata.fps,
            bitrateKbps = videoMetadata.bitrateKbps,
            sizeMb = videoMetadata.sizeMb
        )
    }

//    fun selectVideo(video: AvailableVideoInfoData) {
//        availableVideos.replaceAll { it.copy(isSelected = it.url == video.url) }
////        _selectedVideo.update { video }
//        _availableVideosUiState.update {
//            ChooseVideoUiState.Success(availableVideos.toList())
//        }
//    }

    fun previewLocalVideo(uri: Uri) {
        viewModelScope.launch {
            val bitmap: Bitmap? = loadPreviewBitmap(getApplication(), uri)
            availableVideos.replaceAll { it.copy(isSelected = false) }
            _availableVideosUiState.update { current ->
                if (current is ChooseVideoUiState.Success) ChooseVideoUiState.Success(availableVideos.toList()) else current
            }

            if (bitmap != null) {
                _selectedVideo.update { VideoInfoData(uri = uri, previewBitmaps = listOf<Bitmap>(bitmap)) }
            }
        }
    }

    fun preloadBuiltInPreviews() {
        if (availableVideos.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.Default) {
            _availableVideosUiState.update { ChooseVideoUiState.Loading }

            val sources = videos.filter(::isLikelyVideoSource)
            val processedVideos: List<AvailableVideoInfoData> = sources.mapIndexed { index, source ->
                async {
                    val bitmap = loadPreviewBitmap(getApplication(), source.toUri())
                    AvailableVideoInfoData(label = "Sample video ${index + 1}", url = source, previewBitmap = bitmap)
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

    override fun onCleared() {
        super.onCleared()
        _selectedVideo.value?.previewBitmaps?.forEach { bitmap ->
            if (!bitmap.isRecycled) bitmap.recycle()
        }
    }
}
