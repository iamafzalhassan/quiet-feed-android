package com.example.quietfeed.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.local.entity.ChannelSummaryRow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ChannelDao {
    @Query("DELETE FROM channels WHERE channelId = :channelId")
    abstract suspend fun deleteChannel(channelId: String)

    @Query("SELECT * FROM channels WHERE channelId = :channelId")
    abstract suspend fun findChannel(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channels")
    abstract suspend fun findChannels(): List<ChannelEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertChannel(channel: ChannelEntity): Long

    @Query("SELECT name FROM channels WHERE channelId = :channelId")
    abstract fun observeChannelName(channelId: String): Flow<String?>

    @Query(
        "SELECT c.addedAt, c.channelId, c.name, EXISTS (SELECT 1 FROM videos v WHERE v.channelId = c.channelId AND v.isSeen = 0 AND v.isShort = 0 AND v.publishedAt > c.addedAt) AS hasNewVideos " +
            "FROM channels c ORDER BY hasNewVideos DESC, c.name COLLATE NOCASE ASC",
    )
    abstract fun observeChannelSummaries(): Flow<List<ChannelSummaryRow>>

    @Query("UPDATE channels SET name = :name WHERE channelId = :channelId")
    abstract suspend fun renameChannel(channelId: String, name: String)
}
