package com.example.quietfeed

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.quietfeed.app.App
import com.example.quietfeed.app.NewVideoNotifier
import com.example.quietfeed.domain.channel.CHANNEL_ID_PATTERN

private val channelId: Regex = Regex(CHANNEL_ID_PATTERN)

class MainActivity : ComponentActivity() {
    private var openChannelId: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        val barStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        enableEdgeToEdge(navigationBarStyle = barStyle, statusBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            openChannelId = intent.channelIdExtra()
        }
        val graph = (application as QuietFeedApplication).graph
        setContent { App(graph = graph, onChannelOpened = { openChannelId = null }, openChannelId = openChannelId) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        openChannelId = intent.channelIdExtra()
    }
}

private fun Intent.channelIdExtra(): String? = getStringExtra(NewVideoNotifier.EXTRA_CHANNEL_ID)?.takeIf(channelId::matches)
