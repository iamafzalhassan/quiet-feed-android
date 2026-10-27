package com.example.quietfeed.data.repository

import com.example.quietfeed.data.local.entity.VideoEntity
import com.example.quietfeed.domain.video.Video
import java.time.Instant

fun VideoEntity.toVideo(): Video = Video(isSeen = isSeen, channelId = channelId, id = videoId, title = title, url = url, publishedAt = Instant.ofEpochMilli(publishedAt))
