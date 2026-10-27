package com.example.quietfeed.data.sync

data class ChannelUploads(val channelId: String, val channelName: String, val titles: List<String>)

data class SyncReport(val uploads: List<ChannelUploads>, val result: SyncResult)
