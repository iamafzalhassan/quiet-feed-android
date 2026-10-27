package com.example.quietfeed.data.sync

import com.example.quietfeed.data.remote.FeedEntry

fun List<FeedEntry>.newSince(addedAtMillis: Long, knownIds: Set<String>): List<FeedEntry> = filter { entry -> entry.publishedAt.toEpochMilli() > addedAtMillis && entry.videoId !in knownIds }.distinctBy { entry -> entry.videoId }
