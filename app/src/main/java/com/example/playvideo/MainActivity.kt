package com.example.playvideo

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.playvideo.ui.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        setContent {
            HomeScreen(
                onPlayVideo = {
                    // TODO: Navigate to player screen
                    Toast.makeText(this, "Play Video", Toast.LENGTH_SHORT).show()
                },
                onTrimVideo = {
                    // TODO: Navigate to trim screen
                    Toast.makeText(this, "Trim Video", Toast.LENGTH_SHORT).show()
                },
                onCompressVideo = {
                    // TODO: Navigate to compress screen
                    Toast.makeText(this, "Compress Video", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
