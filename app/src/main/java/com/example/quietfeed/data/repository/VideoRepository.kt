package com.example.quietfeed.data.repository

import com.example.quietfeed.data.local.dao.VideoDao
import com.example.quietfeed.domain.video.Video
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VideoRepository(private val videoDao: VideoDao) {
    suspend fun markSeen(channelId: String) = videoDao.markSeen(channelId)

    fun observeVideos(channelId: String): Flow<List<Video>> = videoDao.observeVideos(channelId).map { entities -> entities.map { entity -> entity.toVideo() } }
}
