package com.example.quietfeed.data.remote

import android.util.Xml
import com.example.quietfeed.common.attempt
import com.example.quietfeed.domain.channel.ChannelReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

private const val HTTP_NOT_FOUND: Int = 404
private const val HTTP_SERVER_ERROR: Int = 500
private const val HTTP_TOO_MANY_REQUESTS: Int = 429

private const val CHANNEL_URL: String = "https://www.youtube.com/channel/"
private const val CONSENT_COOKIE: String = "SOCS=CAI"
private const val FEED_URL: String = "https://www.youtube.com/feeds/videos.xml?channel_id="
private const val HANDLE_URL: String = "https://www.youtube.com/@"
private const val HEADER_COOKIE: String = "Cookie"
private const val SHORTS_URL: String = "https://www.youtube.com/shorts/"

class YouTubeClient(private val channelVideosReader: ChannelVideosReader, private val httpClient: OkHttpClient) {
    private val probeClient: OkHttpClient = httpClient.newBuilder().followRedirects(false).followSslRedirects(false).build()

    suspend fun fetchChannelId(reference: ChannelReference): String = withContext(Dispatchers.IO) {
        val url = when (reference) {
            is ChannelReference.Handle -> HANDLE_URL + reference.handle
            is ChannelReference.Id -> CHANNEL_URL + reference.id
        }
        httpClient.newCall(request(url)).execute().use { response ->
            if (response.code == HTTP_NOT_FOUND) throw ChannelNotFoundException()
            response.requireSuccess()
            ChannelPageParser.findChannelId(response.body.string()) ?: throw ChannelNotFoundException()
        }
    }

    suspend fun fetchLatestVideos(channelId: String): List<FeedEntry> = attempt { fetchFeed(channelId) }.getOrElse { channelVideosReader.fetchVideos(channelId) }

    suspend fun isShort(entry: FeedEntry): Boolean {
        if (entry.isShortLink) return true
        return withContext(Dispatchers.IO) {
            probeClient.newCall(request(SHORTS_URL + entry.videoId).newBuilder().head().build()).execute().use { response ->
                when {
                    response.isSuccessful -> true
                    response.code == HTTP_TOO_MANY_REQUESTS || response.code >= HTTP_SERVER_ERROR -> throw UnexpectedResponseException(response.code)
                    else -> false
                }
            }
        }
    }

    private suspend fun fetchFeed(channelId: String): List<FeedEntry> = withContext(Dispatchers.IO) {
        httpClient.newCall(request(FEED_URL + channelId)).execute().use { response ->
            response.requireSuccess()
            FeedParser.parse(parser = Xml.newPullParser(), reader = response.body.charStream())
        }
    }

    private fun request(url: String): Request = Request.Builder().url(url).header(HEADER_COOKIE, CONSENT_COOKIE).build()
}

private fun Response.requireSuccess() {
    if (!isSuccessful) throw UnexpectedResponseException(code)
}
