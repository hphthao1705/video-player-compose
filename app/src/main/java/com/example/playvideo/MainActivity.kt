package com.example.playvideo

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.playvideo.ui.HomeScreen
import com.example.playvideo.ui.TrimChooseVideoScreen

class MainActivity : ComponentActivity() {

    private enum class AppScreen {
        HOME,
        CHOOSE_TRIM_VIDEO
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        setContent {
            var currentScreen by rememberSaveable { mutableStateOf(AppScreen.HOME) }

            when (currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        onPlayVideo = {
                            // TODO: Navigate to player screen
                            Toast.makeText(this, "Play Video", Toast.LENGTH_SHORT).show()
                        },
                        onTrimVideo = {
                            currentScreen = AppScreen.CHOOSE_TRIM_VIDEO
                        },
                        onCompressVideo = {
                            // TODO: Navigate to compress screen
                            Toast.makeText(this, "Compress Video", Toast.LENGTH_SHORT).show()
                        },
                    )
                }

                AppScreen.CHOOSE_TRIM_VIDEO -> {
                    TrimChooseVideoScreen(
                        onBack = { currentScreen = AppScreen.HOME },
                        onStartTrim = { selectedUri ->
                            // TODO: Navigate to real trim editor with selectedUri.
                            Toast.makeText(
                                this,
                                "Start trimming: $selectedUri",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    )
                }
            }
        }
    }
}
