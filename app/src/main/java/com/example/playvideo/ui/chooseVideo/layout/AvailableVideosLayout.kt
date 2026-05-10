package com.example.playvideo.ui.chooseVideo.layout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.playvideo.R
import com.example.playvideo.data.AvailableVideoInfoData
import com.example.playvideo.util.shimmerLoading

@Composable
fun AvailableVideosSection(
    videos: List<AvailableVideoInfoData>,
    selectedVideo: AvailableVideoInfoData?,
    onSelectVideo: (AvailableVideoInfoData) -> Unit,
) {
    Text(
        text = stringResource(R.string.or_choose_1_in_5_built_in_videos),
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(Modifier.height(8.dp))

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(videos) { _, option ->
            val isSelected = selectedVideo?.url == option.url
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectVideo(option) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color(0xFF24452B) else Color(0xFF1A1A1A)
                ),
            ) {
                Text(
                    text = option.label.orEmpty(),
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }
    }
}

@Composable
fun AvailableVideosLoading(itemCount: Int) {
    Text(
        text = stringResource(R.string.or_choose_1_in_5_built_in_videos),
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
    )

    Spacer(Modifier.height(8.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(itemCount) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
            ) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(0.55f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerLoading(),
                )
            }
        }
    }
}
