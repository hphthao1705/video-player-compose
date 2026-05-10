package com.example.playvideo

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.playvideo.ui.HomeScreen
import com.example.playvideo.ui.chooseVideo.ChooseVideoScreen
import com.example.playvideo.ui.trimVideo.TrimVideoScreen
import com.example.playvideo.ui.trimmedVideo.TrimmedVideoScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val videoViewModel: VideoViewModel by viewModels()
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        videoViewModel.preloadBuiltInPreviews()

        setContent {
            val currentScreen = viewModel.currentScreen.collectAsState().value
            val videoOption = viewModel.option.collectAsState().value
            val selectedVideo = videoViewModel.selectedVideo.collectAsState().value
            var trimmedVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onPlayVideo = {
                            // TODO: Navigate to player screen
                            Toast.makeText(this, "Play Video", Toast.LENGTH_SHORT).show()
                        },
                        onTrimVideo = {
                            viewModel.updateOption(VideoOption.Trim)
                            viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                        },
                        onCompressVideo = {
                            viewModel.updateOption(VideoOption.Compress)
                            viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                        },
                    )
                }

                AppScreen.CHOOSE_TRIM_VIDEO -> {
                    ChooseVideoScreen(
                        onBack = {
                            viewModel.updateScreen(AppScreen.HOME)
                        },
                        onStartTrim = { selectedUri ->
//                            trimVideoUri = selectedUri
                            videoViewModel.changeSelectedVideo(
                                context = applicationContext,
                                uri = selectedUri
                            )
                            viewModel.updateScreen(AppScreen.TRIM_VIDEO)
                        },
                    )
                }

                AppScreen.TRIM_VIDEO -> {
                    val uri = selectedVideo?.uri
                    if (uri != null) {
                        TrimVideoScreen(
//                            selectedVideoData = selectedVideo,
                            mode = videoOption,
                            onBack = {
                                viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                            },
                            onTrimSuccess = { resultUri ->
                                trimmedVideoUri = resultUri
                                viewModel.updateScreen(AppScreen.PLAY_RESULT_VIDEO)
                            },
                        )
                    } else {
                        viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                    }
                }

                AppScreen.PLAY_RESULT_VIDEO -> {
                    val uri = trimmedVideoUri
                    if (uri != null) {
                        TrimmedVideoScreen(
                            videoUri = uri,
                            onBack = {
                                viewModel.updateScreen(AppScreen.TRIM_VIDEO)
                            },
                        )
                    } else {
                        viewModel.updateScreen(AppScreen.TRIM_VIDEO)
                    }
                }
            }
        }
    }
}

enum class AppScreen {
    HOME,
    CHOOSE_TRIM_VIDEO,
    TRIM_VIDEO,
    PLAY_RESULT_VIDEO,
}
