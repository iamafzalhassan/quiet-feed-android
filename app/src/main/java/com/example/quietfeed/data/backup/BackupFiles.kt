package com.example.quietfeed.data.backup

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val WRITE_TRUNCATE_MODE: String = "wt"

class BackupFiles(private val context: Context) {
    suspend fun read(uri: String): String = withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri.toUri())?.use { stream -> stream.bufferedReader().readText() } ?: throw IOException("Cannot open $uri")
    }

    suspend fun write(text: String, uri: String) = withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri.toUri(), WRITE_TRUNCATE_MODE)?.use { stream -> stream.bufferedWriter().use { writer -> writer.write(text) } } ?: throw IOException("Cannot open $uri")
    }
}
