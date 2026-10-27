package com.example.quietfeed.app

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quietfeed.QuietFeedApplication

class FeedSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        (applicationContext as QuietFeedApplication).graph.feedSync.syncAll()
        return Result.success()
    }
}
