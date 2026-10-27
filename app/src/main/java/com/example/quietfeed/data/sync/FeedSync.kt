package com.example.quietfeed.data.sync

import com.example.quietfeed.common.attempt
import com.example.quietfeed.common.attemptOrNull
import com.example.quietfeed.data.local.dao.ChannelDao
import com.example.quietfeed.data.local.dao.VideoDao
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.VideoEntity
import com.example.quietfeed.data.remote.FeedEntry
import com.example.quietfeed.data.remote.YouTubeClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import java.io.IOException

private const val MAX_PARALLEL_SYNCS: Int = 4

private const val NOT_INSERTED: Long = -1L

class FeedSync(private val channelDao: ChannelDao, private val scope: CoroutineScope, private val videoDao: VideoDao, private val youTubeClient: YouTubeClient) {
    private val mutex: Mutex = Mutex()

    private val syncPermits: Semaphore = Semaphore(MAX_PARALLEL_SYNCS)

    private var running: Deferred<SyncReport>? = null

    fun requestChannelSync(channelId: String) {
        scope.launch { syncChannels(listOfNotNull(channelDao.findChannel(channelId))) }
    }

    suspend fun syncAll(): SyncReport = mutex.withLock { running?.takeIf { it.isActive } ?: scope.async { syncChannels(channelDao.findChannels()) }.also { running = it } }.await()

    private suspend fun syncChannels(channels: List<ChannelEntity>): SyncReport {
        val outcomes = coroutineScope { channels.map { channel -> async { syncPermits.withPermit { attempt { syncChannel(channel) } } } }.awaitAll() }
        val failures = outcomes.mapNotNull { outcome -> outcome.exceptionOrNull() }
        val result = when {
            failures.isEmpty() -> SyncResult.SYNCED
            failures.size == channels.size && failures.all { it is IOException } -> SyncResult.OFFLINE
            else -> SyncResult.PARTIAL
        }
        return SyncReport(uploads = outcomes.mapNotNull { outcome -> outcome.getOrNull() }.filter { uploads -> uploads.titles.isNotEmpty() }, result = result)
    }

    private suspend fun syncChannel(channel: ChannelEntity): ChannelUploads {
        val entries = youTubeClient.fetchLatestVideos(channel.channelId)
        val knownIds = videoDao.findVideoIds(channel.channelId).toSet()
        val videos = entries.newSince(addedAtMillis = channel.addedAt, knownIds = knownIds).mapNotNull { entry ->
            attemptOrNull { youTubeClient.isShort(entry) }?.let { isShort -> entry.toVideoEntity(channelId = channel.channelId, isShort = isShort) }
        }
        val rowIds = videoDao.insertVideos(videos)
        val titles = videos.zip(rowIds).filter { (video, rowId) -> rowId != NOT_INSERTED && !video.isShort }.map { (video, _) -> video.title }
        return ChannelUploads(channelId = channel.channelId, channelName = channel.name, titles = titles)
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
