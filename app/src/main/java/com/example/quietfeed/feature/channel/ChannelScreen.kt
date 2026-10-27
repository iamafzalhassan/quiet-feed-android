package com.example.quietfeed.feature.channel

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quietfeed.R
import com.example.quietfeed.domain.video.Video
import com.example.quietfeed.platform.rememberVideoLauncher
import com.example.quietfeed.ui.components.display.AppListTile
import com.example.quietfeed.ui.components.display.AppTopBar
import com.example.quietfeed.ui.components.feedback.AppSnackbarHost
import com.example.quietfeed.ui.components.feedback.ScreenEmptyState
import com.example.quietfeed.ui.components.feedback.rememberAppSnackbarState
import com.example.quietfeed.ui.format.DateFormat
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.AppSpacing
import kotlinx.coroutines.launch

private const val VIDEO_TITLE_MAX_LINES: Int = 2

@Composable
fun ChannelScreen(onBack: () -> Unit, viewModel: ChannelViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val snackbarState = rememberAppSnackbarState()
    val videoLauncher = rememberVideoLauncher()

    Box(modifier = modifier.fillMaxSize().background(AppColors.surfaceBase)) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(onBack = onBack, title = state.channelName)
            when {
                state.isLoading -> Unit
                state.videos.isEmpty() -> ScreenEmptyState(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    icon = Icons.Outlined.VideoLibrary,
                    message = stringResource(R.string.videos_empty_message),
                    title = stringResource(R.string.videos_empty_title),
                )
                else -> VideoList(
                    modifier = Modifier.weight(1f),
                    onOpenVideo = { video ->
                        if (!videoLauncher.open(video.url)) {
                            scope.launch { snackbarState.showError(resources.getString(R.string.video_open_failed)) }
                        }
                    },
                    videos = state.videos,
                )
            }
        }
        AppSnackbarHost(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(), state = snackbarState)
    }
}

@Composable
private fun VideoList(videos: List<Video>, onOpenVideo: (Video) -> Unit, modifier: Modifier = Modifier) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl + navigationBarBottom, end = AppSpacing.screenPadding, start = AppSpacing.screenPadding, top = AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        items(items = videos, key = { video -> video.id }) { video ->
            AppListTile(
                onClick = { onOpenVideo(video) },
                subtitle = DateFormat.formatShort(video.publishedAt),
                title = video.title,
                titleMaxLines = VIDEO_TITLE_MAX_LINES,
            )
        }
    }
}
