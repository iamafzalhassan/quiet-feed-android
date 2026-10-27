package com.example.quietfeed.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class ElapsedFormatTest {
    private val start: Instant = Instant.parse("2026-10-27T10:00:00Z")

    @Test
    fun betweenCountsWholeHoursUnderADay() {
        assertEquals(Elapsed.Hours(1), ElapsedFormat.between(end = start.plusSeconds(60 * 60), start = start))
        assertEquals(Elapsed.Hours(23), ElapsedFormat.between(end = start.plusSeconds(24 * 60 * 60 - 1), start = start))
    }

    @Test
    fun betweenCountsWholeMinutesUnderAnHour() {
        assertEquals(Elapsed.Minutes(1), ElapsedFormat.between(end = start.plusSeconds(60), start = start))
        assertEquals(Elapsed.Minutes(59), ElapsedFormat.between(end = start.plusSeconds(59 * 60 + 59), start = start))
    }

    @Test
    fun betweenFallsBackToTheDateAfterADay() {
        assertEquals(Elapsed.Since(start), ElapsedFormat.between(end = start.plusSeconds(24 * 60 * 60), start = start))
    }

    @Test
    fun betweenIsJustNowUnderAMinuteOrWhenTheClockWentBack() {
        assertEquals(Elapsed.JustNow, ElapsedFormat.between(end = start.plusSeconds(59), start = start))
        assertEquals(Elapsed.JustNow, ElapsedFormat.between(end = start.minusSeconds(600), start = start))
    }
}
