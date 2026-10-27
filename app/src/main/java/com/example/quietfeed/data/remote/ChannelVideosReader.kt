package com.example.quietfeed.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabInfo
import org.schabi.newpipe.extractor.channel.tabs.ChannelTabs
import org.schabi.newpipe.extractor.stream.StreamInfoItem

private const val CHANNEL_URL: String = "https://www.youtube.com/channel/"
private const val WATCH_URL: String = "https://www.youtube.com/watch?v="

private val videoIdPattern: Regex = Regex("[?&]v=([A-Za-z0-9_-]{11})")

class ChannelVideosReader {
    suspend fun fetchVideos(channelId: String): List<FeedEntry> = withContext(Dispatchers.IO) {
        val channel = ChannelInfo.getInfo(ServiceList.YouTube, CHANNEL_URL + channelId)
        val videosTab = channel.tabs.firstOrNull { tab -> ChannelTabs.VIDEOS in tab.contentFilters } ?: return@withContext emptyList()
        ChannelTabInfo.getInfo(ServiceList.YouTube, videosTab).relatedItems.filterIsInstance<StreamInfoItem>().mapNotNull { item -> item.toFeedEntry() }
    }
}

fun StreamInfoItem.toFeedEntry(): FeedEntry? {
    if (isShortFormContent) return null
    val videoId = videoIdPattern.find(url)?.groupValues?.get(1) ?: return null
    val publishedAt = uploadDate?.instant ?: return null
    return FeedEntry(link = WATCH_URL + videoId, title = name, videoId = videoId, publishedAt = publishedAt)
}
