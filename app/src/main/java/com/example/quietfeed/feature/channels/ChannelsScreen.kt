package com.example.quietfeed.feature.channels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quietfeed.R
import com.example.quietfeed.domain.channel.Channel
import com.example.quietfeed.feature.channels.components.AddChannelSheet
import com.example.quietfeed.platform.rememberNotificationPermissionRequest
import com.example.quietfeed.ui.components.control.PrimaryButton
import com.example.quietfeed.ui.components.display.AppListTile
import com.example.quietfeed.ui.components.display.AppTopBar
import com.example.quietfeed.ui.components.feedback.AppPullToRefreshBox
import com.example.quietfeed.ui.components.feedback.AppSnackbarHost
import com.example.quietfeed.ui.components.feedback.ScreenEmptyState
import com.example.quietfeed.ui.components.feedback.rememberAppSnackbarState
import com.example.quietfeed.ui.components.sheet.ConfirmSheet
import com.example.quietfeed.ui.components.sheet.TextInputSheet
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.AppSpacing
import kotlinx.coroutines.launch

@Composable
fun ChannelsScreen(onOpenChannel: (String) -> Unit, viewModel: ChannelsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarState = rememberAppSnackbarState()
    val requestNotificationPermission = rememberNotificationPermissionRequest()
    var isAddSheetOpen by rememberSaveable { mutableStateOf(false) }
    var selectedChannelId by rememberSaveable { mutableStateOf<String?>(null) }
    var channelSheet by rememberSaveable { mutableStateOf<ChannelSheet?>(null) }
    val selectedChannel = state.channels.firstOrNull { channel -> channel.id == selectedChannelId }
    val openAddSheet = {
        viewModel.onEvent(ChannelsEvent.DismissAddError)
        isAddSheetOpen = true
    }
    val clearSelection = {
        channelSheet = null
        selectedChannelId = null
    }

    BackHandler(enabled = selectedChannel != null, onBack = clearSelection)

    LaunchedEffect(state.addedChannelName, isAddSheetOpen) {
        if (state.addedChannelName != null && !isAddSheetOpen) {
            viewModel.onEvent(ChannelsEvent.ConsumeAddedChannel)
            requestNotificationPermission()
        }
    }

    LaunchedEffect(state.feedback) {
        val feedback = state.feedback ?: return@LaunchedEffect
        scope.launch {
            when (feedback) {
                is ChannelsFeedback.ChannelAdded -> snackbarState.showSuccess(resources.getString(R.string.channel_added, feedback.name))
                is ChannelsFeedback.ChannelRemoved -> snackbarState.showSuccess(resources.getString(R.string.channel_removed, feedback.name))
                is ChannelsFeedback.ChannelRenamed -> snackbarState.showSuccess(resources.getString(R.string.channel_renamed, feedback.name))
                ChannelsFeedback.RefreshFailed -> snackbarState.showError(resources.getString(R.string.refresh_failed))
                ChannelsFeedback.RefreshOffline -> snackbarState.showError(resources.getString(R.string.refresh_offline))
            }
        }
        viewModel.onEvent(ChannelsEvent.DismissFeedback)
    }

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (selectedChannel == null) {
                AppTopBar(title = stringResource(R.string.channels_title)) {
                    IconButton(onClick = openAddSheet) {
                        Icon(contentDescription = stringResource(R.string.action_add_channel), imageVector = Icons.Outlined.Add, tint = AppColors.primary)
                    }
                }
            } else {
                AppTopBar(backIcon = Icons.Outlined.Close, backLabel = stringResource(R.string.action_clear_selection), onBack = clearSelection, title = selectedChannel.name) {
                    IconButton(onClick = { channelSheet = ChannelSheet.RENAME }) {
                        Icon(contentDescription = stringResource(R.string.action_rename), imageVector = Icons.Outlined.Edit, tint = AppColors.primary)
                    }
                    IconButton(onClick = { channelSheet = ChannelSheet.REMOVE }) {
                        Icon(contentDescription = stringResource(R.string.action_remove), imageVector = Icons.Outlined.Delete, tint = AppColors.primary)
                    }
                }
            }
            AppPullToRefreshBox(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                isEnabled = selectedChannel == null,
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.onEvent(ChannelsEvent.Refresh) },
            ) {
                when {
                    state.isLoading -> Unit
                    state.channels.isEmpty() -> ScreenEmptyState(
                        modifier = Modifier.fillMaxSize(),
                        icon = Icons.Outlined.RssFeed,
                        message = stringResource(R.string.channels_empty_message),
                        title = stringResource(R.string.channels_empty_title),
                    ) {
                        PrimaryButton(modifier = Modifier.fillMaxWidth(), label = stringResource(R.string.action_add_channel), onClick = openAddSheet)
                    }
                    else -> ChannelList(
                        channels = state.channels,
                        onClickChannel = { channelId ->
                            when (selectedChannel?.id) {
                                null -> onOpenChannel(channelId)
                                channelId -> clearSelection()
                                else -> selectedChannelId = channelId
                            }
                        },
                        onLongClickChannel = { channelId -> selectedChannelId = channelId },
                        selectedChannelId = selectedChannel?.id,
                    )
                }
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }

    if (isAddSheetOpen) {
        AddChannelSheet(
            addedChannelName = state.addedChannelName,
            addError = state.addError,
            isAdding = state.isAdding,
            onAdd = { input, name -> viewModel.onEvent(ChannelsEvent.AddChannel(input = input, name = name)) },
            onClosed = {
                isAddSheetOpen = false
                viewModel.onEvent(ChannelsEvent.DismissAddError)
            },
            onInputChange = {
                if (state.addError != null) {
                    viewModel.onEvent(ChannelsEvent.DismissAddError)
                }
            },
        )
    }

    if (selectedChannel != null) {
        when (channelSheet) {
            ChannelSheet.REMOVE -> ConfirmSheet(
                confirmLabel = stringResource(R.string.action_remove),
                isDestructive = true,
                message = stringResource(R.string.remove_channel_message, selectedChannel.name),
                onConfirm = {
                    clearSelection()
                    viewModel.onEvent(ChannelsEvent.RemoveChannel(selectedChannel.id))
                },
                onDismiss = { channelSheet = null },
                title = stringResource(R.string.remove_channel_title),
            )
            ChannelSheet.RENAME -> TextInputSheet(
                confirmLabel = stringResource(R.string.action_save),
                initialValue = selectedChannel.name,
                label = stringResource(R.string.field_name),
                maxLength = Channel.MAX_NAME_LENGTH,
                onConfirm = { name ->
                    clearSelection()
                    viewModel.onEvent(ChannelsEvent.RenameChannel(channelId = selectedChannel.id, name = name))
                },
                onDismiss = { channelSheet = null },
                title = stringResource(R.string.rename_channel_title),
            )
            null -> Unit
        }
    }
}

@Composable
private fun ChannelList(selectedChannelId: String?, channels: List<Channel>, onClickChannel: (String) -> Unit, onLongClickChannel: (String) -> Unit, modifier: Modifier = Modifier) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl + navigationBarBottom, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        items(items = channels, key = { channel -> channel.id }) { channel ->
            AppListTile(
                isSelected = channel.id == selectedChannelId,
                onClick = { onClickChannel(channel.id) },
                onLongClick = { onLongClickChannel(channel.id) },
                title = channel.name,
            ) {
                if (channel.hasNewVideos) {
                    NewVideosDot()
                }
            }
        }
    }
}

@Composable
private fun NewVideosDot(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.channel_new_videos)

    Box(modifier = modifier.size(AppSpacing.newDot).background(color = AppColors.danger, shape = CircleShape).semantics { contentDescription = description })
}
