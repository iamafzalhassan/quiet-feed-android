package com.example.quietfeed.app

import android.content.Context
import com.example.quietfeed.data.local.QuietFeedDatabase
import com.example.quietfeed.data.local.buildQuietFeedDatabase
import com.example.quietfeed.data.remote.ChannelVideosReader
import com.example.quietfeed.data.remote.NewPipeDownloader
import com.example.quietfeed.data.remote.YouTubeClient
import com.example.quietfeed.data.repository.ChannelRepository
import com.example.quietfeed.data.repository.VideoRepository
import com.example.quietfeed.data.sync.FeedSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import java.util.concurrent.TimeUnit

private const val CALL_TIMEOUT_SECONDS: Long = 20L

class AppGraph(context: Context) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val httpClient: OkHttpClient = OkHttpClient.Builder().callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS).build()

    private val database: QuietFeedDatabase = buildQuietFeedDatabase(context.applicationContext)

    private val youTubeClient: YouTubeClient = YouTubeClient(channelVideosReader = ChannelVideosReader(), httpClient = httpClient)

    val channelRepository: ChannelRepository by lazy { ChannelRepository(channelDao = database.channelDao(), youTubeClient = youTubeClient) }

    val feedSync: FeedSync by lazy { FeedSync(channelDao = database.channelDao(), scope = scope, videoDao = database.videoDao(), youTubeClient = youTubeClient) }

    val videoRepository: VideoRepository by lazy { VideoRepository(database.videoDao()) }

    init {
        NewPipe.init(NewPipeDownloader(httpClient))
    }
}
