package com.example.playvideo

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
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

        setContent {
            val context = LocalContext.current

            val currentScreen = viewModel.currentScreen.collectAsState().value
            val finalVideo = viewModel.finalVideo.collectAsState().value
            val videoOption = viewModel.option.collectAsState().value
            val selectedVideo = videoViewModel.selectedVideo.collectAsState().value
            val isPreparingVideo = videoViewModel.isPreparingVideo.collectAsState().value

            // Navigate to trim screen only after video data is fully prepared —
            // this eliminates the first loading phase on TrimVideoScreen.
            LaunchedEffect(isPreparingVideo, selectedVideo) {
                if (currentScreen == AppScreen.PREPARING_VIDEO
                    && !isPreparingVideo
                    && selectedVideo != null
                ) {
                    viewModel.updateScreen(AppScreen.TRIM_VIDEO)
                }
            }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onPlayVideo = {
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
                        isPreparingVideo = isPreparingVideo,
                        onBack = {
                            viewModel.updateScreen(AppScreen.HOME)
                        },
                        onStartTrim = { selectedUri ->
                            // Kick off IO preparation, then wait — LaunchedEffect above navigates
                            // once both isPreparingVideo == false and selectedVideo != null.
                            videoViewModel.changeSelectedVideo(
                                context = context,
                                uri = selectedUri,
                            )
                            viewModel.updateScreen(AppScreen.PREPARING_VIDEO)
                        },
                    )
                }

                // Intermediate state: data is being fetched, still showing ChooseVideoScreen
                // with a loading overlay so the user has visual feedback.
                AppScreen.PREPARING_VIDEO -> {
                    ChooseVideoScreen(
                        isPreparingVideo = true,
                        onBack = {
                            viewModel.updateScreen(AppScreen.HOME)
                        },
                        onStartTrim = {},
                    )
                }

                AppScreen.TRIM_VIDEO -> {
                    val uri = selectedVideo?.uri
                    if (uri != null) {
                        TrimVideoScreen(
                            mode = videoOption,
                            onBack = {
                                viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                            },
                            onTrimSuccess = { resultUri ->
                                viewModel.updateFinalVideo(resultUri)
                                viewModel.updateScreen(AppScreen.PLAY_RESULT_VIDEO)
                            },
                        )
                    } else {
                        viewModel.updateScreen(AppScreen.CHOOSE_TRIM_VIDEO)
                    }
                }

                AppScreen.PLAY_RESULT_VIDEO -> {
                    val uri = finalVideo
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
    PREPARING_VIDEO,      // IO in progress — still showing ChooseVideoScreen with loading overlay
    TRIM_VIDEO,
    PLAY_RESULT_VIDEO,
}
