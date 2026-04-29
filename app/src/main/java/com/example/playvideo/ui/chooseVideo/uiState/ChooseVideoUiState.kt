package com.example.playvideo.ui.chooseVideo.uiState

import com.example.playvideo.data.VideoInfoData

sealed class ChooseVideoUiState {
    object StandBy: ChooseVideoUiState()
    object Loading : ChooseVideoUiState()
    data class Error(val message: String) : ChooseVideoUiState()
    data class Success(val videos: List<VideoInfoData>) : ChooseVideoUiState()
}
