package com.example.playvideo.ui.trimVideo.uiState

import android.net.Uri

sealed class TrimVideoDialogState {
    data object StandBy : TrimVideoDialogState()
    data class Loading(val progress: Float = 0f) : TrimVideoDialogState()
    data class Error(val title: String, val message: String) : TrimVideoDialogState()
    data object Information : TrimVideoDialogState()
    data object AskSelectOptionToTrimVideo : TrimVideoDialogState()
    data class WarnSelectedNonKeyFrame(
        val nearestBeforeKeyFrame: Long,
        val nearestAfterKeyFrame: Long,
    ) : TrimVideoDialogState()
}

sealed class TrimVideoOption {
    data object TrimExactly : TrimVideoOption()
    data class TrimInexactly(
        val nearestBeforeKeyFrame: Long,
        val nearestAfterKeyFrame: Long,
    ) : TrimVideoOption()
    data object TrimAndCompress : TrimVideoOption()
}
//
//sealed class VideoNameUiState {
//    object StandBy: VideoNameUiState()
//    object Loading: VideoNameUiState()
//    object Error: VideoNameUiState()
//    data class Success(val video: VideoInfoData): VideoNameUiState()
//}

sealed class TrimResultUiState {
    data object StandBy : TrimResultUiState()
    data class Loading(val progress: Float = 0f) : TrimResultUiState()
    data class Success(val uri: Uri) : TrimResultUiState()
    data class Error(val message: String) : TrimResultUiState()
}
