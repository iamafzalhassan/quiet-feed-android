package com.example.quietfeed.common

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.IOException

class AttemptsTest {
    @Test
    fun attemptOrNullReturnsNullOnFailure() = runBlocking {
        assertNull(attemptOrNull<String> { throw IOException() })
        assertEquals("ok", attemptOrNull { "ok" })
    }

    @Test
    fun failureOfReturnsTheException() = runBlocking {
        val failure = IOException()

        assertSame(failure, failureOf { throw failure })
        assertNull(failureOf {})
    }

    @Test
    fun retryingRethrowsAtOnceWhenTheFailureIsNotRetryable() = runBlocking {
        var calls = 0
        val failure = IOException()

        val thrown = runCatching {
            retrying(attempts = 3, delayMillis = 0L, shouldRetry = { false }) {
                calls++
                throw failure
            }
        }.exceptionOrNull()

        assertSame(failure, thrown)
        assertEquals(1, calls)
    }

    @Test
    fun retryingReturnsTheFirstSuccess() = runBlocking {
        var calls = 0

        val result = retrying(attempts = 3, delayMillis = 0L, shouldRetry = { true }) {
            calls++
            if (calls < 2) throw IOException()
            "ok"
        }

        assertEquals("ok", result)
        assertEquals(2, calls)
    }

    @Test
    fun retryingStopsAfterTheLastAttempt() = runBlocking {
        var calls = 0

        val thrown = runCatching {
            retrying(attempts = 3, delayMillis = 0L, shouldRetry = { true }) {
                calls++
                throw IOException()
            }
        }.exceptionOrNull()

        assertEquals(IOException::class.java, thrown?.javaClass)
        assertEquals(3, calls)
    }
}
