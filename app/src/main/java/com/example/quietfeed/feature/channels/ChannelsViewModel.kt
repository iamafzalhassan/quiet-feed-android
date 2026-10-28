package com.example.quietfeed.feature.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietfeed.common.STATE_STOP_TIMEOUT_MILLIS
import com.example.quietfeed.common.attempt
import com.example.quietfeed.data.repository.ChannelRepository
import com.example.quietfeed.data.sync.FeedSync
import com.example.quietfeed.data.sync.SyncResult
import com.example.quietfeed.domain.channel.AddChannelResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChannelsViewModel(private val channelRepository: ChannelRepository, private val feedSync: FeedSync) : ViewModel() {
    private val screenState: MutableStateFlow<ChannelsUiState> = MutableStateFlow(ChannelsUiState())

    val state: StateFlow<ChannelsUiState> = combine(channelRepository.observeChannels(), feedSync.isSyncing, feedSync.observeLastCheckedAt(), screenState) { channels, isSyncing, lastCheckedAt, screen ->
        screen.copy(isLoading = false, isSyncing = isSyncing, channels = channels, lastCheckedAt = lastCheckedAt)
    }.stateIn(initialValue = ChannelsUiState(), scope = viewModelScope, started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS))

    fun onEvent(event: ChannelsEvent) {
        when (event) {
            is ChannelsEvent.AddChannel -> addChannel(input = event.input, name = event.name)
            ChannelsEvent.ConsumeAddedChannel -> screenState.update { state -> state.copy(addedChannelName = null, feedback = state.addedChannelName?.let { name -> ChannelsFeedback.ChannelAdded(name) } ?: state.feedback) }
            ChannelsEvent.DismissAddError -> screenState.update { state -> state.copy(addError = null) }
            ChannelsEvent.DismissFeedback -> screenState.update { state -> state.copy(feedback = null) }
            is ChannelsEvent.ExportChannels -> exportChannels(event.uri)
            is ChannelsEvent.ImportChannels -> importChannels(event.uri)
            is ChannelsEvent.RemoveChannel -> removeChannel(event.channelId)
            is ChannelsEvent.RenameChannel -> renameChannel(channelId = event.channelId, name = event.name)
            ChannelsEvent.Sync -> sync()
        }
    }

    private fun addChannel(input: String, name: String) {
        if (screenState.value.isAdding) return
        screenState.update { state -> state.copy(isAdding = true, addError = null) }
        viewModelScope.launch {
            val result = channelRepository.addChannel(input = input, name = name)
            if (result is AddChannelResult.Added) {
                feedSync.requestChannelSync(result.channel.id)
            }
            screenState.update { state ->
                when (result) {
                    is AddChannelResult.Added -> state.copy(isAdding = false, addedChannelName = result.channel.name)
                    is AddChannelResult.Rejected -> state.copy(isAdding = false, addError = result.error)
                }
            }
        }
    }

    private fun exportChannels(uri: String) {
        viewModelScope.launch {
            val feedback = attempt { channelRepository.exportChannels(uri) }.fold(onFailure = { ChannelsFeedback.ExportFailed }, onSuccess = { count -> ChannelsFeedback.ChannelsExported(count) })
            screenState.update { state -> state.copy(feedback = feedback) }
        }
    }

    private fun importChannels(uri: String) {
        viewModelScope.launch {
            val outcome = attempt { channelRepository.importChannels(uri) }
            val imported = outcome.getOrNull()
            val feedback = when {
                outcome.isFailure -> ChannelsFeedback.ImportFailed
                imported == null -> ChannelsFeedback.ImportInvalid
                else -> ChannelsFeedback.ChannelsImported(imported)
            }
            screenState.update { state -> state.copy(feedback = feedback) }
            if (imported != null && imported > 0) {
                feedSync.syncAll()
            }
        }
    }

    private fun removeChannel(channelId: String) {
        val name = state.value.channels.firstOrNull { channel -> channel.id == channelId }?.name ?: return
        viewModelScope.launch {
            channelRepository.deleteChannel(channelId)
            screenState.update { state -> state.copy(feedback = ChannelsFeedback.ChannelRemoved(name)) }
        }
    }

    private fun renameChannel(channelId: String, name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            channelRepository.renameChannel(channelId = channelId, name = name)
            screenState.update { state -> state.copy(feedback = ChannelsFeedback.ChannelRenamed(name.trim())) }
        }
    }

    private fun sync() {
        if (state.value.isSyncing) return
        viewModelScope.launch {
            val feedback = when (feedSync.syncAll().result) {
                SyncResult.OFFLINE -> ChannelsFeedback.SyncOffline
                SyncResult.PARTIAL -> ChannelsFeedback.SyncFailed
                SyncResult.SYNCED -> null
            }
            if (feedback != null) {
                screenState.update { state -> state.copy(feedback = feedback) }
            }
        }
    }
}
