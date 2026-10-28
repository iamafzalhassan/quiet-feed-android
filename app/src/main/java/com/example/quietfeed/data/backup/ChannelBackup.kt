package com.example.quietfeed.data.backup

import com.example.quietfeed.domain.channel.CHANNEL_ID_PATTERN
import com.example.quietfeed.domain.channel.Channel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant
import java.time.format.DateTimeParseException

private const val FORMAT_VERSION: Int = 1

private const val FORMAT_NAME: String = "QuietFeed"
private const val KEY_ADDED_AT: String = "addedAt"
private const val KEY_APP: String = "app"
private const val KEY_CHANNELS: String = "channels"
private const val KEY_EXPORTED_AT: String = "exportedAt"
private const val KEY_ID: String = "id"
private const val KEY_NAME: String = "name"
private const val KEY_VERSION: String = "version"

private val channelId: Regex = Regex(CHANNEL_ID_PATTERN)

data class BackupChannel(val id: String, val name: String, val addedAt: Instant)

private fun JSONObject.toBackupChannel(importedAt: Instant): BackupChannel? {
    val id = optString(KEY_ID).trim()
    val name = optString(KEY_NAME).trim().take(Channel.MAX_NAME_LENGTH)
    if (!channelId.matches(id) || name.isBlank()) return null
    val addedAt = try {
        Instant.parse(optString(KEY_ADDED_AT))
    } catch (exception: DateTimeParseException) {
        importedAt
    }
    return BackupChannel(id = id, name = name, addedAt = addedAt)
}

object ChannelBackup {
    fun decode(text: String, importedAt: Instant): List<BackupChannel>? = try {
        val root = JSONObject(text)
        if (root.optString(KEY_APP) != FORMAT_NAME) {
            null
        } else {
            val channels = root.getJSONArray(KEY_CHANNELS)
            (0 until channels.length()).mapNotNull { index -> channels.optJSONObject(index)?.toBackupChannel(importedAt) }.distinctBy { channel -> channel.id }
        }
    } catch (exception: JSONException) {
        null
    }

    fun encode(channels: List<BackupChannel>, exportedAt: Instant): String {
        val entries = JSONArray()
        channels.forEach { channel -> entries.put(JSONObject().put(KEY_ID, channel.id).put(KEY_NAME, channel.name).put(KEY_ADDED_AT, channel.addedAt.toString())) }
        return JSONObject()
            .put(KEY_APP, FORMAT_NAME)
            .put(KEY_VERSION, FORMAT_VERSION)
            .put(KEY_EXPORTED_AT, exportedAt.toString())
            .put(KEY_CHANNELS, entries)
            .toString(2)
    }
}
