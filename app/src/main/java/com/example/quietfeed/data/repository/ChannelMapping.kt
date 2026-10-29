package com.example.quietfeed.data.repository

import com.example.quietfeed.data.backup.BackupChannel
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.ChannelSummaryRow
import com.example.quietfeed.domain.channel.Channel
import java.time.Instant

fun BackupChannel.toChannelEntity(addedAt: Instant): ChannelEntity = ChannelEntity(addedAt = addedAt.toEpochMilli(), channelId = id, name = name)

fun ChannelEntity.toBackupChannel(): BackupChannel = BackupChannel(id = channelId, name = name)

fun ChannelEntity.toChannel(): Channel = Channel(hasNewVideos = false, id = channelId, name = name, addedAt = Instant.ofEpochMilli(addedAt))

fun ChannelSummaryRow.toChannel(): Channel = Channel(hasNewVideos = hasNewVideos, id = channelId, name = name, addedAt = Instant.ofEpochMilli(addedAt))
