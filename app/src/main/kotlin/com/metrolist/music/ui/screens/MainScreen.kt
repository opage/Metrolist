package com.metrolist.music.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.metrolist.innertube.models.SongItem
import com.metrolist.music.BuildConfig
import com.metrolist.music.R
import com.metrolist.music.constants.AppBarHeight
import com.metrolist.music.constants.DefaultOpenTabKey
import com.metrolist.music.constants.MiniPlayerHeight
import com.metrolist.music.constants.MiniPlayerBottomSpacing
import com.metrolist.music.constants.UseNewMiniPlayerDesignKey
import com.metrolist.music.constants.NavigationBarAnimationSpec
import com.metrolist.music.constants.NavigationBarHeight
import com.metrolist.music.constants.PauseSearchHistoryKey
import com.metrolist.music.constants.SearchSource
import com.metrolist.music.constants.SearchSourceKey
import com.metrolist.music.constants.SlimNavBarHeight
import com.metrolist.music.constants.SlimNavBarKey
import com.metrolist.music.db.MusicDatabase
import com.metrolist.music.db.entities.SearchHistory
import com.metrolist.music.extensions.toEnum
import com.metrolist.music.playback.DownloadUtil
import com.metrolist.music.playback.PlayerConnection
import com.metrolist.music.ui.component.AccountSettingsDialog
import com.metrolist.music.ui.component.BottomSheetMenu
import com.metrolist.music.ui.component.BottomSheetPage
import com.metrolist.music.ui.component.LocalBottomSheetPageState
import com.metrolist.music.ui.component.LocalMenuState
import com.metrolist.music.ui.component.TopSearch
import com.metrolist.music.ui.component.rememberBottomSheetState
import com.metrolist.music.ui.component.shimmer.ShimmerTheme
import com.metrolist.music.ui.menu.YouTubeSongMenu
import com.metrolist.music.ui.player.BottomSheetPlayer
import com.metrolist.music.ui.screens.search.LocalSearchScreen
import com.metrolist.music.ui.screens.search.OnlineSearchScreen
import com.metrolist.music.ui.screens.settings.NavigationTab
import com.metrolist.music.ui.utils.appBarScrollBehavior
import com.metrolist.music.ui.utils.resetHeightOffset
import com.metrolist.music.utils.SyncUtils
import com.metrolist.music.utils.dataStore
import com.metrolist.music.utils.get
import com.metrolist.music.utils.rememberEnumPreference
import com.metrolist.music.utils.rememberPreference
import com.metrolist.music.viewmodels.HomeViewModel
import com.valentinilk.shimmer.LocalShimmerTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    database: MusicDatabase,
    playerConnection: PlayerConnection?,
    downloadUtil: DownloadUtil,
    syncUtils: SyncUtils,
    pureBlack: Boolean,
    latestVersionName: String,
    onNewIntentListener: (Consumer<Intent>) -> Unit,
    onRemoveNewIntentListener: (Consumer<Intent>) -> Unit,
    onHandleDeepLink: (Intent, NavHostController) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val windowsInsets = WindowInsets.systemBars
    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }
    val bottomInsetDp = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()

    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val accountImageUrl by homeViewModel.accountImageUrl.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val (previousTab) = rememberSaveable { mutableStateOf("home") }

    val navigationItems = remember { Screens.MainScreens }
    val (slimNav) = rememberPreference(SlimNavBarKey, defaultValue = false)
    val (useNewMiniPlayerDesign) = rememberPreference(UseNewMiniPlayerDesignKey, defaultValue = true)
    val defaultOpenTab =
        remember {
            dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME)
        }

    val topLevelScreens =
        listOf(
            Screens.Home.route,
            Screens.Search.route,
            Screens.Library.route,
            "settings",
        )

    val (query, onQueryChange) =
        rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue())
        }

    var active by rememberSaveable {
        mutableStateOf(false)
    }

    val onActiveChange: (Boolean) -> Unit = { newActive ->
        active = newActive
        if (!newActive) {
            focusManager.clearFocus()
            if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                onQueryChange(TextFieldValue())
            }
        }
    }

    var searchSource by rememberEnumPreference(SearchSourceKey, SearchSource.ONLINE)

    val searchBarFocusRequester = remember { FocusRequester() }

    val onSearch: (String) -> Unit = {
        if (it.isNotEmpty()) {
            onActiveChange(false)
            navController.navigate("search/${URLEncoder.encode(it, "UTF-8")}")
            if (dataStore[PauseSearchHistoryKey] != true) {
                database.query {
                    insert(SearchHistory(query = it))
                }
            }
        }
    }

    var openSearchImmediately: Boolean by remember {
        mutableStateOf(false)
    }

    val shouldShowSearchBar =
        remember(active, navBackStackEntry) {
            active ||
                    navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } ||
                    navBackStackEntry?.destination?.route?.startsWith("search/") == true
        }

    val shouldShowNavigationBar =
        remember(navBackStackEntry, active) {
            navBackStackEntry?.destination?.route == null ||
                    navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } &&
                    !active
        }

    fun getNavPadding(): Dp {
        return if (shouldShowNavigationBar) {
            if (slimNav) SlimNavBarHeight else NavigationBarHeight
        } else {
            0.dp
        }
    }

    val navigationBarHeight by animateDpAsState(
        targetValue = if (shouldShowNavigationBar) NavigationBarHeight else 0.dp,
        animationSpec = NavigationBarAnimationSpec,
        label = "",
    )

    val playerBottomSheetState =
        rememberBottomSheetState(
            dismissedBound = 0.dp,
            collapsedBound = bottomInset + getNavPadding() + (if (useNewMiniPlayerDesign) MiniPlayerBottomSpacing else 0.dp) + MiniPlayerHeight,
            expandedBound = with(LocalDensity.current) { 
                // We need to get maxHeight from BoxWithConstraints context
                // This will be handled in the parent composable
                2000.dp // Placeholder value
            },
        )

    val playerAwareWindowInsets =
        remember(
            bottomInset,
            shouldShowNavigationBar,
            playerBottomSheetState.isDismissed,
        ) {
            var bottom = bottomInset
            if (shouldShowNavigationBar) bottom += NavigationBarHeight
            if (!playerBottomSheetState.isDismissed) bottom += MiniPlayerHeight
            windowsInsets
                .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                .add(WindowInsets(top = AppBarHeight, bottom = bottom))
        }

    val searchBarScrollBehavior =
        appBarScrollBehavior(
            canScroll = {
                navBackStackEntry?.destination?.route?.startsWith("search/") == false &&
                        (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
            },
        )
    val topAppBarScrollBehavior =
        appBarScrollBehavior(
            canScroll = {
                navBackStackEntry?.destination?.route?.startsWith("search/") == false &&
                        (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
            },
        )

    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
            val searchQuery =
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    if (navBackStackEntry
                            ?.arguments
                            ?.getString(
                                "query",
                            )!!
                            .contains(
                                "%",
                            )
                    ) {
                        navBackStackEntry?.arguments?.getString(
                            "query",
                        )!!
                    } else {
                        URLDecoder.decode(
                            navBackStackEntry?.arguments?.getString("query")!!,
                            "UTF-8"
                        )
                    }
                }
            onQueryChange(
                TextFieldValue(
                    searchQuery,
                    TextRange(searchQuery.length)
                )
            )
        } else if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
            onQueryChange(TextFieldValue())
        }
        searchBarScrollBehavior.state.resetHeightOffset()
        topAppBarScrollBehavior.state.resetHeightOffset()
    }
    LaunchedEffect(active) {
        if (active) {
            searchBarScrollBehavior.state.resetHeightOffset()
            topAppBarScrollBehavior.state.resetHeightOffset()
            searchBarFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(playerConnection) {
        val player = playerConnection?.player ?: return@LaunchedEffect
        if (player.currentMediaItem == null) {
            if (!playerBottomSheetState.isDismissed) {
                playerBottomSheetState.dismiss()
            }
        } else {
            if (playerBottomSheetState.isDismissed) {
                playerBottomSheetState.collapseSoft()
            }
        }
    }

    DisposableEffect(playerConnection, playerBottomSheetState) {
        val player =
            playerConnection?.player ?: return@DisposableEffect onDispose { }
        val listener =
            object : Player.Listener {
                override fun onMediaItemTransition(
                    mediaItem: MediaItem?,
                    reason: Int,
                ) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED &&
                        mediaItem != null &&
                        playerBottomSheetState.isDismissed
                    ) {
                        playerBottomSheetState.collapseSoft()
                    }
                }
            }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }

    var shouldShowTopBar by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(navBackStackEntry) {
        shouldShowTopBar =
            !active && navBackStackEntry?.destination?.route in topLevelScreens && navBackStackEntry?.destination?.route != "settings"
    }

    val coroutineScope = rememberCoroutineScope()
    var sharedSong: SongItem? by remember {
        mutableStateOf(null)
    }

    DisposableEffect(Unit) {
        val listener = Consumer<Intent> { intent ->
            onHandleDeepLink(intent, navController)
        }

        onNewIntentListener(listener)
        onDispose { onRemoveNewIntentListener(listener) }
    }

    val currentTitleRes = remember(navBackStackEntry) {
        when (navBackStackEntry?.destination?.route) {
            Screens.Home.route -> R.string.home
            Screens.Search.route -> R.string.search
            Screens.Library.route -> R.string.filter_library
            else -> null
        }
    }

    var showAccountDialog by remember { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalDatabase provides database,
        LocalContentColor provides if (pureBlack) Color.White else contentColorFor(MaterialTheme.colorScheme.surface),
        LocalPlayerConnection provides playerConnection,
        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
        LocalDownloadUtil provides downloadUtil,
        LocalShimmerTheme provides ShimmerTheme,
        LocalSyncUtils provides syncUtils,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            // Update playerBottomSheetState with actual maxHeight
            LaunchedEffect(maxHeight) {
                // This would require modifying the bottom sheet state
                // For now, we'll work with the existing structure
            }

            Scaffold(
                topBar = {
                    if (shouldShowTopBar) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = currentTitleRes?.let { stringResource(it) }
                                        ?: "",
                                    style = MaterialTheme.typography.titleLarge,
                                )
                            },
                            actions = {
                                IconButton(onClick = { navController.navigate("history") }) {
                                    Icon(
                                        painter = painterResource(R.drawable.history),
                                        contentDescription = stringResource(R.string.history)
                                    )
                                }
                                IconButton(onClick = { navController.navigate("stats") }) {
                                    Icon(
                                        painter = painterResource(R.drawable.stats),
                                        contentDescription = stringResource(R.string.stats)
                                    )
                                }
                                IconButton(onClick = { showAccountDialog = true }) {
                                    BadgedBox(badge = {
                                        if (latestVersionName != BuildConfig.VERSION_NAME) {
                                            Badge()
                                        }
                                    }) {
                                        if (accountImageUrl != null) {
                                            AsyncImage(
                                                model = accountImageUrl,
                                                contentDescription = stringResource(R.string.account),
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Icon(
                                                painter = painterResource(R.drawable.account),
                                                contentDescription = stringResource(R.string.account),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            scrollBehavior =
                            searchBarScrollBehavior,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                scrolledContainerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                    AnimatedVisibility(
                        visible = active || navBackStackEntry?.destination?.route?.startsWith("search/") == true,
                        enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = fadeOut(animationSpec = tween(durationMillis = 200))
                    ) {
                        TopSearch(
                            query = query,
                            onQueryChange = onQueryChange,
                            onSearch = onSearch,
                            active = active,
                            onActiveChange = onActiveChange,
                            placeholder = {
                                Text(
                                    text = stringResource(
                                        when (searchSource) {
                                            SearchSource.LOCAL -> R.string.search_library
                                            SearchSource.ONLINE -> R.string.search_yt_music
                                        }
                                    ),
                                )
                            },
                            leadingIcon = {
                                com.metrolist.music.ui.component.IconButton(
                                    onClick = {
                                        when {
                                            active -> onActiveChange(false)
                                            !navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } -> {
                                                navController.navigateUp()
                                            }

                                            else -> onActiveChange(true)
                                        }
                                    },
                                    onLongClick = {
                                        when {
                                            active -> {}
                                            !navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } -> {
                                                com.metrolist.music.ui.utils.backToMain(navController)
                                            }
                                            else -> {}
                                        }
                                    },
                                ) {
                                    Icon(
                                        painterResource(
                                            if (active ||
                                                !navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }
                                            ) {
                                                R.drawable.arrow_back
                                            } else {
                                                R.drawable.search
                                            },
                                        ),
                                        contentDescription = null,
                                    )
                                }
                            },
                            trailingIcon = {
                                Row {
                                    if (active) {
                                        if (query.text.isNotEmpty()) {
                                            IconButton(
                                                onClick = {
                                                    onQueryChange(
                                                        TextFieldValue(
                                                            ""
                                                        )
                                                    )
                                                },
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.close),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                searchSource =
                                                    if (searchSource == SearchSource.ONLINE) SearchSource.LOCAL else SearchSource.ONLINE
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    when (searchSource) {
                                                        SearchSource.LOCAL -> R.drawable.library_music
                                                        SearchSource.ONLINE -> R.drawable.language
                                                    },
                                                ),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                }
                            },
                            modifier =
                            Modifier
                                .focusRequester(searchBarFocusRequester)
                                .align(Alignment.TopCenter),
                            focusRequester = searchBarFocusRequester,
                            colors = if (pureBlack && active) {
                                SearchBarDefaults.colors(
                                    containerColor = Color.Black,
                                    dividerColor = Color.DarkGray,
                                    inputFieldColors = TextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.Gray,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        cursorColor = Color.White,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            } else {
                                SearchBarDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            }
                        ) {
                            Crossfade(
                                targetState = searchSource,
                                label = "",
                                modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (!playerBottomSheetState.isDismissed) MiniPlayerHeight else 0.dp)
                                    .navigationBarsPadding(),
                            ) { searchSource ->
                                when (searchSource) {
                                    SearchSource.LOCAL ->
                                        LocalSearchScreen(
                                            query = query.text,
                                            navController = navController,
                                            onDismiss = { onActiveChange(false) },
                                            pureBlack = pureBlack,
                                        )

                                    SearchSource.ONLINE ->
                                        OnlineSearchScreen(
                                            query = query.text,
                                            onQueryChange = onQueryChange,
                                            navController = navController,
                                            onSearch = {
                                                navController.navigate(
                                                    "search/${
                                                        URLEncoder.encode(
                                                            it,
                                                            "UTF-8"
                                                        )
                                                    }"
                                                )
                                                if (dataStore[PauseSearchHistoryKey] != true) {
                                                    database.query {
                                                        insert(SearchHistory(query = it))
                                                    }
                                                }
                                            },
                                            onDismiss = { onActiveChange(false) },
                                            pureBlack = pureBlack
                                        )
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    Box {
                        BottomSheetPlayer(
                            state = playerBottomSheetState,
                            navController = navController,
                            pureBlack = pureBlack
                        )
                        NavigationBar(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(bottomInset + getNavPadding())
                                .offset {
                                    if (navigationBarHeight == 0.dp) {
                                        IntOffset(
                                            x = 0,
                                            y = (bottomInset + NavigationBarHeight).roundToPx(),
                                        )
                                    } else {
                                        val slideOffset =
                                            (bottomInset + NavigationBarHeight) *
                                                    playerBottomSheetState.progress.coerceIn(
                                                        0f,
                                                        1f,
                                                    )
                                        val hideOffset =
                                            (bottomInset + NavigationBarHeight) * (1 - navigationBarHeight / NavigationBarHeight)
                                        IntOffset(
                                            x = 0,
                                            y = (slideOffset + hideOffset).roundToPx(),
                                        )
                                    }
                                },
                            containerColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            navigationItems.fastForEach { screen ->
                                val isSelected =
                                    navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true

                                NavigationBarItem(
                                    selected = isSelected,
                                    icon = {
                                        Icon(
                                            painter = painterResource(
                                                id = if (isSelected) screen.iconIdActive else screen.iconIdInactive
                                            ),
                                            contentDescription = null,
                                        )
                                    },
                                    label = {
                                        if (!slimNav) {
                                            Text(
                                                text = stringResource(screen.titleId),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    },
                                    onClick = {
                                        if (screen.route == Screens.Search.route) {
                                            onActiveChange(true)
                                        } else if (isSelected) {
                                            navController.currentBackStackEntry?.savedStateHandle?.set("scrollToTop", true)
                                            coroutineScope.launch {
                                                searchBarScrollBehavior.state.resetHeightOffset()
                                            }
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                )
                            }
                        }
                        val baseBg = if (pureBlack) Color.Black else MaterialTheme.colorScheme.surfaceContainer
                        val insetBg = if (playerBottomSheetState.progress > 0f) Color.Transparent else baseBg

                        Box(
                            modifier = Modifier
                                .background(insetBg)
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .height(bottomInsetDp)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(searchBarScrollBehavior.nestedScrollConnection)
            ) {
                var transitionDirection =
                    AnimatedContentTransitionScope.SlideDirection.Left

                if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route }) {
                    if (navigationItems.fastAny { it.route == previousTab }) {
                        val curIndex = navigationItems.indexOf(
                            navigationItems.fastFirstOrNull {
                                it.route == navBackStackEntry?.destination?.route
                            }
                        )

                        val prevIndex = navigationItems.indexOf(
                            navigationItems.fastFirstOrNull {
                                it.route == previousTab
                            }
                        )

                        if (prevIndex > curIndex)
                            AnimatedContentTransitionScope.SlideDirection.Right.also {
                                transitionDirection = it
                            }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = when (defaultOpenTab) {
                        NavigationTab.HOME -> Screens.Home
                        NavigationTab.LIBRARY -> Screens.Library
                        else -> Screens.Home
                    }.route,
                    enterTransition = {
                        if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                            fadeIn(tween(250))
                        } else {
                            fadeIn(tween(250)) + slideInHorizontally { it / 2 }
                        }
                    },
                    exitTransition = {
                        if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                            fadeOut(tween(200))
                        } else {
                            fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
                        }
                    },
                    popEnterTransition = {
                        if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith("search/") == true) && targetState.destination.route in topLevelScreens) {
                            fadeIn(tween(250))
                        } else {
                            fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
                        }
                    },
                    popExitTransition = {
                        if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith("search/") == true) && targetState.destination.route in topLevelScreens) {
                            fadeOut(tween(200))
                        } else {
                            fadeOut(tween(200)) + slideOutHorizontally { it / 2 }
                        }
                    },
                    modifier = Modifier.nestedScroll(
                        if (navigationItems.fastAny { it.route == navBackStackEntry?.destination?.route } ||
                            navBackStackEntry?.destination?.route?.startsWith("search/") == true
                        ) {
                            searchBarScrollBehavior.nestedScrollConnection
                        } else {
                            topAppBarScrollBehavior.nestedScrollConnection
                        }
                    )
                ) {
                    navigationBuilder(
                        navController,
                        topAppBarScrollBehavior,
                        latestVersionName
                    )
                }
            }

            BottomSheetMenu(
                state = LocalMenuState.current,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            BottomSheetPage(
                state = LocalBottomSheetPageState.current,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            if (showAccountDialog) {
                AccountSettingsDialog(
                    navController = navController,
                    onDismiss = { showAccountDialog = false },
                    latestVersionName = latestVersionName
                )
            }

            sharedSong?.let { song ->
                playerConnection?.let {
                    Dialog(
                        onDismissRequest = { sharedSong = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false),
                    ) {
                        Surface(
                            modifier = Modifier.padding(24.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = AlertDialogDefaults.containerColor,
                            tonalElevation = AlertDialogDefaults.TonalElevation,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                YouTubeSongMenu(
                                    song = song,
                                    navController = navController,
                                    onDismiss = { sharedSong = null },
                                )
                            }
                        }
                    }
                }
            }

            LaunchedEffect(shouldShowSearchBar, openSearchImmediately) {
                if (shouldShowSearchBar && openSearchImmediately) {
                    onActiveChange(true)
                    try {
                        delay(100)
                        searchBarFocusRequester.requestFocus()
                    } catch (_: Exception) {
                    }
                    openSearchImmediately = false
                }
            }
        }
    }
}

// Composition locals that were previously defined in MainActivity
val LocalDatabase = androidx.compose.runtime.staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalPlayerConnection =
    androidx.compose.runtime.staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets =
    androidx.compose.runtime.compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalDownloadUtil = androidx.compose.runtime.staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }
val LocalSyncUtils = androidx.compose.runtime.staticCompositionLocalOf<SyncUtils> { error("No SyncUtils provided") }