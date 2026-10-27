package com.example.quietfeed.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class DateFormatTest {
    @Test
    fun formatShortUsesDayAbbreviatedMonthAndYear() {
        assertEquals("14 Sep 2026", DateFormat.formatShort(instant = Instant.parse("2026-09-14T10:00:00Z"), zone = ZoneOffset.UTC))
        assertEquals("1 Jan 2027", DateFormat.formatShort(instant = Instant.parse("2027-01-01T00:00:00Z"), zone = ZoneOffset.UTC))
    }

    @Test
    fun formatShortUsesTheGivenTimeZone() {
        val instant = Instant.parse("2026-10-25T20:00:00Z")

        assertEquals("25 Oct 2026", DateFormat.formatShort(instant = instant, zone = ZoneOffset.UTC))
        assertEquals("26 Oct 2026", DateFormat.formatShort(instant = instant, zone = ZoneId.of("Asia/Colombo")))
    }
}
