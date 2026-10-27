package com.example.quietfeed.data.remote

import com.example.quietfeed.domain.channel.CHANNEL_ID_PATTERN

object ChannelPageParser {
    private val channelIdPatterns: List<Regex> = listOf(
        Regex("<link rel=\"canonical\" href=\"https?://(?:www\\.)?youtube\\.com/channel/($CHANNEL_ID_PATTERN)\""),
        Regex("<meta itemprop=\"identifier\" content=\"($CHANNEL_ID_PATTERN)\""),
        Regex("\"externalId\":\"($CHANNEL_ID_PATTERN)\""),
    )

    fun findChannelId(html: String): String? = channelIdPatterns.firstNotNullOfOrNull { pattern -> pattern.find(html)?.groupValues?.get(1) }
}
