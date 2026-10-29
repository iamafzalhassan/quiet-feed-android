package com.example.quietfeed.data.backup

import com.example.quietfeed.domain.channel.CHANNEL_ID_PATTERN
import com.example.quietfeed.domain.channel.Channel
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

private const val FORMAT_VERSION: Int = 2

private const val FORMAT_NAME: String = "QuietFeed"
private const val KEY_APP: String = "app"
private const val KEY_CHANNELS: String = "channels"
private const val KEY_ID: String = "id"
private const val KEY_NAME: String = "name"
private const val KEY_VERSION: String = "version"

private val channelId: Regex = Regex(CHANNEL_ID_PATTERN)

data class BackupChannel(val id: String, val name: String)

private fun JSONObject.toBackupChannel(): BackupChannel? {
    val id = optString(KEY_ID).trim()
    val name = optString(KEY_NAME).trim().take(Channel.MAX_NAME_LENGTH)
    return if (channelId.matches(id) && name.isNotBlank()) BackupChannel(id = id, name = name) else null
}

object ChannelBackup {
    fun decode(text: String): List<BackupChannel>? = try {
        val root = JSONObject(text)
        if (root.optString(KEY_APP) != FORMAT_NAME) {
            null
        } else {
            val channels = root.getJSONArray(KEY_CHANNELS)
            (0 until channels.length()).mapNotNull { index -> channels.optJSONObject(index)?.toBackupChannel() }.distinctBy { channel -> channel.id }
        }
    } catch (exception: JSONException) {
        null
    }

    fun encode(channels: List<BackupChannel>): String {
        val entries = JSONArray()
        channels.forEach { channel -> entries.put(JSONObject().put(KEY_ID, channel.id).put(KEY_NAME, channel.name)) }
        return JSONObject()
            .put(KEY_APP, FORMAT_NAME)
            .put(KEY_VERSION, FORMAT_VERSION)
            .put(KEY_CHANNELS, entries)
            .toString(2)
    }
}
