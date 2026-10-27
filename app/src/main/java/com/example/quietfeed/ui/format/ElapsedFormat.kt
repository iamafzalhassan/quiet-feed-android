package com.example.quietfeed.ui.format

import java.time.Duration
import java.time.Instant

private const val MINUTES_PER_DAY: Long = 1_440L
private const val MINUTES_PER_HOUR: Long = 60L

sealed interface Elapsed {
    data class Hours(val count: Int) : Elapsed

    data object JustNow : Elapsed

    data class Minutes(val count: Int) : Elapsed

    data class Since(val instant: Instant) : Elapsed
}

object ElapsedFormat {
    fun between(start: Instant, end: Instant): Elapsed {
        val minutes = Duration.between(start, end).toMinutes()
        return when {
            minutes < 1 -> Elapsed.JustNow
            minutes < MINUTES_PER_HOUR -> Elapsed.Minutes(minutes.toInt())
            minutes < MINUTES_PER_DAY -> Elapsed.Hours((minutes / MINUTES_PER_HOUR).toInt())
            else -> Elapsed.Since(start)
        }
    }
}
