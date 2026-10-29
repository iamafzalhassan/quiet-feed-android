package com.example.quietfeed.app

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.quietfeed.QuietFeedApplication
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class FeedSyncWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        withContext(NonCancellable) {
            val report = (applicationContext as QuietFeedApplication).graph.feedSync.syncAll()
            val isAppVisible = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            if (!isAppVisible) {
                NewVideoNotifier(applicationContext).notify(report.uploads)
            }
        }
        return Result.success()
    }
}
