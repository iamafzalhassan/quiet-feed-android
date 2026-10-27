package com.example.quietfeed.feature.channels

import androidx.compose.runtime.Immutable
import com.example.quietfeed.domain.channel.AddChannelError
import com.example.quietfeed.domain.channel.Channel

@Immutable
data class ChannelsUiState(
    val isAdding: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val addedChannelName: String? = null,
    val channels: List<Channel> = emptyList(),
    val addError: AddChannelError? = null,
    val feedback: ChannelsFeedback? = null,
)
