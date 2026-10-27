package com.example.quietfeed.feature.channels

sealed interface ChannelsFeedback {
    data class ChannelAdded(val name: String) : ChannelsFeedback

    data class ChannelRemoved(val name: String) : ChannelsFeedback

    data class ChannelRenamed(val name: String) : ChannelsFeedback

    data object RefreshFailed : ChannelsFeedback

    data object RefreshOffline : ChannelsFeedback
}
