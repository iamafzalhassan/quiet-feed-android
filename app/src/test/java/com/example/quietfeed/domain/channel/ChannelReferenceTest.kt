package com.example.quietfeed.domain.channel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val CHANNEL_ID: String = "UCBJycsmduvYEL83R_U4JriQ"

class ChannelReferenceTest {
    @Test
    fun parseAcceptsABareChannelId() {
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse(CHANNEL_ID))
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse("  $CHANNEL_ID  "))
    }

    @Test
    fun parseAcceptsAChannelUrlInEveryForm() {
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse("https://www.youtube.com/channel/$CHANNEL_ID"))
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse("http://youtube.com/channel/$CHANNEL_ID/videos"))
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse("m.youtube.com/channel/$CHANNEL_ID?si=abc"))
        assertEquals(ChannelReference.Id(CHANNEL_ID), ChannelReferenceParser.parse("HTTPS://WWW.YOUTUBE.COM/channel/$CHANNEL_ID"))
    }

    @Test
    fun parseAcceptsAHandleWithOrWithoutUrl() {
        assertEquals(ChannelReference.Handle("mkbhd"), ChannelReferenceParser.parse("@mkbhd"))
        assertEquals(ChannelReference.Handle("mkbhd"), ChannelReferenceParser.parse("https://www.youtube.com/@mkbhd"))
        assertEquals(ChannelReference.Handle("mkbhd"), ChannelReferenceParser.parse("youtube.com/@mkbhd/videos?view=0"))
        assertEquals(ChannelReference.Handle("some.name-1_x"), ChannelReferenceParser.parse("@some.name-1_x"))
    }

    @Test
    fun parseRejectsAnythingElse() {
        assertNull(ChannelReferenceParser.parse(""))
        assertNull(ChannelReferenceParser.parse("mkbhd"))
        assertNull(ChannelReferenceParser.parse("@"))
        assertNull(ChannelReferenceParser.parse("UCtooshort"))
        assertNull(ChannelReferenceParser.parse("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertNull(ChannelReferenceParser.parse("https://example.com/@mkbhd"))
        assertNull(ChannelReferenceParser.parse("https://www.youtube.com/channel/UCtooshort"))
    }
}
