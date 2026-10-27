package com.example.quietfeed.data.sync

import com.example.quietfeed.common.attemptOrNull
import com.example.quietfeed.common.failureOf
import com.example.quietfeed.data.local.dao.ChannelDao
import com.example.quietfeed.data.local.dao.VideoDao
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.VideoEntity
import com.example.quietfeed.data.remote.FeedEntry
import com.example.quietfeed.data.remote.YouTubeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class FeedSync(private val channelDao: ChannelDao, private val scope: CoroutineScope, private val videoDao: VideoDao, private val youTubeClient: YouTubeClient) {
    private val mutex: Mutex = Mutex()

    private var running: Deferred<SyncResult>? = null

    fun requestChannelSync(channelId: String) {
        scope.launch { syncChannels(listOfNotNull(channelDao.findChannel(channelId))) }
    }

    suspend fun syncAll(): SyncResult = mutex.withLock { running?.takeIf { it.isActive } ?: scope.async { syncChannels(channelDao.findChannels()) }.also { running = it } }.await()

    private suspend fun syncChannels(channels: List<ChannelEntity>): SyncResult {
        val failures = channels.mapNotNull { channel -> failureOf { syncChannel(channel) } }
        return when {
            failures.isEmpty() -> SyncResult.SYNCED
            failures.size == channels.size && failures.all { it is IOException } -> SyncResult.OFFLINE
            else -> SyncResult.PARTIAL
        }
    }

    private suspend fun syncChannel(channel: ChannelEntity) {
        val entries = youTubeClient.fetchFeed(channel.channelId)
        val knownIds = videoDao.findVideoIds(channel.channelId).toSet()
        val videos = entries.newSince(addedAtMillis = channel.addedAt, knownIds = knownIds).mapNotNull { entry ->
            attemptOrNull { youTubeClient.isShort(entry) }?.let { isShort -> entry.toVideoEntity(channelId = channel.channelId, isShort = isShort) }
        }
        videoDao.insertVideos(videos)
    }
}

private fun FeedEntry.toVideoEntity(isShort: Boolean, channelId: String): VideoEntity = VideoEntity(
    isSeen = false,
    isShort = isShort,
    publishedAt = publishedAt.toEpochMilli(),
    channelId = channelId,
    title = title,
    url = link,
    videoId = videoId,
)
