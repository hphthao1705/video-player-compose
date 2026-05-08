package com.example.playvideo.util

import com.example.playvideo.util.VideoHelper.printDebugStackTrace

object MathHelper {
    fun Long.toTimestamp(): String {
        val totalSeconds = this / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun Int.toLongOrNull(): Long? {
        return try {
            this.toLong()
        } catch (e: Exception) {
            e.printDebugStackTrace()
            null
        }
    }

    fun Int.toLongOrZero(): Long {
        return this.toLongOrNull() ?: 0L
    }
}
