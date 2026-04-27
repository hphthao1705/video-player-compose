package com.example.playvideo.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playvideo.R

// ── Color tokens ────────────────────────────────────────────────────────────
private val BgPage      = Color(0xFF0D0D0D)
private val BgCard      = Color(0xFF1A1A1A)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted   = Color(0xFF666666)
private val ChevronTint = Color(0xFF444444)

data class HomeOption(
    val title: String,
    val subtitle: String,
    val iconRes: Int,
    val iconBgColor: Color,
    val onClick: () -> Unit,
)

@Composable
fun HomeScreen(
    onPlayVideo: () -> Unit = {},
    onTrimVideo: () -> Unit = {},
    onCompressVideo: () -> Unit = {},
) {
    val options = listOf(
        HomeOption(
            title = "Play Video",
            subtitle = "Stream or play local video files",
            iconRes = R.drawable.ic_play_video,
            iconBgColor = Color(0xFF1A3A5C),
            onClick = onPlayVideo,
        ),
        HomeOption(
            title = "Trim Video",
            subtitle = "Cut and trim video to desired length",
            iconRes = R.drawable.ic_trim_video,
            iconBgColor = Color(0xFF3D2800),
            onClick = onTrimVideo,
        ),
        HomeOption(
            title = "Compress Video",
            subtitle = "Reduce file size while keeping quality",
            iconRes = R.drawable.ic_compress_video,
            iconBgColor = Color(0xFF0D2E1A),
            onClick = onCompressVideo,
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPage)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Header ──────────────────────────────────────────────────────────
        Text(
            text = "PlayVideo",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Choose an action to get started",
            color = TextMuted,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(40.dp))

        // ── Option cards ────────────────────────────────────────────────────
        options.forEach { option ->
            OptionCard(option)
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OptionCard(option: HomeOption) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "cardScale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White),
                onClick = option.onClick,
            )
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(option.iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = option.iconRes),
                contentDescription = option.title,
                tint = Color.White,
                modifier = Modifier.size(26.dp),
            )
        }

        // Text
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = option.title,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = option.subtitle,
                color = TextMuted,
                fontSize = 13.sp,
            )
        }

        // Chevron
        Icon(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = ChevronTint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
