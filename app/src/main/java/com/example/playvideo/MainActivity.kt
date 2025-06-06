package com.example.playvideo

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playvideo.data.videos

class MainActivity : AppCompatActivity() {

    private var player: AndroidVideoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        actionBar?.hide()

        //forcing Android to use the GPU to render views instead of the CPU.
        // => Improves video playback performance
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        //set
        player = AndroidVideoPlayer(this, findViewById(R.id.player_view))

        val uri = videos.getOrNull(1)

        if(uri.isNullOrBlank()) {
            Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show()
        } else {
            player?.playVideo(Uri.parse(uri))
        }
    }
}
