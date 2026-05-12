package com.example.playvideo.ui.chooseVideo.layout

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.playvideo.R
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.util.VideoHelper.printDebugStackTrace

private val TrimColorPrimary = Color(0xFFF9A825)

@Composable
fun StartTrimVideoSection(
    selectedVideo: VideoInfoData?,
    isLoading: Boolean = false,
    onStartTrim: (Uri) -> Unit,
    onNoVideoSelected: () -> Unit,
) {
    Button(
        onClick = {
            if (isLoading) return@Button
            if (selectedVideo == null) {
                onNoVideoSelected()
            } else {
                try {
                    selectedVideo.uri?.let { uri -> onStartTrim(uri) }
                } catch (e: Exception) {
                    e.printDebugStackTrace()
                }
            }
        },
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TrimColorPrimary,
            contentColor = Color.Black,
            disabledContainerColor = TrimColorPrimary.copy(alpha = 0.6f),
            disabledContentColor = Color.Black.copy(alpha = 0.6f),
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.Black,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = stringResource(R.string.start_to_trim_your_video),
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
