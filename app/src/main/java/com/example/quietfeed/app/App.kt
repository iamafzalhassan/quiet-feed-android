package com.example.quietfeed.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quietfeed.app.navigation.Routes
import com.example.quietfeed.feature.channel.ChannelScreen
import com.example.quietfeed.feature.channel.ChannelViewModel
import com.example.quietfeed.feature.channels.ChannelsScreen
import com.example.quietfeed.feature.channels.ChannelsViewModel
import com.example.quietfeed.ui.theme.AppColors
import com.example.quietfeed.ui.theme.QuietFeedTheme

@Composable
fun App(openChannelId: String?, onChannelOpened: () -> Unit, graph: AppGraph) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val notifier = remember(context) { NewVideoNotifier(context.applicationContext) }
    val currentOnChannelOpened by rememberUpdatedState(onChannelOpened)

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            graph.feedSync.syncAll()
        }
    }

    QuietFeedTheme {
        val navController = rememberNavController()

        NavHost(modifier = Modifier.fillMaxSize().background(AppColors.surfaceBase), navController = navController, startDestination = Routes.CHANNELS) {
            composable(Routes.CHANNELS) {
                ChannelsScreen(
                    onOpenChannel = { channelId -> navController.navigate(Routes.channel(channelId)) },
                    viewModel = viewModel { ChannelsViewModel(channelRepository = graph.channelRepository, feedSync = graph.feedSync) },
                )
            }
            composable(arguments = listOf(navArgument(Routes.ARG_CHANNEL_ID) { type = NavType.StringType }), route = Routes.CHANNEL) { entry ->
                val channelId = entry.arguments?.getString(Routes.ARG_CHANNEL_ID).orEmpty()

                LaunchedEffect(channelId) {
                    notifier.cancel(channelId)
                }

                ChannelScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = viewModel { ChannelViewModel(channelId = channelId, channelRepository = graph.channelRepository, videoRepository = graph.videoRepository) },
                )
            }
        }

        LaunchedEffect(openChannelId) {
            val channelId = openChannelId ?: return@LaunchedEffect
            navController.navigate(Routes.channel(channelId)) {
                launchSingleTop = true
                popUpTo(Routes.CHANNELS)
            }
            currentOnChannelOpened()
        }
    }
}
