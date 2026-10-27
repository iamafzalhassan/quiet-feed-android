package com.example.quietfeed.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AttemptsTest {
    @Test
    fun attemptOrNullReturnsNullOnFailure() = runBlocking {
        assertNull(attemptOrNull<String> { throw IOException() })
        assertEquals("ok", attemptOrNull { "ok" })
    }

    @Test
    fun attemptRethrowsCancellation() = runBlocking {
        val thrown = runCatching { attempt<String> { throw CancellationException() } }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }

    @Test
    fun attemptWrapsTheOutcome() = runBlocking {
        val failure = IOException()

        assertSame(failure, attempt<String> { throw failure }.exceptionOrNull())
        assertEquals("ok", attempt { "ok" }.getOrNull())
    }
}
