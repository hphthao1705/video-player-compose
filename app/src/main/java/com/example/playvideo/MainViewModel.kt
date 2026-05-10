package com.example.playvideo

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(): ViewModel() {
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen

    private val _option = MutableStateFlow(VideoOption.Nothing)
    val option = _option.asStateFlow()

    fun updateScreen(screen: AppScreen) {
        _currentScreen.update { screen }
    }

    fun updateOption(option: VideoOption) {
        _option.update { option }
    }
}

enum class VideoOption {
    Nothing,
    Play,
    Trim,
    Compress,
}
