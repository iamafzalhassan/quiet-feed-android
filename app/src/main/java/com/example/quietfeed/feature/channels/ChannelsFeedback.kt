package com.example.quietfeed.feature.channels

sealed interface ChannelsFeedback {
    data class ChannelAdded(val name: String) : ChannelsFeedback

    data class ChannelRemoved(val name: String) : ChannelsFeedback

    data class ChannelRenamed(val name: String) : ChannelsFeedback

    data class ChannelsExported(val count: Int) : ChannelsFeedback

    data class ChannelsImported(val count: Int) : ChannelsFeedback

    data object ExportFailed : ChannelsFeedback

    data object ImportFailed : ChannelsFeedback

    data object ImportInvalid : ChannelsFeedback

    data object SyncFailed : ChannelsFeedback

    data object SyncOffline : ChannelsFeedback
}
