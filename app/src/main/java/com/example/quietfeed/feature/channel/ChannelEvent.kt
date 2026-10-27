package com.example.quietfeed.feature.channel

sealed interface ChannelEvent {
    data object ClearVideos : ChannelEvent

    data object DismissFeedback : ChannelEvent
}
