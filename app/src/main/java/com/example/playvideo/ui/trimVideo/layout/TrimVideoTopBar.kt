package com.example.playvideo.ui.trimVideo.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playvideo.R

private val TrimColorPrimary = Color(0xFFF9A825)
private val TrimColorDeselected = Color(0xFF666666)
private val TrimColorText = Color.White

@Composable
fun TrimVideoTopBar(
//    title: String,
    isReadyToTrim: Boolean,
    onBackClick: () -> Unit,
    onInfoClick: () -> Unit,
    onTrimClick: () -> Unit,
) {
    val iconTint = if (isReadyToTrim) TrimColorPrimary else TrimColorDeselected

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .size(28.dp)
                .clip(CircleShape),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = "Back",
                tint = TrimColorText,
                modifier = Modifier.size(28.dp),
            )
        }

        Text(
            text = stringResource(R.string.trim_video),
            color = TrimColorText,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp,
            letterSpacing = (-0.41).sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 72.dp),
        )

        // Info icon — 56dp from right edge
        IconButton(
            onClick = onInfoClick,
            enabled = isReadyToTrim,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 56.dp)
                .size(28.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Information",
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )
        }

        // Trim icon — 12dp from right edge
        IconButton(
            onClick = onTrimClick,
            enabled = isReadyToTrim,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .size(28.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_trim_video),
                contentDescription = stringResource(R.string.trim),
                tint = iconTint,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}