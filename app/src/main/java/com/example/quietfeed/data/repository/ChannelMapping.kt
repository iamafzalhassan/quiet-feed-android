package com.example.quietfeed.data.repository

import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.ChannelSummaryRow
import com.example.quietfeed.domain.channel.Channel
import java.time.Instant

fun ChannelEntity.toChannel(): Channel = Channel(hasNewVideos = false, id = channelId, name = name, addedAt = Instant.ofEpochMilli(addedAt))

fun ChannelSummaryRow.toChannel(): Channel = Channel(hasNewVideos = hasNewVideos, id = channelId, name = name, addedAt = Instant.ofEpochMilli(addedAt))
