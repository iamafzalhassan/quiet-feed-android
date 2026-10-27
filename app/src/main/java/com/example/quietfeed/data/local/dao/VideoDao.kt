package com.example.quietfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.quietfeed.data.local.entity.VideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class VideoDao {
    @Query("DELETE FROM videos WHERE channelId = :channelId")
    abstract suspend fun deleteVideos(channelId: String)

    @Query("SELECT videoId FROM videos WHERE channelId = :channelId")
    abstract suspend fun findVideoIds(channelId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertVideos(videos: List<VideoEntity>): List<Long>

    @Query("UPDATE videos SET isSeen = 1 WHERE channelId = :channelId AND isSeen = 0")
    abstract suspend fun markSeen(channelId: String)

    @Query("UPDATE channels SET addedAt = :cutoff WHERE channelId = :channelId")
    abstract suspend fun moveCutoff(cutoff: Long, channelId: String)

    @Query("SELECT v.* FROM videos v INNER JOIN channels c ON c.channelId = v.channelId WHERE v.channelId = :channelId AND v.isShort = 0 AND v.publishedAt > c.addedAt ORDER BY v.publishedAt DESC")
    abstract fun observeVideos(channelId: String): Flow<List<VideoEntity>>

    @Transaction
    open suspend fun clearVideos(clearedAt: Long, channelId: String) {
        deleteVideos(channelId)
        moveCutoff(channelId = channelId, cutoff = clearedAt)
    }
}
