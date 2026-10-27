# QuietFeed

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-2.8-3DDC84)
![Platform](https://img.shields.io/badge/platform-Android_8.0%2B-3DDC84?logo=android&logoColor=white)

A quiet, text-only way to follow YouTube channels on Android, built with Kotlin and Jetpack Compose. Add a channel, and QuietFeed shows you only what it uploads from that moment on: titles and dates, with no thumbnails, no avatars, no recommendations and no Shorts.

There is no Google account, no subscription, no API key and no server. Channels and their videos live on the device, and new uploads arrive through YouTube's public RSS feeds, with a fallback for when those feeds fail.

## Features

- **Add a channel your way**: paste a channel ID, a channel URL or an `@handle`, and give it your own name. The name is what the app shows everywhere, and you can change it later.
- **Only what's new**: each channel has its own starting point, the moment you added it. Nothing uploaded before then ever appears.
- **Text only**: every list is titles and dates. No image is ever loaded or cached.
- **No Shorts**: Shorts are detected and left out everywhere, including notifications.
- **Red dot** next to a channel with unseen videos, cleared as soon as you open it.
- **Ordered by latest upload**, so the channel that posted most recently is always at the top.
- **Notifications** for new uploads while the app is closed, one per channel, opening straight to that channel when tapped. Android 13 and later ask for permission once.
- **Background sync** about every 30 minutes when online, plus whenever you open the app and from the Sync button in the top bar.
- **History that grows**: a video stays after it drops out of YouTube's feed.
- **Selection mode**: long-press a channel to rename or remove it from the top bar. Removing a channel deletes its videos.
- **Clear a channel**: wipe its saved videos and start it over from now.
- **Opens in YouTube**: tapping a video opens the YouTube app, or the browser if it isn't installed.

## Architecture

- **MVVM with unidirectional data flow.** Each screen has a ViewModel exposing one immutable `UiState` as a `StateFlow` and accepting a sealed `Event` through a single `onEvent`.
- **Packages named for their role.** `domain` holds the pure models and input parsing, `data` holds Room (`local`), YouTube access (`remote`), the repositories and `sync`, `ui` holds the theme, components and formatters, `platform` the Android bridges, and `feature` one package per screen. Only `app` sees everything, to wire the graph, navigation, background work and notifications.
- **Layering by convention.** Composables never touch a repository, DAO or sync; ViewModels never import Compose UI or a `Context`.
- **Manual dependency graph.** `AppGraph` builds the database, the HTTP client, the repositories and `FeedSync` once per process. No DI framework.

## How the feed works

1. **Adding a channel.** The input is parsed into a channel ID or a handle. The channel's YouTube page is fetched and the ID read from its canonical link, so a handle resolves to the right channel and an ID that doesn't exist is rejected.
2. **Fetching uploads.** Each channel's RSS feed is tried first, because it's small and its dates are exact. When the feed fails, which YouTube's feed endpoint does often, QuietFeed reads the channel's Videos tab through [NewPipeExtractor](https://github.com/TeamNewPipe/NewPipeExtractor) instead.
3. **Filtering.** Only videos published after the channel's starting point and not already stored are kept. A video is checked once to rule out Shorts, then inserted unseen.
4. **Syncing safely.** Up to four channels sync at a time, one failing channel never stops the others, and two syncs started together share one run. Sync never deletes a video.
5. **Notifying.** Each background sync reports the videos it actually inserted, so a video can only ever be announced once. Notifications are only posted while the app isn't on screen.

## Storage

- **Room**, with a `channels` table and a `videos` table. Videos cascade-delete with their channel, and the schema is exported to `app/schemas`.
- **Seen state and the starting point** are stored per video and per channel, so the red dot and the cutoff survive restarts.
- **No backups.** `allowBackup` is off; everything stays on the device.

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.2, Coroutines, Flow, JDK 17 |
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Persistence | Room with KSP, exported schema |
| Background | WorkManager, Process Lifecycle |
| Networking | OkHttp |
| Feeds | `XmlPullParser` for RSS, NewPipeExtractor (GPL-3) as the fallback |
| Platform | Android 8.0 (API 26) and later, compile SDK 37, core library desugaring |

## Design system

QuietFeed shares one design system with my other apps: a dark navy-night palette with a periwinkle accent, and a dotted divider as the signature motif. Colours, spacing and typography are tokens in `ui/theme`, with SF Pro Display for all text. Shared components (`AppTextField`, `AppButtons`, `AppListTile`, `AppTopBar`, `AppModalSheet`, `ConfirmSheet`, `TextInputSheet`, `AppSnackbar`, `EmptyState`, `DottedDivider`, and the iOS-style `ActivityIndicator`) live in `ui/components`. Screens never use a raw colour or a bare measurement. The launcher icon and splash screen are a single vector mark, a block Q, in paper on navy.

## Code conventions

- A strict member ordering convention for every Kotlin file: properties sorted by type tier, then type, then name; functions ordered by call order.
- No comments in source. Names, types and ordering carry the meaning.
- All user-facing text lives in `strings.xml`.
- `.editorconfig` configures ktlint's `intellij_idea` style at a 240-column line width for the IDE; no ktlint plugin runs in the build.

## Project structure

```
app/src/main/java/com/example/quietfeed/
    MainActivity.kt, QuietFeedApplication.kt
    app/                 App, AppGraph, FeedSyncWorker, FeedSyncScheduler, NewVideoNotifier, navigation routes
    domain/channel/      Channel, ChannelReference and its parser, add-channel results
    domain/video/        Video
    data/local/          QuietFeedDatabase, entity/, dao/
    data/remote/         YouTubeClient, FeedParser, ChannelPageParser, ChannelVideosReader, NewPipeDownloader
    data/repository/     Channel and video repositories, mapping
    data/sync/           FeedSync, SyncReport, the new-video filter
    platform/            VideoLauncher, NotificationPermission
    ui/theme/            AppColors, AppSpacing, AppTextStyles, QuietFeedTheme
    ui/components/       Shared components: control/, display/, feedback/, sheet/
    ui/format/           DateFormat
    common/              Coroutine and state helpers
    feature/channels/    Channel list, add sheet, selection mode
    feature/channel/     A channel's videos
app/schemas/             Exported Room schema
```

## Building

**Requirements:** Android Studio with the Android SDK (compile SDK 37) and JDK 17.

- Open the project in Android Studio and run the `app` configuration, or run `./gradlew :app:assembleDebug`.
- The first Gradle sync downloads NewPipeExtractor from JitPack.
- Release builds are shrunk with R8; run `./gradlew :app:assembleRelease`.

## Testing

JUnit 4 tests live in `app/src/test/java`, mirroring the package of the code they cover:

- **`domain/channel/ChannelReferenceTest`**: channel IDs, channel URLs in every form and `@handles` with or without a URL are accepted, and everything else is rejected.
- **`data/remote/FeedParserTest`**: entries are read with their own titles, offsets convert to UTC, a missing link falls back to the watch URL, and media fields are ignored.
- **`data/remote/ChannelPageParserTest`**: the canonical link wins, the identifier and `externalId` are fallbacks, and an echoed `channelId` alone never counts as a channel.
- **`data/remote/ChannelVideosReaderTest`**: extractor results become feed entries, while Shorts, undated items and URLs without a video ID are dropped.
- **`data/remote/FeedEntryTest`**: Shorts links are recognised.
- **`data/sync/NewEntriesTest`**: only videos published after the starting point and not already stored are kept, without duplicates.
- **`ui/format/DateFormatTest`**: dates read as `14 Sep 2026` in the given time zone.
- **`common/AttemptsTest`**: failures are captured and cancellation is never swallowed.

Run them with `./gradlew :app:testDebugUnitTest`.

## Roadmap

- GitHub Actions CI for tests and builds
- Export and import the channel list
- Search across saved video titles
