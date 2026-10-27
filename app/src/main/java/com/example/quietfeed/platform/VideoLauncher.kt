package com.example.quietfeed.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

private const val YOUTUBE_PACKAGE: String = "com.google.android.youtube"

class VideoLauncher(private val context: Context) {
    fun open(url: String): Boolean {
        val uri = url.toUri()
        return start(Intent(Intent.ACTION_VIEW, uri).setPackage(YOUTUBE_PACKAGE)) || start(Intent(Intent.ACTION_VIEW, uri))
    }

    private fun start(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (exception: ActivityNotFoundException) {
        false
    }
}

@Composable
fun rememberVideoLauncher(): VideoLauncher {
    val context = LocalContext.current
    return remember(context) { VideoLauncher(context) }
}
