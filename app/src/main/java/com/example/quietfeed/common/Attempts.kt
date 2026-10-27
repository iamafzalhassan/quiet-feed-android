package com.example.quietfeed.common

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

suspend fun <T : Any> attemptOrNull(action: suspend () -> T): T? = try {
    action()
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    null
}

suspend fun failureOf(action: suspend () -> Unit): Exception? = try {
    action()
    null
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    exception
}

suspend fun <T> retrying(attempts: Int, delayMillis: Long, shouldRetry: (Exception) -> Boolean, action: suspend () -> T): T {
    var attempt = 1
    while (true) {
        try {
            return action()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (attempt >= attempts || !shouldRetry(exception)) throw exception
            delay(delayMillis * attempt)
            attempt++
        }
    }
}
