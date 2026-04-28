package com.example.playvideo

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.data.videos
import com.example.playvideo.util.VideoHelper.isLikelyVideoSource
import com.example.playvideo.util.VideoHelper.loadPreviewBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {
    private val _availableVideos = MutableStateFlow<List<VideoInfoData>>(emptyList())
    val availableVideos: StateFlow<List<VideoInfoData>> = _availableVideos.asStateFlow()

    fun preloadBuiltInPreviews() {
        if (_availableVideos.value.isNotEmpty()) return

        viewModelScope.launch {
            videos
                .filter(::isLikelyVideoSource)
                
                .forEach { source ->
                    val bitmap = withContext(Dispatchers.IO) {
                        loadPreviewBitmap(getApplication(), source.toUri())
                    }
                    upsertVideoInfo(source, bitmap)
                }
        }
    }

    fun cachePreview(uri: Uri, bitmap: Bitmap?) {
        upsertVideoInfo(uri.toString(), bitmap)
    }

    private fun upsertVideoInfo(url: String, bitmap: Bitmap?) {
        _availableVideos.update { current ->
            val index = current.indexOfFirst { it.url == url }
            if (index < 0) {
                current + VideoInfoData(url = url, previewBitmap = bitmap)
            } else {
                val existing = current[index]
                current.toMutableList().apply {
                    set(
                        index,
                        existing.copy(
                            url = url,
                            // Keep the previous bitmap if a new call only adds URL metadata.
                            previewBitmap = bitmap ?: existing.previewBitmap,
                        )
                    )
                }
            }
        }
    }
}
