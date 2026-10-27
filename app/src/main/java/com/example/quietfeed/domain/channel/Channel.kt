package com.example.quietfeed.domain.channel

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Channel(val hasNewVideos: Boolean, val id: String, val name: String, val addedAt: Instant) {
    companion object {
        const val MAX_NAME_LENGTH: Int = 50
    }
}
