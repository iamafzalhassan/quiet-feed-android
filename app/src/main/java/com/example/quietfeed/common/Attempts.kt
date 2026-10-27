package com.example.quietfeed.common

import kotlinx.coroutines.CancellationException

suspend fun <T> attempt(action: suspend () -> T): Result<T> = try {
    Result.success(action())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}

suspend fun <T : Any> attemptOrNull(action: suspend () -> T): T? = attempt(action).getOrNull()
