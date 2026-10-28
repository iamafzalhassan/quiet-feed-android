package com.example.quietfeed.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

private const val CHANNEL_ID: String = "UCpqXJOEqGS-TCnazcHCo0rA"
private const val OTHER_CHANNEL_ID: String = "UCBJycsmduvYEL83R_U4JriQ"

class ChannelBackupTest {
    private val addedAt: Instant = Instant.parse("2026-10-26T08:30:00Z")
    private val now: Instant = Instant.parse("2026-10-28T12:00:00Z")

    @Test
    fun decodeDropsInvalidAndDuplicateChannels() {
        val text = """{"app":"QuietFeed","channels":[{"id":"$CHANNEL_ID","name":"Brad"},{"id":"$CHANNEL_ID","name":"Again"},{"id":"UCtooshort","name":"Bad"},{"id":"$OTHER_CHANNEL_ID","name":"  "}]}"""

        assertEquals(listOf(BackupChannel(id = CHANNEL_ID, name = "Brad", addedAt = now)), ChannelBackup.decode(importedAt = now, text = text))
    }

    @Test
    fun decodeRejectsAnythingThatIsNotAQuietFeedBackup() {
        assertNull(ChannelBackup.decode(importedAt = now, text = "not json"))
        assertNull(ChannelBackup.decode(importedAt = now, text = """{"app":"Other","channels":[]}"""))
        assertNull(ChannelBackup.decode(importedAt = now, text = """{"app":"QuietFeed"}"""))
    }

    @Test
    fun decodeUsesTheImportTimeWhenTheAddedTimeIsMissing() {
        val text = """{"app":"QuietFeed","channels":[{"id":"$CHANNEL_ID","name":"Brad","addedAt":"yesterday"}]}"""

        assertEquals(now, ChannelBackup.decode(importedAt = now, text = text)?.single()?.addedAt)
    }

    @Test
    fun encodeAndDecodeRoundTrip() {
        val channels = listOf(BackupChannel(id = CHANNEL_ID, name = "theRadBrad", addedAt = addedAt), BackupChannel(id = OTHER_CHANNEL_ID, name = "MKBHD", addedAt = now))

        assertEquals(channels, ChannelBackup.decode(importedAt = now, text = ChannelBackup.encode(channels = channels, exportedAt = now)))
    }
}
