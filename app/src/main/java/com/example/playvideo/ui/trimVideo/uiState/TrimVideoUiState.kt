package com.example.playvideo.ui.trimVideo.uiState

import android.net.Uri

sealed class DialogState {
    data object StandBy : DialogState()
    data class Loading(val progress: Float = 0f) : DialogState()
    data class Error(val title: String, val message: String) : DialogState()
    data object Information : DialogState()
    data object AskSelectOptionTo : DialogState()
    data class WarnSelectedNonKeyFrame(
        val nearestBeforeKeyFrame: Long,
        val nearestAfterKeyFrame: Long,
    ) : DialogState()
}

sealed class TrimVideoOption {
    data object TrimExactly : TrimVideoOption()
    data class TrimInexactly(
        val nearestBeforeKeyFrame: Long,
        val nearestAfterKeyFrame: Long,
    ) : TrimVideoOption()
    data object TrimAndCompress : TrimVideoOption()
}

sealed class TrimResultUiState {
    data object StandBy : TrimResultUiState()
    data class Loading(val progress: Float = 0f) : TrimResultUiState()
    data class Success(val uri: Uri) : TrimResultUiState()
    data class Error(val message: String) : TrimResultUiState()
}
