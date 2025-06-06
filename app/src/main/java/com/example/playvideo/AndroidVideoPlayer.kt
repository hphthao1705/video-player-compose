package com.example.playvideo

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AndroidVideoPlayer(
    private var context: Context,
    val playerView: PlayerSurface,
) {
    //ExoPlayer is the default implementation of the Player interface in Media3.
    private var player: ExoPlayer? = null

    fun playVideo(uri: Uri?, autoPlay: Boolean = false) {
        uri ?: return

        val mediaItem = MediaItem.fromUri(uri)
        player = ExoPlayer.Builder(context).build().apply {
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = autoPlay
//            play()
        }
        playerView.player = player
    }

    fun addListener(listener: Player.Listener) {
        player?.addListener(listener)
    }

}