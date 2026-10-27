package com.example.quietfeed

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.quietfeed.app.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        val barStyle = SystemBarStyle.light(darkScrim = Color.TRANSPARENT, scrim = Color.TRANSPARENT)
        enableEdgeToEdge(navigationBarStyle = barStyle, statusBarStyle = barStyle)
        super.onCreate(savedInstanceState)
        val graph = (application as QuietFeedApplication).graph
        setContent { App(graph) }
    }
}
