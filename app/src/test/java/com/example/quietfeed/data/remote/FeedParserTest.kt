package com.example.quietfeed.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test
import org.kxml2.io.KXmlParser
import java.io.StringReader
import java.time.Instant

private const val FEED: String = """<?xml version="1.0" encoding="UTF-8"?>
<feed xmlns:yt="http://www.youtube.com/xml/schemas/2015" xmlns:media="http://search.yahoo.com/mrss/" xmlns="http://www.w3.org/2005/Atom">
 <link rel="self" href="http://www.youtube.com/feeds/videos.xml?channel_id=UCBJycsmduvYEL83R_U4JriQ"/>
 <id>yt:channel:BJycsmduvYEL83R_U4JriQ</id>
 <yt:channelId>BJycsmduvYEL83R_U4JriQ</yt:channelId>
 <title> Marques Brownlee </title>
 <link rel="alternate" href="https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ"/>
 <author>
  <name>Marques Brownlee</name>
  <uri>https://www.youtube.com/channel/UCBJycsmduvYEL83R_U4JriQ</uri>
 </author>
 <published>2008-03-21T15:25:54+00:00</published>
 <entry>
  <id>yt:video:aaaaaaaaaaa</id>
  <yt:videoId>aaaaaaaaaaa</yt:videoId>
  <yt:channelId>UCBJycsmduvYEL83R_U4JriQ</yt:channelId>
  <title>A long video</title>
  <link rel="alternate" href="https://www.youtube.com/watch?v=aaaaaaaaaaa"/>
  <author>
   <name>Marques Brownlee</name>
  </author>
  <published>2026-10-25T14:00:06+00:00</published>
  <updated>2026-10-26T02:11:15+00:00</updated>
  <media:group>
   <media:title>Media title that must be ignored</media:title>
   <media:thumbnail url="https://i1.ytimg.com/vi/aaaaaaaaaaa/hqdefault.jpg" width="480" height="360"/>
   <media:description>Description</media:description>
  </media:group>
 </entry>
 <entry>
  <id>yt:video:bbbbbbbbbbb</id>
  <yt:videoId>bbbbbbbbbbb</yt:videoId>
  <title>A short</title>
  <link rel="alternate" href="https://www.youtube.com/shorts/bbbbbbbbbbb"/>
  <published>2026-10-24T09:30:00+05:30</published>
 </entry>
 <entry>
  <yt:videoId>ccccccccccc</yt:videoId>
  <title>No date</title>
 </entry>
 <entry>
  <yt:videoId>ddddddddddd</yt:videoId>
  <title>No link</title>
  <published>2026-10-20T00:00:00+00:00</published>
 </entry>
</feed>
"""

class FeedParserTest {
    @Test
    fun parseConvertsOffsetsToUtc() {
        assertEquals(Instant.parse("2026-10-24T04:00:00Z"), parse()[1].publishedAt)
    }

    @Test
    fun parseFallsBackToTheWatchUrlWhenTheLinkIsMissing() {
        assertEquals("https://www.youtube.com/watch?v=ddddddddddd", parse()[2].link)
    }

    @Test
    fun parseReadsEveryDatedEntryAndIgnoresMediaFields() {
        val entries = parse()

        assertEquals(listOf("aaaaaaaaaaa", "bbbbbbbbbbb", "ddddddddddd"), entries.map { entry -> entry.videoId })
        assertEquals(FeedEntry(link = "https://www.youtube.com/watch?v=aaaaaaaaaaa", title = "A long video", videoId = "aaaaaaaaaaa", publishedAt = Instant.parse("2026-10-25T14:00:06Z")), entries[0])
        assertEquals("https://www.youtube.com/shorts/bbbbbbbbbbb", entries[1].link)
    }

    @Test
    fun parseTakesEachTitleFromItsEntryNotTheFeed() {
        assertEquals(listOf("A long video", "A short", "No link"), parse().map { entry -> entry.title })
    }

    private fun parse(): List<FeedEntry> = FeedParser.parse(parser = KXmlParser(), reader = StringReader(FEED))
}
