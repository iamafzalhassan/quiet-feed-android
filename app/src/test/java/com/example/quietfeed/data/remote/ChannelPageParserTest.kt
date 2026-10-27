package com.example.quietfeed.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val CHANNEL_ID: String = "UCpqXJOEqGS-TCnazcHCo0rA"
private const val OTHER_CHANNEL_ID: String = "UCXuqSBlHAE6Xw-yeJA0Tunw"

class ChannelPageParserTest {
    @Test
    fun findChannelIdFallsBackToTheIdentifierMetaThenExternalId() {
        assertEquals(CHANNEL_ID, ChannelPageParser.findChannelId("""<meta itemprop="identifier" content="$CHANNEL_ID">"""))
        assertEquals(CHANNEL_ID, ChannelPageParser.findChannelId("""{"channelId":"$OTHER_CHANNEL_ID","externalId":"$CHANNEL_ID"}"""))
    }

    @Test
    fun findChannelIdIgnoresAnEchoedChannelId() {
        assertNull(ChannelPageParser.findChannelId("""<html><script>{"channelId":"$CHANNEL_ID"}</script></html>"""))
    }

    @Test
    fun findChannelIdPrefersTheCanonicalLink() {
        val html = """<html><head><link rel="canonical" href="https://www.youtube.com/channel/$CHANNEL_ID"></head><script>{"externalId":"$OTHER_CHANNEL_ID"}</script></html>"""

        assertEquals(CHANNEL_ID, ChannelPageParser.findChannelId(html))
    }

    @Test
    fun findChannelIdReturnsNullWithoutAnId() {
        assertNull(ChannelPageParser.findChannelId("<html><body>Not found</body></html>"))
        assertNull(ChannelPageParser.findChannelId("""<link rel="canonical" href="https://www.youtube.com/channel/UCtooshort">"""))
    }
}
