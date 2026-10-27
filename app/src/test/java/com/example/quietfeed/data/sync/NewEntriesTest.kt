package com.example.quietfeed.data.sync

import com.example.quietfeed.data.remote.FeedEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class NewEntriesTest {
    private val addedAt: Instant = Instant.parse("2026-10-20T12:00:00Z")

    @Test
    fun newSinceDropsDuplicateIdsWithinOneFeed() {
        val entries = listOf(entry(id = "a", publishedAt = addedAt.plusSeconds(60)), entry(id = "a", publishedAt = addedAt.plusSeconds(120)))

        assertEquals(listOf("a"), entries.newSince(addedAtMillis = addedAt.toEpochMilli(), knownIds = emptySet()).map { entry -> entry.videoId })
    }

    @Test
    fun newSinceDropsVideosAlreadyStored() {
        val entries = listOf(entry(id = "a", publishedAt = addedAt.plusSeconds(60)), entry(id = "b", publishedAt = addedAt.plusSeconds(120)))

        assertEquals(listOf("b"), entries.newSince(addedAtMillis = addedAt.toEpochMilli(), knownIds = setOf("a")).map { entry -> entry.videoId })
    }

    @Test
    fun newSinceKeepsOnlyVideosPublishedAfterTheChannelWasAdded() {
        val entries = listOf(
            entry(id = "before", publishedAt = addedAt.minusSeconds(1)),
            entry(id = "exactly", publishedAt = addedAt),
            entry(id = "after", publishedAt = addedAt.plusMillis(1)),
        )

        assertEquals(listOf("after"), entries.newSince(addedAtMillis = addedAt.toEpochMilli(), knownIds = emptySet()).map { entry -> entry.videoId })
    }

    @Test
    fun newSinceReturnsNothingForAnEmptyFeed() {
        assertTrue(emptyList<FeedEntry>().newSince(addedAtMillis = addedAt.toEpochMilli(), knownIds = emptySet()).isEmpty())
    }

    private fun entry(id: String, publishedAt: Instant): FeedEntry = FeedEntry(link = "https://www.youtube.com/watch?v=$id", title = id, videoId = id, publishedAt = publishedAt)
}
