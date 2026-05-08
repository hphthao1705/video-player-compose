package com.example.playvideo.ui.trimVideo

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.playvideo.util.AppVideoUtil.extractVideoFrames
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TrimVideoViewModel @Inject constructor(): ViewModel() {
    suspend fun getFrameBitmaps(
        context: android.content.Context,
        uri: Uri,
        frameCount: Int = 8,
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        extractVideoFrames(context, uri, frameCount)
    }
}