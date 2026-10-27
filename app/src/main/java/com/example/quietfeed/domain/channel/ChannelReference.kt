package com.example.quietfeed.domain.channel

const val CHANNEL_ID_PATTERN: String = "UC[A-Za-z0-9_-]{22}"

sealed interface ChannelReference {
    data class Handle(val handle: String) : ChannelReference

    data class Id(val id: String) : ChannelReference
}

object ChannelReferenceParser {
    private val channelId: Regex = Regex(CHANNEL_ID_PATTERN)
    private val channelPath: Regex = Regex("^channel/($CHANNEL_ID_PATTERN)(?:[/?#].*)?$")
    private val handlePath: Regex = Regex("^@([^/?#\\s]+)(?:[/?#].*)?$")
    private val youTubeUrl: Regex = Regex("^(?:https?://)?(?:www\\.|m\\.)?youtube\\.com/(.*)$", RegexOption.IGNORE_CASE)

    fun parse(input: String): ChannelReference? {
        val text = input.trim()
        if (channelId.matches(text)) return ChannelReference.Id(text)
        val path = youTubeUrl.matchEntire(text)?.groupValues?.get(1) ?: text
        channelPath.matchEntire(path)?.let { return ChannelReference.Id(it.groupValues[1]) }
        return handlePath.matchEntire(path)?.let { ChannelReference.Handle(it.groupValues[1]) }
    }
}
