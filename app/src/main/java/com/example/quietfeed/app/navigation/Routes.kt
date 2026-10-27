package com.example.quietfeed.app.navigation

object Routes {
    const val ARG_CHANNEL_ID: String = "channelId"
    const val CHANNEL: String = "channel/{$ARG_CHANNEL_ID}"
    const val CHANNELS: String = "channels"

    fun channel(channelId: String): String = "channel/$channelId"
}
