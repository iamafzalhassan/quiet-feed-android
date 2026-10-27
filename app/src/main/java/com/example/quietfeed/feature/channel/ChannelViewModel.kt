package com.example.quietfeed.feature.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietfeed.common.STATE_STOP_TIMEOUT_MILLIS
import com.example.quietfeed.data.repository.ChannelRepository
import com.example.quietfeed.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChannelViewModel(private val channelId: String, channelRepository: ChannelRepository, private val videoRepository: VideoRepository) : ViewModel() {
    private val feedback: MutableStateFlow<ChannelFeedback?> = MutableStateFlow(null)

    val state: StateFlow<ChannelUiState> = combine(
        channelRepository.observeChannelName(channelId),
        videoRepository.observeVideos(channelId).onEach { videos ->
            if (videos.any { video -> !video.isSeen }) {
                videoRepository.markSeen(channelId)
            }
        },
        feedback,
    ) { name, videos, currentFeedback -> ChannelUiState(isLoading = false, channelName = name.orEmpty(), videos = videos, feedback = currentFeedback) }
        .stateIn(initialValue = ChannelUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: ChannelEvent) {
        when (event) {
            ChannelEvent.ClearVideos -> clearVideos()
            ChannelEvent.DismissFeedback -> feedback.update { null }
        }
    }

    private fun clearVideos() {
        viewModelScope.launch {
            videoRepository.clearVideos(channelId)
            feedback.update { ChannelFeedback.VIDEOS_CLEARED }
        }
    }
}
