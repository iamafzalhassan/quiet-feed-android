# QuietFeed

Text-only Android app for following YouTube channels without a Google account
and without subscribing. It reads YouTube's public RSS feeds, so there is no API
key, no YouTube Data API and no sign-in. Native Kotlin, Jetpack Compose
(Material 3), Room, WorkManager, OkHttp and `XmlPullParser`.

Full specification: `QUIETFEED-SPEC.md`, kept outside the repo at
`C:\Users\AfzalHassan\Desktop\QUIETFEED-SPEC.md`. It is the single source of
truth, except where the **Decisions** section below overrides it. Every
decision is recorded in this file, not in separate notes.

QuietFeed is a personal project. It is never published, so the
`com.example.quietfeed` package and application id stay as they are.

The bar is industry grade: one design system applied with total consistency, one
architecture applied the same way in every feature, and every file written to the
member ordering convention below from its first line.

## Hard constraints

- **Claude never builds, runs, tests or installs the app. The developer does.**
  No `gradlew`, no `assembleDebug`, no unit tests, no instrumented tests, no
  `adb`, no emulator, no Android Studio invocation. Not to "verify", not to
  "check it compiles", not once at the end. Verification is static: read the
  code, reason it through, and report which cases were checked by reading and
  which were not.
- **No git actions without explicit permission.** Never commit, push, branch,
  init or run any other git command unless the developer asks. Even when asked,
  confirm a second time before running it.
- **No images, ever.** No channel avatars, no video thumbnails, no image
  loading library, no image caching. `media:thumbnail` and every other image
  field in the feed are ignored. Material vector icons in the chrome (top bar,
  empty states) are not images and are allowed.
- **No comments anywhere in the project. None, of any kind.** No `//`, no
  `/* */`, no KDoc `/** */`, no section headers, no TODOs, no commented-out
  code, no `#` comments in `.properties`, `.toml` or `.pro`, no `<!-- -->` in
  XML. This is absolute and it overrides §4 and §4a of the ordering convention
  below. Names, types and ordering carry the meaning; if something needs
  explaining, rename it. Tool-generated files are exempt because their tools
  write and rewrite them: `gradlew`, `gradlew.bat`,
  `gradle/gradle-daemon-jvm.properties`, `gradle/wrapper/gradle-wrapper.properties`
  and `local.properties`. They are never hand-edited.
- **Every Kotlin file follows the member ordering convention below**, without
  exception, tests included. A violation is a regression to fix, not legacy to
  leave alone.
- **Never touch another project.** `Debitter`, `biller` and `Evenly` in
  `C:\Users\AfzalHassan\My Projects` are read-only design references. Read them
  freely; never create, edit, move, format or delete anything outside
  `QuietFeed`.
- **Unit tests mirror the source tree.** A test for
  `app/src/main/java/<package path>/X.kt` lives at
  `app/src/test/java/<package path>/XTest.kt`, uses JUnit 4, covers critical
  flows, follows the member ordering and no-comments rules, and is run by the
  developer.
- **Alignment and spacing are strict.** Every measurement comes from
  `AppSpacing`. Never a bare number in a composable.
- **Single-line text never wraps.** `maxLines = 1` with `TextOverflow.Ellipsis`,
  so a long value can never push a row out of alignment. The one exception is a
  video title, see **Decisions**.
- **Loading is always the iOS activity indicator** (`ActivityIndicator`). Never
  a Material progress indicator, never a "Loading…" label.
- **Dark theme only.** No light scheme, no dynamic color, no following the
  system setting. This departs from the light-only family apps on purpose.
- **All user-facing text is a string resource** in `res/values/strings.xml`,
  read with `stringResource` in composition and with `LocalResources` inside a
  coroutine (snackbar messages). No string literal reaches the screen.

## Architecture

Single `app` module. Each top-level package is named for its role, following
`Evenly`:

```
app/src/main/java/com/example/quietfeed/
    MainActivity, QuietFeedApplication   platform entry points
    app/                         App, AppGraph, FeedSyncWorker, FeedSyncScheduler, NewVideoNotifier; the only package that sees every other one
    app/navigation/              Routes
    domain/                      pure Kotlin rules, no IO, no Compose UI
        channel/                 Channel, ChannelReference, ChannelReferenceParser, AddChannelResult, AddChannelError
        video/                   Video
    data/
        local/                   QuietFeedDatabase
        local/entity/            Room entities and query projections
        local/dao/               Room DAOs
        remote/                  YouTubeClient, FeedParser, ChannelPageParser, FeedEntry, remote exceptions
        repository/              repositories and their entity-to-model mappings
        sync/                    FeedSync, SyncReport, ChannelUploads, SyncResult, newSince
    platform/                    VideoLauncher, NotificationPermission
    ui/
        theme/                   AppColors, AppSpacing, AppTextStyles, QuietFeedTheme
        components/control/      AppButtons, AppTextField
        components/display/      AppTopBar, AppListTile, DottedDivider
        components/feedback/     ActivityIndicator, AppPullToRefreshBox, AppSnackbar, EmptyState
        components/sheet/        AppModalSheet, ConfirmSheet, TextInputSheet
        format/                  DateFormat
    common/                      dependency-free helpers: Attempts (attemptOrNull, failureOf, retrying), StateSharing
    feature/channels/            ChannelsScreen, ChannelsViewModel, ChannelsUiState, ChannelsEvent, ChannelsFeedback, ChannelSheet, components/AddChannelSheet
    feature/channel/             ChannelScreen, ChannelViewModel, ChannelUiState
app/src/main/res/font/           sf_pro_display_*.otf
app/src/main/res/values/         strings.xml, colors.xml, themes.xml
app/schemas/                     exported Room schemas
```

## Layering

1. Dependencies point inward. `domain` imports nothing from the app. `data`
   imports `domain` and `common`. `ui` imports `domain` and `common`.
   `platform` and `common` import nothing from the app. `feature/*` may import
   any of those, never `app` and never another feature. Only `app` sees
   everything, to wire the graph, navigation and background work.
2. Composables never touch repositories, DAOs or `FeedSync` directly. Only
   ViewModels do.
3. ViewModels never import a Compose UI type or a `Context`. `@Immutable` /
   `@Stable` runtime annotations are the one exception.
4. State flows one way: an immutable `UiState` down as `StateFlow`, a sealed
   `<Feature>Event` up through a single `onEvent`. No state is duplicated
   between Compose and the ViewModel.
5. Model classes are `@Immutable` data classes whose lists are only ever rebuilt,
   never mutated in place.
6. A change to shared behaviour lands in one place. If two screens need the same
   widget, it moves to `ui/components/`, into the folder for its kind. Code only
   one feature uses lives in that feature.
7. Repositories are the only place an entity is mapped to a model. `FeedSync`
   is the only writer of videos.

## Decisions

Every deliberate deviation from the spec or from this file, and every
significant choice, is recorded here as a bullet when it is made, with its
reason.

- **Code generation is allowed for Room only.** KSP compiles Room, as in
  `Evenly`. Debitter forbids code generation, but the spec requires Room. No
  other processor, no serialization plugin.
- **`minSdk` 26, `compileSdk` 37, `targetSdk` 36, Java 17, AGP 9.1.1 with
  built-in Kotlin.** The wizard's minSdk 29 was lowered to the spec's 26. The
  compile SDK and library versions match `Debitter`; `targetSdk` stays at 36
  because it opts into runtime behaviour and is a separate decision.
- **Shorts are never shown.** The feed does not mark Shorts, so every new entry
  is checked: a `/shorts/` link is a Short outright; otherwise `FeedSync` sends a
  `HEAD` to `https://www.youtube.com/shorts/<id>` without following redirects.
  `2xx` is a Short, a redirect or other `4xx` is a normal video, and `429`,
  `5xx` or a network error skips the entry so the next sync tries it again.
  Shorts are stored with `isShort = 1` so they are never probed twice, and every
  query filters them out. This adds `isShort` to the spec's `Video` table.
- **Requests carry `Cookie: SOCS=CAI`** so YouTube's EU consent page never
  replaces the channel page or the Shorts check.
- **A channel is confirmed by its channel page, not by its RSS feed.** The spec
  validates a new channel by fetching its feed and takes the name from the feed
  title. YouTube's feed endpoint fails intermittently (404 and 500 for every
  channel, seen on 27 Oct 2026 and reported by others since December 2025), so
  adding a channel fetches `youtube.com/@handle` or `youtube.com/channel/UC…`
  instead and reads the id from it (`ChannelPageParser`). The feed is used only
  for videos.
- **The channel name is the user's own.** The spec takes the name from the feed
  title; instead the Add Channel sheet has a required **Name** field above the
  **Channel** field, capped at `Channel.MAX_NAME_LENGTH` (50). That name is the
  display name everywhere (list, sort order, channel screen title, snackbars,
  confirm sheet), and sync never changes it, so the feed title is not parsed at
  all. The name can be changed later through **Rename**.
- **The channel id is read from the page in this order**: the canonical
  `/channel/UC…` link, `itemprop="identifier"`, then `"externalId"`. The
  spec's `"channelId"` is deliberately not used: a page carries other
  channels' ids in that key, and the page for a channel id that does not exist
  still answers `200` and echoes the requested id there. A page with none of the
  three is a channel that does not exist.
- **Videos come from the RSS feed first and NewPipeExtractor second.** This is
  the deliberate departure from the spec's "RSS only". The feed failed on most
  requests for days (404 and 500 for most channels, October 2026), so
  `YouTubeClient.fetchLatestVideos` tries the feed once, with no retries, and on
  any failure reads the channel's Videos tab through NewPipeExtractor
  (`ChannelVideosReader`), the library the NewPipe app uses, with no API key and
  no account. The feed stays first because it is light and its dates are exact.
  The Videos tab gives YouTube's relative dates ("3 hours ago"), converted by the
  extractor into approximate instants, so a video uploaded within about one
  date unit of the moment a channel was added can fall on the wrong side of the
  cutoff; everything older or newer is unaffected. The Videos tab holds no
  Shorts, and items flagged as short-form or without a date are dropped. Only
  when both sources fail is the channel marked as not checked, and an error
  never means "channel not found".
- **NewPipeExtractor setup.** `com.github.TeamNewPipe:NewPipeExtractor` from
  JitPack, restricted in `settings.gradle.kts` to the `com.github.TeamNewPipe`
  group. It needs core library desugaring (`desugar_jdk_libs_nio`, as in the
  NewPipe app). `AppGraph` calls `NewPipe.init` once with `NewPipeDownloader`,
  which sends the extractor's requests through the app's one `OkHttpClient`
  with NewPipe's desktop Firefox user agent and turns a `429` into the
  extractor's `ReCaptchaException`. `proguard-rules.pro` carries the NewPipe
  app's keep rules for the extractor's time-ago patterns, Rhino and PrettyTime,
  so R8 does not strip what it loads by reflection. The library is GPL-3, which
  is acceptable for this personal app. When YouTube changes its site the
  extractor can break until a new release; the fix is a version bump in
  `libs.versions.toml`, and the RSS feed keeps working meanwhile wherever it
  answers.
- **Add Channel is a `+` icon in the top bar**, plus a primary button in the
  empty state, as on `Evenly`'s Groups screen. There is no FAB. The empty state
  keeps the spec's wording, "No channels yet. Tap + to add one."
- **Add Channel is a bottom sheet, not a dialog** (`AddChannelSheet` on
  `AppModalSheet`). It stays open while the channel is checked, with the
  activity indicator in the Add button, and shows the error under the Channel
  field in place of the hint. Add stays disabled until both fields are filled.
  It closes itself after a success, and the success snackbar appears only once
  it has finished animating away.
- **A long press selects a channel**, the standard Android selection pattern.
  One channel at a time. The selected tile takes a `primary` border on a
  `surfaceSunken` fill (`AppListTile.isSelected`), and the top bar switches to
  selection mode: a Close icon in the back slot, the channel's name as the
  title, and Edit and Delete icons in `primary`. Close, the system back, or
  tapping the selected tile clears the selection; tapping another tile moves it;
  pull-to-refresh is disabled while a channel is selected. Edit opens a
  `TextInputSheet` prefilled with the current name, whose Save stays disabled
  until the name is non-blank and different. Delete opens a `ConfirmSheet` with
  a `DangerButton`; the spec's dialog and swipe are not used. Cancelling a sheet
  keeps the selection; completing it clears the selection and confirms with a
  success snackbar. Deleting a channel cascades to its videos through the
  foreign key.
- **Errors when adding**: invalid input (rejected before any network call),
  already added (checked before any network call for a channel id, and by the
  primary key for a handle), not found (`404` or no id on the channel page),
  offline (any `IOException`) and failed (anything else).
- **Sync** runs every time the app comes to the foreground, on pull-to-refresh,
  for the new channel right after it is added, and every 30 minutes through
  WorkManager with a connected-network constraint. `FeedSync` runs in the app
  scope so leaving a screen never cancels it, and a second full sync joins the
  one already running. One failing channel never stops the others. Channels sync
  in parallel, at most `MAX_PARALLEL_SYNCS` (4) at a time through one shared
  semaphore, so a refresh costs roughly one channel's feed-and-fallback round
  per batch of four instead of every channel's in a row. Videos are never
  deleted by sync.
- **New-video notifications (the spec's v2) are in.** Every sync returns a
  `SyncReport` with the videos it actually inserted per channel
  (`ChannelUploads`): rows Room did not ignore, Shorts excluded, so a video can
  only ever be announced once. Only `FeedSyncWorker` posts, and only while the
  app is not visible (`ProcessLifecycleOwner` below `STARTED`); syncs the user
  can see show the red dot instead. `NewVideoNotifier` posts one notification
  per channel, tagged with the channel id: the user's channel name as the
  title, the video title as the text, or a count with up to five titles in an
  inbox style. Android groups several automatically, so there is no summary
  notification. Small icon `ic_notification` is the block-Q on a 23 grid,
  tinted `brand_navy`. Tapping opens that channel: the `PendingIntent` carries
  the channel id to a `singleTop` `MainActivity`, which hands it to `App` to
  navigate once, and opening a channel by any route cancels its notification.
  The notification channel is `new_videos` at default importance. On Android
  13 and later the permission is asked once, the first time the channel list
  has a channel and no add sheet is open (`rememberNotificationPermissionRequest`,
  remembered in `SharedPreferences`), so channels added before notifications
  existed are covered too; asking only after an add left those installs never
  asked and silently without notifications. If it is denied, nothing is posted
  and the system settings are the way back. Videos found by a sync the user can
  see (opening the app, pull to refresh) never notify; they show the red dot.
- **Release builds are shrunk with R8** (`isMinifyEnabled`, `isShrinkResources`),
  as in `Evenly`, because `material-icons-extended` is otherwise shipped whole.
  `proguard-rules.pro` is empty: OkHttp, Room and WorkManager ship their own
  consumer rules. Debug builds are not shrunk, so a release build is checked on
  a device before it is relied on.
- **A channel's videos can be cleared**, outside the spec. The channel screen's
  top bar shows a Clear All icon while the list has videos; it opens a
  `ConfirmSheet` with a `DangerButton`, and confirming runs
  `VideoDao.clearVideos` in one transaction: it deletes the channel's stored
  videos and moves the channel's cutoff (`addedAt`) to now. Moving the cutoff
  is what keeps cleared videos from coming back on the next sync while they
  are still in the feed, so clearing means "start over from now", and the
  empty state's "since you added this channel" then reads from the moment of
  clearing. A success snackbar confirms it through `ChannelFeedback`.
- **Channels are ordered by their latest upload**, newest first, then by name
  (case-insensitive) for channels with no videos yet. The spec orders channels
  with new videos first and then alphabetically; the developer chose upload
  recency instead, which still lifts a channel the moment it gets a new video.
  Only videos the channel screen would show count: published after the channel
  was added, Shorts excluded. The sort is a subquery in
  `ChannelDao.observeChannelSummaries`, served by the `channelId` index.
- **The red dot is `AppColors.danger`, `AppSpacing.newDot` (8dp)**, trailing the
  channel name. The channel screen marks videos seen whenever it shows an unseen
  one, so a video that arrives while the screen is open does not leave a dot
  behind.
- **Video titles may take three lines** (`VIDEO_TITLE_MAX_LINES`), because a
  single line hides most titles. The date line under it stays single-line.
- **Scrolling lists fade at their edges** (`Modifier.fadingEdges` in
  `ui/components/display/FadingEdges.kt`), on the channel list and the video
  list. The list is drawn offscreen and masked with `BlendMode.DstIn`, so the
  fade works on any background: the top `fadingEdge` (32dp) fades only while
  the list can scroll back, the bottom one ends at the navigation bar and fades
  only while it can scroll forward, and each fades in and out over 200ms. The
  lists hoist their `LazyListState` to drive it.
- **Videos open in the YouTube app**, through an `ACTION_VIEW` intent with the
  YouTube package. If it is not installed the same intent opens without a
  package (the browser), and if nothing can open it a snackbar explains why. No
  `<queries>` entry is needed because nothing is resolved before starting.
- **Dates are `14 Sep 2026`** in the device's time zone, through
  `ui/format/DateFormat`, matching `Evenly`.
- **Navigation uses plain string routes** in `app/navigation/Routes`, as in
  `Debitter`, so no serialization plugin is needed for type-safe routes.
- **Dependency wiring is manual.** `AppGraph` builds the database, the HTTP
  client, the repositories and `FeedSync` once; `QuietFeedApplication` holds it
  and `FeedSyncWorker` reads it from there. No DI framework.
- **No backup.** `allowBackup="false"`, as in `Debitter`.
- **Launcher icon and splash share one vector mark**
  (`drawable/ic_launcher_foreground`): a block Q, redrawn from the developer's
  concept on a 21 by 21 grid. A 16 square with its lower-right corner cut on
  the diagonal, a 5-wide bar to its right and a 5-high bar below it, and a
  lower-right corner triangle; every edge and diagonal sits on the grid. Paper
  (`brand_paper`) on navy (`brand_navy`), the theme's `surfaceBase` and
  `primary`. The grid is scaled by 2.1 and centred on the 108 canvas, so the
  mark's corners stay inside the 66dp adaptive safe zone and the 160dp splash
  mask. It is an adaptive icon with the same
  drawable as its monochrome layer, so Android 13 themed icons work; the
  template's raster mipmaps were removed because `minSdk` 26 always uses the
  adaptive icon. The splash is `core-splashscreen` (`Theme.QuietFeed.Launch`),
  as in `Debitter`, on a navy ground, then `Theme.QuietFeed` with the paper
  window background.
- **Feed parsing takes the `XmlPullParser` as a parameter.** Production passes
  `Xml.newPullParser()`; unit tests pass kxml2's `KXmlParser`, because the
  Android stub jar cannot parse on the JVM. Namespace processing is off and the
  parser matches `yt:videoId` by its raw name.
- **Unverified until the developer runs it:** Gradle sync with KSP 2.3.12 and
  the Room plugin on AGP 9.1.1, and YouTube's answer to the Shorts `HEAD` check.

---

# Design system

Ported from `Evenly` and `Debitter`, which are themselves ported from `biller`,
so all the apps read as one family: the same tokens, components, type, spacing
and dotted rules. QuietFeed alone is dark only, in the "Navy night" palette the
developer chose: navy-tinted charcoal ground, cool white ink, and a light
periwinkle accent, because the family's navy has too little contrast on a dark
ground. The launcher icon and splash keep the brand navy and paper
(`brand_navy`, `brand_paper` in `colors.xml`).

## Colors — `ui/theme/AppColors.kt`

| Token | Hex | Use |
|---|---|---|
| `primary` | `#A3AEEB` | periwinkle — buttons, focus, active icons, selected tile border |
| `primaryOn` | `#12141C` | text and spinner on primary |
| `danger` | `#EF7B70` | inline errors, danger buttons, the new-videos dot |
| `dangerSurface` | `#4A2328` | failure snackbar |
| `successSurface` | `#1E3B30` | success snackbar |
| `surfaceBase` | `#12141C` | scaffold ground, window background, sheet scrim |
| `surfaceCard` | `#1A1D28` | cards, sheets |
| `surfaceField` | `#242836` | text field fill |
| `surfaceSunken` | `#1F2230` | disabled fill, selected tile, empty state circle |
| `surfaceInverse` | `#2D3142` | neutral snackbar |
| `divider` | `#282C3A` | card borders, hairlines |
| `dividerStrong` | `#363B4D` | dotted dividers |
| `textPrimary` | `#ECEAF2` | values, headings, snackbar text |
| `textSecondary` | `#A2A4B3` | labels, captions, the spinner in a busy button |
| `textTertiary` | `#868899` | empty state icon |
| `textDisabled` | `#555869` | disabled text |

`AppColors` is an `object` of `Color` vals in name order. The Material
`darkColorScheme` in `QuietFeedTheme` maps every slot onto these tokens, so no
Material default colour can leak into a component. Snackbars use tinted dark
surfaces with `textPrimary`, not the bright `danger` as a fill, so their text
stays readable. The window theme is `android:Theme.Material.NoActionBar` on
`surface_base`, and `MainActivity` uses `SystemBarStyle.dark`, so the status and
navigation bar icons are light. Text selection uses `primary` at
`SELECTION_ALPHA` 0.24.

## Spacing and sizing — `ui/theme/AppSpacing.kt`

Scale `xs 4, sm 8, md 12, lg 16, xl 24`.
`screenPadding 16`, `cardPadding 12`, `controlHeight 52`, `topBarHeight 56`,
`hairline 1`. `buttonHeight` is a computed alias of `controlHeight`, so a button
and a field can never drift apart.
Radii: `radiusField 10`, `radiusCard 12`, `radiusButton 50` (fully rounded pill).
Icons: `iconEmptyState 32`, `emptyStateIcon 80`. Edges: `fadingEdge 32`. Indicators:
`progressIndicator 20`, `newDot 8`.

The token set is trimmed to what the app uses, so an unreferenced token is dead
code rather than vocabulary. Add a token here before using a new size, with the
same value `Evenly` or `Debitter` uses when one exists, and name it for its role,
not its value.

## Typography — `ui/theme/AppTextStyles.kt`

SF Pro Display for everything, from `res/font/` (`sf_pro_display_regular`,
`sf_pro_display_medium`, `sf_pro_display_bold`). Upright only, no italics, no
other family; a SemiBold style resolves to Bold. Every numeric style carries
`fontFeatureSettings = "tnum"`. `AppTextStyles` is an `object`, as in `Debitter`,
and every style is built by one private `style(...)` helper.

| Style | Size / weight | Use |
|---|---|---|
| `screenTitle` | 20 / 700 | app bar title |
| `sectionHeading` | 17 / 600 | sheet titles |
| `button` | 16 / 600, +0.2 tracking | all buttons |
| `body` | 15 / 400 | prose, confirm sheet message |
| `fieldValue` | 15 / 600 tnum | text field content |
| `listPrimary` | 15 / 600 | channel name, video title |
| `snack` | 14 / 500 | snackbar message |
| `listMeta` | 13 / 400 tnum | video date |
| `listSecondary` | 13 / 400 | helper text, empty state message |
| `errorHint` | 12 / 500 | inline errors |
| `fieldLabel` | 11 / 500 | label inside a filled field |

## Component conventions — `ui/components/`

- **Text field** (`AppTextField`) — filled, not outlined. `surfaceField` ground,
  `radiusField` corners, an 11sp `fieldLabel` stacked directly above a 15sp
  `fieldValue` inside a single 52dp-tall container. No floating label, no
  border. The whole frame focuses the field when tapped, and the label turns
  `primary` while the field has focus.
- **Helper text** sits `xs` under the control it explains, single-line, in
  `listSecondary`; an error replaces it in `errorHint`.
- **Vertical rhythm**: `xl 24` between major sections and at a scrolling list's
  end, `lg 16` before an action row and for list top padding, `md 12` between
  sibling form fields and between paired buttons, `sm 8` between list rows,
  `xs 4` for micro gaps.
- **Dotted divider** (`DottedDivider`) — 3pt dash, 4pt gap, round cap,
  `hairline` stroke, `dividerStrong`. The signature motif of the design.
- **List tile** (`AppListTile`) — text only, never a leading icon or picture.
  `surfaceCard` fill, `hairline` `divider` border, `radiusCard`, `cardPadding`
  inside, at least `controlHeight` tall, a `listPrimary` title and an optional
  single-line `listMeta` subtitle, a trailing slot, tap and long-press. When
  `isSelected`, the fill is `surfaceSunken` and the border `primary`, same
  hairline width, so selecting never shifts the layout.
- **Buttons** (`AppButtons`) — `PrimaryButton`: filled pill, navy, 52dp, no
  elevation; disabled uses `surfaceSunken` and `textDisabled`; `isLoading`
  disables it and swaps the label for the activity indicator in
  `textSecondary`, at the same height. `SecondaryButton`: outlined pill, navy
  `hairline` border and text, same metrics. `DangerButton`: outlined pill in
  `danger`.
- **App bar** (`AppTopBar`) — flat, `surfaceBase`, no elevation, no surface
  tint, title in `screenTitle` left-aligned at `screenPadding`, back arrow and
  actions in `primary`. The back slot takes another icon and label when needed
  (`backIcon`, `backLabel`), such as Close in selection mode.
- **Sheet** (`AppModalSheet`, `SheetActions`) — `surfaceCard`, drag handle,
  title in `sectionHeading`, `sm`, dotted divider, `lg`, content, then a dotted
  divider and a `Cancel | primary` action row split evenly with an `md` gap.
  Sheet actions hide the sheet first and act from `invokeOnCompletion`
  (`AppSheetState.closeThen`), so nothing ever appears over a sheet that is
  still animating away, and a second tap during the animation is ignored.
- **Confirm sheet** (`ConfirmSheet`) — a sheet with a `body` message and a
  `Cancel | confirm` action row; the confirm button is `DangerButton` when the
  action removes data.
- **Loading** (`ActivityIndicator`, `AppPullToRefreshBox`) — the iOS activity
  indicator: eight fading spokes on a one-second cycle, `progressIndicator`
  square. Pull-to-refresh keeps Material's gesture but draws this indicator,
  revealing its spokes as the pull grows. A busy button shows it in place of its
  label. Never a Material progress indicator.
- **Snackbar** (`AppSnackbarHost`, `AppSnackbarState`) — square corners,
  `surfaceInverse` ground, `dangerSurface` for failure, `successSurface` for a
  completed action, `textPrimary` text, max 2 lines, `SnackbarDuration.Long`.
  It sits flush on the bottom edge: the host is aligned to the bottom with no
  inset of its own, and the message carries the navigation bar padding, so the
  snackbar's ground runs behind the navigation bar instead of floating above it
  with the screen showing through underneath. Every message is a full
  explanation: what happened, then what to do or what it means. Never a bare
  "Saved".
- **Empty state** (`EmptyState`, `ScreenEmptyState`) — `emptyStateIcon` circle on
  `surfaceSunken` holding an `iconEmptyState` icon in `textTertiary`, `lg`, a
  `listPrimary` title, `xs`, a `listSecondary` message, and an optional primary
  action preceded by an `xl` gap, centred in the scrollable remainder of the
  screen.
- **Icons** — Material Icons, Outlined, tinted `primary` in the chrome.
- **Labels** — button and field labels are Title Case (`Add Channel`); titles,
  messages and hints stay in sentence case.

---

# Kotlin / Jetpack Compose member ordering convention

Apply this to **every** Kotlin/Compose file in this project, without exception,
including tests and Gradle Kotlin scripts where the constructs apply. When
applying it to existing code, reorder and re-space only — never change logic,
names, or behaviour.

Ported from the team's Dart convention. Section numbering matches the Dart and
React Native docs, so all three can be diffed side by side.

## 0. What changes vs. Dart

| Dart | Kotlin |
|---|---|
| Fields declared separately from the constructor | Primary-constructor `val`/`var` params **are** the fields |
| `late final x = ...` | `val x by lazy { }` / `lateinit var x` |
| `static` members inline | `companion object` block |
| `State` class with lifecycle overrides | Composable + `remember` / `LaunchedEffect` / `DisposableEffect`, or a state-holder class |

## 1. Member order within a class

Primary-constructor properties come first — they live in the class header, and
the header itself is sorted by §2. In Kotlin the constructor *is* the field
block, which replaces Dart's "constructor at position 8".

Then, in the class body:

1. `companion object` (see §1a)
2. `val` properties
3. `by lazy` properties
4. `lateinit var` properties
5. `var` properties
6. nullable `var` properties
7. `init { }` block
8. secondary constructors
9. computed properties (`val x get() =`) and custom accessors
10. public functions
11. `internal` functions
12. `private` functions
13. `operator` functions
14. **overrides, last** — overridden properties (sorted by §2), then other
    overridden functions by name, then `equals` and `hashCode` adjacent, then
    `toString`, then framework teardown (`onCleared`, `close`) at the very bottom

**Placement rules:**

- Visibility never moves a property between groups 2–6. A `private var` is
  still a property: it stays in group 5/6, not with the private functions.
- An overridden **property** (`override val items`) leaves the property block
  and joins the overrides tail.
- Abstract and `open` members stay in their groups. Abstract functions lead
  their visibility group — they have no body, so no call order.
- Computed properties and custom accessors (group 9) sort by §2.

### 1a. Inside `companion object`

1. `const val`
2. `val`
3. `var`
4. factory functions

Groups 1–3 sorted by §2; factories by call order (§3).

**Placement:** the official Kotlin style guide puts `companion object` last.
This convention puts it first, mirroring the Dart rule that statics lead. See §8.

### 1b. File-level ordering

1. Imports, in the order ktlint's `import-ordering` rule expects (§9)
2. File-private `const val`, sorted by §2
3. Enums, then value classes, then sealed hierarchies — sealed subtypes in name
   order
4. Classes, data classes, and interfaces — each type before the types that
   reference or extend it
5. Public `@Composable` entry point
6. Its `@Composable` children, in call order (§3)
7. `remember*` factory functions
8. State-holder classes
9. Private helpers
10. Extension properties, then extension functions — each sorted by receiver
    type, then name
11. `object` declarations
12. `@Preview` composables, last

## 2. Property ordering inside each group

Sort by **type tier**, then **type name**, then **property name**.

**Tier order:**

1. **Primitives** — `Boolean`, `Byte`, `Char`, `Double`, `Float`, `Int`,
   `Long`, `Short`, `String`, then unsigned variants by name
2. **Collections** — `Array`, `Iterable`, `List`, `Map`, `MutableList`,
   `MutableMap`, `MutableSet`, `Sequence`, `Set`
3. **Everything else** — function types, `Any`, `BigDecimal`, `Duration`,
   `Instant`, `Regex`, `Throwable`, Compose types (`Color`, `Dp`, `Modifier`,
   `TextStyle`), coroutine types (`Flow`, `Job`, `StateFlow`), enums, value
   classes, and all project model classes

**Within a tier:**

- Type name alphabetical, case-insensitive.
- Then property name alphabetical, case-insensitive.
- A nullable type sits immediately after its non-nullable base
  (`String?` right after `String`, `List<String>?` right after `List<String>`).
- Sort on the **full type string**, so type arguments break ties.
  `List<Int>` before `List<String>`.
  **`Flow<Int>` before `FlowCollector`** — `<` (0x3C) sorts before `C` (0x43).
  Counterintuitive; expect to correct it in review.
- Function types start with `(` — place them first in tier 3, before `Any`.
  `() -> Unit` before `(Int) -> Unit` before `(String) -> Boolean`.
  A nullable function type follows its non-nullable shape: `((Int) -> Unit)?`
  sits right after `(Int) -> Unit`.
- SCREAMING_SNAKE constants get no special tier — sort them case-insensitively
  by type first, like everything else. So `IS_STRICT` (Boolean), `TAX_RATE`
  (Double), `MAX_ITEMS` (Int), `PREFIX` (String).
- `Nothing` and `Unit` cannot be property types.
- `lateinit` cannot apply to primitive types, so the only tier-1 type that can
  appear in group 4 is `String`.

## 3. Function ordering

**Properties sort by type. Functions sort by call order.**

Within each visibility group, place functions in four steps:

1. **Construction** — functions called while the object is constructed
   (property initializers, `init` blocks, constructors), in the order first
   called.
2. **Entry points** (public and `internal` groups only) — functions never called
   inside the class, in name order.
3. **Called functions** — functions called from other members, ordered by their
   last call site, reading the class top to bottom.
4. **Unused** (private group only) — functions never called inside the class, in
   name order.

**Refinements:**

- A function called by another function of its own group goes directly after
  that caller — after the last one, if several call it. Several such helpers
  follow their caller in the order it first calls them.
- Passing a function by reference counts as a call (`state::onTextChanged`).

Computed properties sort by §2, like other properties — they have no call order.

Judgment call, not a mechanical rule: two reviewers may order the same class
differently. Accepted — reading order beats sortability for functions.

## 4. Blank lines

- One blank line between each distinct **type** in the property block. `T` and
  `T?` count as distinct types.
- Properties of the *same* type sit together with **no** break between them.
  `val fingerprint: String` and `val slug: String` are adjacent;
  `val checksum: Int` and `val displayName: String` are separated.
- Single-line computed properties follow the same rule.
- Every other member — `init`, constructors, functions, multi-line properties —
  gets one blank line above and below.
- One blank line always separates two groups, even when the types match.
- **No** blank lines inside parameter lists, map literals, or argument lists —
  including the primary constructor, even where the type changes.
- **No** section header comments. Group boundaries are conveyed by the ordering
  itself.

### 4a. Comments

**None.** See the hard constraint above — this project carries no comments in
any source file, of any kind: no `//`, no `/* */`, no KDoc `/** */`, no section
headers, no TODOs, no commented-out code, in Kotlin, Gradle, XML, TOML and
properties alike. That constraint is absolute and it overrides the upstream
convention, which allows a single-line `/* */` comment when genuinely urgent and
exempts KDoc. The section number is kept only so the document still lines up
with the Dart original.

## 5. Compactness

- Single-expression bodies always use `=`, unless that would change the
  inferred return type — `job?.cancel()` would return `Unit?`, and `TODO()`
  would return `Nothing`. Those keep a block body.
- Multi-statement bodies stay expanded.
- Trailing commas on every multi-line parameter and argument list.
- Introduce no line break that ktlint would not require at 240 columns (§9).

## 6. Compose arguments

### At call sites

Named arguments are alphabetical, with two exceptions:

- `modifier` always first (the `key` analogue).
- `content` / trailing lambda always last, hoisted out of the parens.

Example: `modifier`, then `accent`, `draft`, `enabled`, `gutter`,
`onValueChange`, `style`, `subtitle`, `tags`, then `content`.

This covers every named-argument call — composables, framework functions, and
helpers — except the mirrored lists in §7.

### In declarations

Compose's own convention wins over pure alphabetical:

1. required params, in §2 tier order
2. `modifier: Modifier = Modifier`
3. optional params with defaults, in §2 tier order
4. trailing `content: @Composable () -> Unit`, last

## 7. Mirrored lists stay consistent

Primary-constructor params, `copy()` call sites, secondary-constructor
delegation, `toJson` / snapshot keys, `fromJson` arguments, database column
lists, destructuring order, and state-holder constructor calls follow the
**same flat tier + type + name order** as the properties they mirror.

- Sort on the **mirrored property's type**, not the serialised type —
  `createdAt` sorts as `Instant` in a snapshot even though it becomes a string.
- The sort is flat across groups — `val` and `var` properties interleave by type.
- A parameter with no matching property (e.g. `clearMessages`) sorts into its
  type group by name like any other.

## 8. Known trade-offs (accepted)

- `companion object` first contradicts the official Kotlin style guide.
  Accepted for consistency with the Dart codebase.
- Constant order inside `companion object` reads oddly, because it is driven by
  type rather than name.
- Snapshot / JSON key order is driven by Kotlin types, not readability — `id`
  will not be first.
- Alphabetical ordering splits semantically related properties
  (`marginTop` / `paddingTop`).
- Data classes generate `equals` / `hashCode` / `toString`, so the overrides
  tail is usually empty — but constructor order still drives `copy()` and
  destructuring, so order it carefully.
- With no section header comments, the boundary between two adjacent groups is
  invisible when the last member of one and the first of the next have
  different types — it looks like an ordinary type break.
- `hashCode` is a function in Kotlin, so Dart's "getter living with the
  overrides" trade-off disappears.

## 9. Tooling

- No ktlint or detekt rule enforces property ordering — team convention, upheld
  in review.
- Android Studio "Rearrange Code" uses a different scheme. Turn rearranging off
  in the reformat dialog (uncheck *Rearrange entries*).
- No ktlint Gradle plugin is applied, so nothing enforces formatting in the
  build. `.editorconfig` configures the ktlint style the IDE formats to:
  signature wrapping at 240 columns, trailing commas, and import order. It does
  not fight this convention. The settings match `Debitter`. The trailing-comma
  keys are required, because the `intellij_idea` style removes trailing commas
  by default, and Composables must be exempt from the lowercase function-name
  rule:

```editorconfig
[*.{kt,kts}]
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
indent_size = 4
insert_final_newline = true
ktlint_code_style = intellij_idea
ktlint_function_naming_ignore_when_annotated_with = Composable
max_line_length = 240

[*.{xml,toml,properties}]
indent_size = 4
insert_final_newline = true
```

## 10. Retrofitting

`app/src/main/java/` and `app/src/test/java/` are written to this convention
from the start. A violation there is a regression to fix, not legacy to leave
alone.

---

## Reference example

Covers every tier, group, and tie-break case in one file.

```kotlin
package com.example.ordering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import kotlin.time.Duration

private const val DEFAULT_RETRIES: Int = 3

private const val TIMEOUT_LABEL: String = "timeout"

enum class SyncState { FAILED, IDLE, RUNNING, SYNCED }

@JvmInline
value class UserId(val raw: String)

sealed interface SyncEvent {
    data class Failed(val cause: Throwable) : SyncEvent

    data object Started : SyncEvent

    data class Succeeded(val at: Instant) : SyncEvent
}

class KitchenSink(
    val isEnabled: Boolean,
    val isVisible: Boolean,
    val flagOverride: Boolean?,
    val separator: Char,
    val ratio: Double,
    val threshold: Double?,
    val scale: Float,
    val retries: Int,
    val timeoutMs: Long,
    val label: String,
    val name: String,
    val nickname: String?,
    val revision: UInt,
    val rawBytes: Array<Byte>,
    val ids: Iterable<Int>,
    val counts: List<Int>,
    val tags: List<String>,
    val optionalTags: List<String>?,
    val members: List<UserId>,
    val scores: Map<String, Int>,
    val overrides: MutableList<String>,
    val cache: MutableMap<String, ByteArray>,
    val seen: MutableSet<UserId>,
    val stream: Sequence<Int>,
    val unique: Set<String>,
    val onIdle: () -> Unit,
    val onCount: (Int) -> Unit,
    val onRetry: ((Int) -> Unit)?,
    val onValidate: (String) -> Boolean,
    val payload: Any,
    val extra: Any?,
    val amount: BigDecimal,
    val accent: Color,
    val gutter: Dp,
    val ttl: Duration,
    val events: Flow<Int>,
    val collector: FlowCollector<Int>,
    val createdAt: Instant,
    val status: SyncState,
    val bodyStyle: TextStyle,
    val owner: UserId,
) {
    companion object {
        const val IS_STRICT: Boolean = true

        const val TAX_RATE: Double = 0.08

        const val MAX_ITEMS: Int = 50

        const val PREFIX: String = "KS"

        val DEFAULT_TAGS: List<String> = listOf("default")

        val EPOCH: Instant = Instant.EPOCH

        var instanceCount: Int = 0

        fun fromSnapshot(snapshot: Map<String, Any?>): KitchenSink = TODO()

        fun minimal(name: String): KitchenSink = TODO()
    }

    val fingerprint: String = "$PREFIX-$name"
    val slug: String = name.lowercase()

    val defaults: List<String> = DEFAULT_TAGS

    val checksum: Int by lazy { fingerprint.hashCode() }

    val displayName: String by lazy { nickname ?: name }

    val summary: Map<String, String> by lazy { mapOf("name" to name, "slug" to slug) }

    lateinit var sessionId: String

    lateinit var scope: CoroutineScope

    var attempts: Int = 0

    var lastLabel: String = TIMEOUT_LABEL

    var syncState: SyncState = SyncState.IDLE

    var errorText: String? = null

    private var pollJob: Job? = null

    var lastError: Throwable? = null

    init {
        instanceCount++
        seedCache()
    }

    constructor(name: String) : this(
        isEnabled = true,
        isVisible = true,
        flagOverride = null,
        separator = ',',
        ratio = 1.0,
        threshold = null,
        scale = 1f,
        retries = DEFAULT_RETRIES,
        timeoutMs = 30_000L,
        label = name,
        name = name,
        nickname = null,
        revision = 0u,
        rawBytes = emptyArray(),
        ids = emptyList(),
        counts = emptyList(),
        tags = DEFAULT_TAGS,
        optionalTags = null,
        members = emptyList(),
        scores = emptyMap(),
        overrides = mutableListOf(),
        cache = mutableMapOf(),
        seen = mutableSetOf(),
        stream = emptySequence(),
        unique = emptySet(),
        onIdle = {},
        onCount = {},
        onRetry = null,
        onValidate = { it.isNotBlank() },
        payload = Unit,
        extra = null,
        amount = BigDecimal.ZERO,
        accent = Color.Unspecified,
        gutter = 0.dp,
        ttl = Duration.ZERO,
        events = emptyFlow(),
        collector = FlowCollector {},
        createdAt = EPOCH,
        status = SyncState.IDLE,
        bodyStyle = TextStyle.Default,
        owner = UserId(""),
    )

    val canRetry: Boolean get() = attempts < retries && lastError != null
    val isDirty: Boolean get() = overrides.isNotEmpty()

    val itemCount: Int get() = tags.size

    var displayLabel: String
        get() = lastLabel
        set(value) {
            lastLabel = value.trim()
        }

    fun start() {
        syncState = SyncState.RUNNING
        attempts = 0
        poll()
    }

    fun poll() {
        pollJob = scope.launch { drain() }
    }

    fun stop() {
        pollJob?.cancel()
        syncState = SyncState.IDLE
    }

    fun toSnapshot(): Map<String, Any?> = mapOf("isEnabled" to isEnabled, "isVisible" to isVisible, "retries" to retries, "label" to label, "name" to name, "tags" to tags, "createdAt" to createdAt.toString(), "status" to status.name)

    internal fun debugDump(): String = toSnapshot().toString()

    private fun seedCache() = cache.clear()

    private suspend fun drain() = events.collect { value -> record(value) }

    private fun record(value: Int) {
        attempts += value
    }

    private fun formatError(cause: Throwable): String = "${cause::class.simpleName}: ${cause.message}"

    operator fun plus(other: KitchenSink): KitchenSink = TODO()

    override fun equals(other: Any?): Boolean = other is KitchenSink && other.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "KitchenSink($name, $syncState)"
}

data class UserProfile(val isVerified: Boolean, val age: Int, val bio: String, val handle: String, val handleOld: String?, val badges: List<String>, val joinedAt: Instant, val id: UserId) {
    fun redacted(): UserProfile = copy(bio = "", handleOld = null)
}

abstract class Repository<T>(protected val cacheKey: String) {
    abstract val items: StateFlow<List<T>>

    abstract suspend fun refresh()

    open fun invalidate() = Unit
}

class UserRepository(cacheKey: String, private val scope: CoroutineScope) :
    Repository<UserProfile>(cacheKey),
    AutoCloseable {
    private val _items: MutableStateFlow<List<UserProfile>> = MutableStateFlow(emptyList())

    private var refreshJob: Job? = null

    fun prefetch() {
        refreshJob = scope.launch { refresh() }
    }

    private fun merge(incoming: List<UserProfile>): List<UserProfile> = (_items.value + incoming).distinctBy { it.id }

    override val items: StateFlow<List<UserProfile>> = _items

    override fun invalidate() {
        _items.value = emptyList()
    }

    override suspend fun refresh() {
        _items.value = merge(emptyList())
    }

    override fun close() {
        refreshJob?.cancel()
    }
}

@Composable
fun SinkPanel(
    isEnabled: Boolean,
    maxLines: Int,
    title: String,
    tags: List<String>,
    accent: Color,
    gutter: Dp,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    style: TextStyle = TextStyle.Default,
    content: @Composable () -> Unit = {},
) {
    val state = rememberSinkPanelState(maxLines = maxLines, title = title)

    SinkPanelBody(modifier = modifier, accent = accent, draft = state.draft, enabled = isEnabled, gutter = gutter, onValueChange = state::onTextChanged, style = style, subtitle = subtitle, tags = tags, content = content)
}

@Composable
private fun SinkPanelBody(
    enabled: Boolean,
    draft: String,
    subtitle: String?,
    tags: List<String>,
    onValueChange: (String) -> Unit,
    accent: Color,
    gutter: Dp,
    style: TextStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TODO()
}

@Composable
fun rememberSinkPanelState(maxLines: Int, title: String): SinkPanelState {
    val scope = rememberCoroutineScope()
    return remember(scope) { SinkPanelState(maxLines = maxLines, title = title, scope = scope) }
}

@Stable
class SinkPanelState(private val maxLines: Int, private val title: String, private val scope: CoroutineScope) {
    var isDirty: Boolean by mutableStateOf(false)
        private set

    var charCount: Int by mutableStateOf(0)
        private set

    var draft: String by mutableStateOf(title)
        private set

    var errorText: String? by mutableStateOf(null)
        private set

    private var debounceJob: Job? = null

    val canSave: Boolean get() = isDirty && errorText == null

    val remaining: Int get() = maxLines * 80 - charCount

    fun cancel() {
        debounceJob?.cancel()
    }

    fun onTextChanged(value: String) {
        draft = value
        errorText = validate(value)
        isDirty = value != title
        charCount = value.length
    }

    private fun validate(value: String): String? = if (value.isBlank()) "Cannot be empty" else null
}

val UserProfile.initials: String get() = handle.take(2).uppercase()

fun UserProfile.matches(query: String): Boolean = handle.contains(query, ignoreCase = true) || bio.contains(query, ignoreCase = true)

object SyncDefaults {
    const val RETRY_LIMIT: Int = 5

    val BACKOFF: Duration = Duration.ZERO
}
```
