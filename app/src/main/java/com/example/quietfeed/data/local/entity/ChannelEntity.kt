package com.example.quietfeed.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(val addedAt: Long, @PrimaryKey val channelId: String, val name: String)
