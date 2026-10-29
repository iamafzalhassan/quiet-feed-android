package com.example.quietfeed.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.time.Instant

private const val KEY_LAST_CHECKED_AT: String = "last_checked_at"
private const val PREFERENCES_NAME: String = "sync"

class SyncPreferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun observeLastCheckedAt(): Flow<Instant?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_LAST_CHECKED_AT) {
                trySend(lastCheckedAt())
            }
        }
        trySend(lastCheckedAt())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun lastCheckedAt(): Instant? = if (preferences.contains(KEY_LAST_CHECKED_AT)) Instant.ofEpochMilli(preferences.getLong(KEY_LAST_CHECKED_AT, 0L)) else null

    fun setLastCheckedAt(instant: Instant) = preferences.edit { putLong(KEY_LAST_CHECKED_AT, instant.toEpochMilli()) }
}
