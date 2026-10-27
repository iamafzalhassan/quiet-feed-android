package com.example.quietfeed.data.remote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FeedEntryTest {
    @Test
    fun isShortLinkIsFalseForAWatchLink() {
        assertFalse(entry("https://www.youtube.com/watch?v=aaaaaaaaaaa").isShortLink)
    }

    @Test
    fun isShortLinkIsTrueForAShortsLink() {
        assertTrue(entry("https://www.youtube.com/shorts/aaaaaaaaaaa").isShortLink)
    }

    private fun entry(link: String): FeedEntry = FeedEntry(link = link, title = "Title", videoId = "aaaaaaaaaaa", publishedAt = Instant.EPOCH)
}
