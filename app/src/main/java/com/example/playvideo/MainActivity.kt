package com.example.playvideo

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.playvideo.ui.HomeScreen
import com.example.playvideo.ui.chooseVideo.ChooseVideoScreen
import com.example.playvideo.ui.trimVideo.TrimVideoScreen
import com.example.playvideo.ui.trimmedVideo.TrimmedVideoScreen
import com.example.playvideo.ui.trimVideo.uiState.TrimVideoMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val videoPreviewViewModel: VideoPreviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)
        videoPreviewViewModel.preloadBuiltInPreviews()

        setContent {
            var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }
            var trimVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
            var trimmedVideoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
            var trimVideoMode by rememberSaveable { mutableStateOf(TrimVideoMode.Trim) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onPlayVideo = {
                            // TODO: Navigate to player screen
                            Toast.makeText(this, "Play Video", Toast.LENGTH_SHORT).show()
                        },
                        onTrimVideo = {
                            trimVideoMode = TrimVideoMode.Trim
                            currentScreen = AppScreen.CHOOSE_TRIM_VIDEO
                        },
                        onCompressVideo = {
                            trimVideoMode = TrimVideoMode.Compress
                            // TODO: Navigate to compress screen
                            Toast.makeText(this, "Compress Video", Toast.LENGTH_SHORT).show()
                        },
                    )
                }

                AppScreen.CHOOSE_TRIM_VIDEO -> {
                    ChooseVideoScreen(
                        onBack = { currentScreen = AppScreen.HOME },
                        onStartTrim = { selectedUri ->
                            trimVideoUri = selectedUri
                            currentScreen = AppScreen.TRIM_VIDEO
                        },
                    )
                }

                AppScreen.TRIM_VIDEO -> {
                    val uri = trimVideoUri
                    if (uri != null) {
                        TrimVideoScreen(
                            videoUri = uri,
                            mode = trimVideoMode,
                            onBack = { currentScreen = AppScreen.CHOOSE_TRIM_VIDEO },
                            onTrimSuccess = { resultUri ->
                                trimmedVideoUri = resultUri
                                currentScreen = AppScreen.PLAY_TRIMMED_VIDEO
                            },
                        )
                    } else {
                        currentScreen = AppScreen.CHOOSE_TRIM_VIDEO
                    }
                }

                AppScreen.PLAY_TRIMMED_VIDEO -> {
                    val uri = trimmedVideoUri
                    if (uri != null) {
                        TrimmedVideoScreen(
                            videoUri = uri,
                            onBack = { currentScreen = AppScreen.TRIM_VIDEO },
                        )
                    } else {
                        currentScreen = AppScreen.TRIM_VIDEO
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
    PLAY_TRIMMED_VIDEO,
}
