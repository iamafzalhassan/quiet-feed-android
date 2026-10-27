package com.example.quietfeed.data.remote

import java.time.Instant

data class FeedEntry(val link: String, val title: String, val videoId: String, val publishedAt: Instant) {
    companion object {
        const val SHORTS_PATH: String = "/shorts/"
    }

    val isShortLink: Boolean get() = SHORTS_PATH in link
}
