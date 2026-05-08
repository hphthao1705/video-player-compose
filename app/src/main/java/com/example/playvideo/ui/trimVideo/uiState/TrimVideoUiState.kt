package com.example.playvideo.ui.trimVideo.uiState

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