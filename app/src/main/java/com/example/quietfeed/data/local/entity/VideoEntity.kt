package com.example.quietfeed.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(childColumns = ["channelId"], entity = ChannelEntity::class, onDelete = ForeignKey.CASCADE, parentColumns = ["channelId"])],
    indices = [Index(value = ["channelId"])],
    tableName = "videos",
)
data class VideoEntity(
    val isSeen: Boolean,
    val isShort: Boolean,
    val publishedAt: Long,
    val channelId: String,
    val title: String,
    val url: String,
    @PrimaryKey val videoId: String,
)
