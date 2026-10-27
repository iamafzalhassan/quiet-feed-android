package com.example.quietfeed.ui.format

import java.time.Instant
import java.time.ZoneId

object DateFormat {
    private val monthNames: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    fun formatShort(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val date = instant.atZone(zone).toLocalDate()
        return "${date.dayOfMonth} ${monthNames[date.monthValue - 1]} ${date.year}"
    }
}
