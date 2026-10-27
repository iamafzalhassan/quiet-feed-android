package com.example.quietfeed.domain.video

import androidx.compose.runtime.Immutable
import java.time.Instant

@Immutable
data class Video(val isSeen: Boolean, val channelId: String, val id: String, val title: String, val url: String, val publishedAt: Instant)
