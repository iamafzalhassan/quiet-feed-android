package com.example.quietfeed.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

private const val CHANNEL_ID: String = "UCpqXJOEqGS-TCnazcHCo0rA"
private const val OTHER_CHANNEL_ID: String = "UCBJycsmduvYEL83R_U4JriQ"

class ChannelBackupTest {
    @Test
    fun decodeDropsInvalidAndDuplicateChannels() {
        val text = """{"app":"QuietFeed","channels":[{"id":"$CHANNEL_ID","name":"Brad"},{"id":"$CHANNEL_ID","name":"Again"},{"id":"UCtooshort","name":"Bad"},{"id":"$OTHER_CHANNEL_ID","name":"  "}]}"""

        assertEquals(listOf(BackupChannel(id = CHANNEL_ID, name = "Brad")), ChannelBackup.decode(text))
    }

    @Test
    fun decodeIgnoresTimesInOlderFiles() {
        val text = """{"app":"QuietFeed","version":1,"exportedAt":"2026-10-28T12:00:00Z","channels":[{"id":"$CHANNEL_ID","name":"Brad","addedAt":"2026-10-26T08:30:00Z"}]}"""

        assertEquals(listOf(BackupChannel(id = CHANNEL_ID, name = "Brad")), ChannelBackup.decode(text))
    }

    @Test
    fun decodeRejectsAnythingThatIsNotAQuietFeedBackup() {
        assertNull(ChannelBackup.decode("not json"))
        assertNull(ChannelBackup.decode("""{"app":"Other","channels":[]}"""))
        assertNull(ChannelBackup.decode("""{"app":"QuietFeed"}"""))
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val channels = listOf(BackupChannel(id = CHANNEL_ID, name = "theRadBrad"), BackupChannel(id = OTHER_CHANNEL_ID, name = "MKBHD"))

        assertEquals(channels, ChannelBackup.decode(ChannelBackup.encode(channels)))
    }

    @Test
    fun encodeWritesNoTimes() {
        val text = ChannelBackup.encode(listOf(BackupChannel(id = CHANNEL_ID, name = "theRadBrad")))

        assertFalse(text.contains("addedAt"))
        assertFalse(text.contains("exportedAt"))
    }
}
