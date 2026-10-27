package com.example.quietfeed.domain.channel

sealed interface AddChannelResult {
    data class Added(val channel: Channel) : AddChannelResult

    data class Rejected(val error: AddChannelError) : AddChannelResult
}
