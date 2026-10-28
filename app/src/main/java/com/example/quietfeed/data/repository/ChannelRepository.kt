package com.example.quietfeed.data.repository

import com.example.quietfeed.data.backup.BackupFiles
import com.example.quietfeed.data.backup.ChannelBackup
import com.example.quietfeed.data.local.dao.ChannelDao
import com.example.quietfeed.data.local.entity.ChannelEntity
import com.example.quietfeed.data.remote.ChannelNotFoundException
import com.example.quietfeed.data.remote.YouTubeClient
import com.example.quietfeed.domain.channel.AddChannelError
import com.example.quietfeed.domain.channel.AddChannelResult
import com.example.quietfeed.domain.channel.Channel
import com.example.quietfeed.domain.channel.ChannelReference
import com.example.quietfeed.domain.channel.ChannelReferenceParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.Instant

class ChannelRepository(private val backupFiles: BackupFiles, private val channelDao: ChannelDao, private val youTubeClient: YouTubeClient) {
    suspend fun addChannel(input: String, name: String): AddChannelResult {
        val reference = ChannelReferenceParser.parse(input) ?: return AddChannelResult.Rejected(AddChannelError.INVALID_INPUT)
        if (reference is ChannelReference.Id && channelDao.findChannel(reference.id) != null) return AddChannelResult.Rejected(AddChannelError.ALREADY_ADDED)
        return try {
            val channelId = youTubeClient.fetchChannelId(reference)
            val channel = ChannelEntity(addedAt = Instant.now().toEpochMilli(), channelId = channelId, name = name.trim().take(Channel.MAX_NAME_LENGTH))
            if (channelDao.insertChannel(channel) == -1L) AddChannelResult.Rejected(AddChannelError.ALREADY_ADDED) else AddChannelResult.Added(channel.toChannel())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: ChannelNotFoundException) {
            AddChannelResult.Rejected(AddChannelError.NOT_FOUND)
        } catch (exception: IOException) {
            AddChannelResult.Rejected(AddChannelError.OFFLINE)
        } catch (exception: Exception) {
            AddChannelResult.Rejected(AddChannelError.FAILED)
        }
    }

    suspend fun deleteChannel(channelId: String) = channelDao.deleteChannel(channelId)

    suspend fun exportChannels(uri: String): Int {
        val channels = channelDao.findChannels()
        backupFiles.write(text = ChannelBackup.encode(channels = channels.map { channel -> channel.toBackupChannel() }, exportedAt = Instant.now()), uri = uri)
        return channels.size
    }

    suspend fun importChannels(uri: String): Int? {
        val channels = ChannelBackup.decode(importedAt = Instant.now(), text = backupFiles.read(uri)) ?: return null
        return channels.count { channel -> channelDao.insertChannel(channel.toChannelEntity()) != -1L }
    }

    fun observeChannelName(channelId: String): Flow<String?> = channelDao.observeChannelName(channelId)

    fun observeChannels(): Flow<List<Channel>> = channelDao.observeChannelSummaries().map { rows -> rows.map { row -> row.toChannel() } }

    suspend fun renameChannel(channelId: String, name: String) = channelDao.renameChannel(channelId = channelId, name = name.trim().take(Channel.MAX_NAME_LENGTH))
}
