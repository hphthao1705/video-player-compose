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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.playvideo.R
import com.example.playvideo.util.AppDimension.DIMENSION_16
import com.example.playvideo.util.AppDimension.DIMENSION_24
import com.example.playvideo.util.AppDimension.DIMENSION_40
import com.example.playvideo.util.AppDimension.DIMENSION_48

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
            title = stringResource(R.string.home_option_play_video_title),
            subtitle = stringResource(R.string.home_option_play_video_subtitle),
            iconRes = R.drawable.ic_play_video,
            iconBgColor = Color(0xFF1A3A5C),
            onClick = onPlayVideo,
        ),
        HomeOption(
            title = stringResource(R.string.home_option_trim_video_title),
            subtitle = stringResource(R.string.home_option_trim_video_subtitle),
            iconRes = R.drawable.ic_trim_video,
            iconBgColor = Color(0xFF3D2800),
            onClick = onTrimVideo,
        ),
        HomeOption(
            title = stringResource(R.string.home_option_compress_video_title),
            subtitle = stringResource(R.string.home_option_compress_video_subtitle),
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
            .padding(horizontal = DIMENSION_24),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(DIMENSION_48))

        // ── Header ──────────────────────────────────────────────────────────
        Text(
            text = stringResource(R.string.app_name),
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.5).sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_subtitle),
            color = TextMuted,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(DIMENSION_40))

        // ── Option cards ────────────────────────────────────────────────────
        options.forEach { option ->
            OptionCard(option)
            Spacer(Modifier.height(DIMENSION_16))
        }

        Spacer(Modifier.height(DIMENSION_24))
    }
}

@Composable
private fun OptionCard(option: HomeOption) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = stringResource(R.string.home_card_scale_label),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(DIMENSION_16))
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
                .padding(horizontal = DIMENSION_16),
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
