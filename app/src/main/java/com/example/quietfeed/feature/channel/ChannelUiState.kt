package com.example.quietfeed.feature.channel

import androidx.compose.runtime.Immutable
import com.example.quietfeed.domain.video.Video

@Immutable
data class ChannelUiState(val isLoading: Boolean = true, val channelName: String = "", val videos: List<Video> = emptyList())
