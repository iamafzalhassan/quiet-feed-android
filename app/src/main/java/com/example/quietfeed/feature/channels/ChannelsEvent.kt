package com.example.quietfeed.feature.channels

sealed interface ChannelsEvent {
    data class AddChannel(val input: String, val name: String) : ChannelsEvent

    data object ConsumeAddedChannel : ChannelsEvent

    data object DismissAddError : ChannelsEvent

    data object DismissFeedback : ChannelsEvent

    data object Refresh : ChannelsEvent

    data class RemoveChannel(val channelId: String) : ChannelsEvent

    data class RenameChannel(val channelId: String, val name: String) : ChannelsEvent
}
