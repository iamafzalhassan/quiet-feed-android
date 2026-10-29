package com.example.quietfeed.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.DateWrapper
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import java.time.Instant

private const val VIDEO_ID: String = "dQw4w9WgXcQ"

class ChannelVideosReaderTest {
    private val publishedAt: Instant = Instant.parse("2026-10-27T10:00:00Z")

    @Test
    fun toFeedEntryKeepsARegularVideo() {
        val entry = item(url = "https://www.youtube.com/watch?v=$VIDEO_ID&t=5").toFeedEntry()

        assertEquals(FeedEntry(link = "https://www.youtube.com/watch?v=$VIDEO_ID", title = "Title", videoId = VIDEO_ID, publishedAt = publishedAt), entry)
    }

    @Test
    fun toFeedEntrySkipsAnUrlWithoutAVideoId() {
        assertNull(item(url = "https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ").toFeedEntry())
    }

    @Test
    fun toFeedEntrySkipsAShort() {
        val item = item(url = "https://www.youtube.com/watch?v=$VIDEO_ID")
        item.isShortFormContent = true

        assertNull(item.toFeedEntry())
    }

    @Test
    fun toFeedEntrySkipsAVideoWithoutADate() {
        val item = item(url = "https://www.youtube.com/watch?v=$VIDEO_ID")
        item.uploadDate = null

        assertNull(item.toFeedEntry())
    }

    @Test
    fun uploadedWithinADayDropsOlderApproximateDates() {
        val now = publishedAt.plusSeconds(23 * 60 * 60)
        val recent = FeedEntry(link = "https://www.youtube.com/watch?v=$VIDEO_ID", title = "Recent", videoId = VIDEO_ID, publishedAt = publishedAt)
        val old = FeedEntry(link = "https://www.youtube.com/watch?v=aaaaaaaaaaa", title = "Old", videoId = "aaaaaaaaaaa", publishedAt = publishedAt.minusSeconds(2 * 60 * 60))

        assertEquals(listOf(recent), listOf(recent, old).uploadedWithinADay(now))
    }

    private fun item(url: String): StreamInfoItem {
        val item = StreamInfoItem(ServiceList.YouTube.serviceId, url, "Title", StreamType.VIDEO_STREAM)
        item.uploadDate = DateWrapper(publishedAt, true)
        return item
    }
}
