package com.example.quietfeed.data.remote

import org.xmlpull.v1.XmlPullParser
import java.io.Reader
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

object FeedParser {
    private const val FEED_CHILD_DEPTH: Int = 2

    private const val ATTRIBUTE_HREF: String = "href"
    private const val ATTRIBUTE_REL: String = "rel"
    private const val REL_ALTERNATE: String = "alternate"
    private const val TAG_ENTRY: String = "entry"
    private const val TAG_LINK: String = "link"
    private const val TAG_PUBLISHED: String = "published"
    private const val TAG_TITLE: String = "title"
    private const val TAG_VIDEO_ID: String = "yt:videoId"
    private const val WATCH_URL: String = "https://www.youtube.com/watch?v="

    fun parse(parser: XmlPullParser, reader: Reader): List<FeedEntry> {
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(reader)
        val entries = mutableListOf<FeedEntry>()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.depth == FEED_CHILD_DEPTH && parser.name == TAG_ENTRY) {
                readEntry(parser)?.let(entries::add)
            }
            event = parser.next()
        }
        return entries
    }

    private fun readEntry(parser: XmlPullParser): FeedEntry? {
        val entryDepth = parser.depth
        var link = ""
        var title = ""
        var videoId = ""
        var publishedAt: Instant? = null
        var event = parser.next()
        while (event != XmlPullParser.END_DOCUMENT && !(event == XmlPullParser.END_TAG && parser.depth == entryDepth)) {
            if (event == XmlPullParser.START_TAG && parser.depth == entryDepth + 1) {
                when (parser.name) {
                    TAG_LINK -> if (parser.getAttributeValue(null, ATTRIBUTE_REL) == REL_ALTERNATE) link = parser.getAttributeValue(null, ATTRIBUTE_HREF).orEmpty()
                    TAG_PUBLISHED -> publishedAt = parseInstant(parser.nextText())
                    TAG_TITLE -> title = parser.nextText().trim()
                    TAG_VIDEO_ID -> videoId = parser.nextText().trim()
                }
            }
            event = parser.next()
        }
        val published = publishedAt ?: return null
        if (videoId.isBlank()) return null
        return FeedEntry(link = link.ifBlank { WATCH_URL + videoId }, title = title, videoId = videoId, publishedAt = published)
    }

    private fun parseInstant(text: String): Instant? = try {
        OffsetDateTime.parse(text.trim()).toInstant()
    } catch (exception: DateTimeParseException) {
        null
    }
}
