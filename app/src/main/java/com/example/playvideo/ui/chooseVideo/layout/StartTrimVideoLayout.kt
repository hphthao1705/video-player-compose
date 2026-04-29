package com.example.playvideo.ui.chooseVideo.layout

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.playvideo.R
import com.example.playvideo.data.VideoInfoData
import com.example.playvideo.util.VideoHelper.printDebugStackTrace

@Composable
fun StartTrimVideoSection(
    selected: VideoInfoData?,
    onStartTrim: (Uri) -> Unit,
    onNoVideoSelected: () -> Unit,
) {
    Button(
        onClick = {
            if (selected == null) {
                onNoVideoSelected()
            } else {
                try {
                    onStartTrim(selected.url.orEmpty().toUri())
                } catch (e: Exception) {
                    e.printDebugStackTrace()
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF9A825),
            contentColor = Color.Black,
        ),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(
            text = stringResource(R.string.start_to_trim_your_video),
            fontWeight = FontWeight.SemiBold,
        )
    }
}
