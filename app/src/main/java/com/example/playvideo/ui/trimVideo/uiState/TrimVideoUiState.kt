package com.example.playvideo.ui.trimVideo.uiState

import com.example.playvideo.ui.trimVideo.uiModel.LocalVideoUiModel

sealed class TrimVideoDialogState {
    data object StandBy : TrimVideoDialogState()
    data object Loading : TrimVideoDialogState()
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

enum class TrimVideoMode {
    Trim,
    Compress,
}

sealed class VideoNameUiState {
    object StandBy: VideoNameUiState()
    object Loading: VideoNameUiState()
    object Error: VideoNameUiState()
    data class Success(val video: LocalVideoUiModel): VideoNameUiState()
}