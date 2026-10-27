package com.example.quietfeed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.quietfeed.data.local.dao.ChannelDao
import com.example.quietfeed.data.local.dao.VideoDao
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.VideoEntity

private const val DATABASE_FILE_NAME: String = "quietfeed.db"

@Database(entities = [ChannelEntity::class, VideoEntity::class], version = 1)
abstract class QuietFeedDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao

    abstract fun videoDao(): VideoDao
}

fun buildQuietFeedDatabase(context: Context): QuietFeedDatabase = Room.databaseBuilder(context, QuietFeedDatabase::class.java, DATABASE_FILE_NAME).build()
