package com.example.quietfeed

import android.app.Application
import com.example.quietfeed.app.AppGraph
import com.example.quietfeed.app.FeedSyncScheduler

class QuietFeedApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }

    override fun onCreate() {
        super.onCreate()
        FeedSyncScheduler.schedule(this)
    }
}
