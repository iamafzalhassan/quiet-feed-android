package com.example.quietfeed.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietfeed.common.STATE_STOP_TIMEOUT_MILLIS
import com.example.quietfeed.data.repository.ChannelRepository
import com.example.quietfeed.data.repository.VideoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class ChannelViewModel(channelId: String, channelRepository: ChannelRepository, videoRepository: VideoRepository) : ViewModel() {
    val state: StateFlow<ChannelUiState> = combine(
        channelRepository.observeChannelName(channelId),
        videoRepository.observeVideos(channelId).onEach { videos ->
            if (videos.any { video -> !video.isSeen }) {
                videoRepository.markSeen(channelId)
            }
        },
    ) { name, videos -> ChannelUiState(isLoading = false, channelName = name.orEmpty(), videos = videos) }
        .stateIn(initialValue = ChannelUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))
}
