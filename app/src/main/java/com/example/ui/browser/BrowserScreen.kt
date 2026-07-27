package com.example.ui.browser

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BrowserWebView
import com.example.ui.theme.BrowserTheme

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel
) {
    val tabs by viewModel.tabs.collectAsState()
    val activeTabId by viewModel.activeTabId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val searchEngine by viewModel.searchEngine.collectAsState()
    val isAdBlockerEnabled by viewModel.isAdBlockerEnabled.collectAsState()
    val blockedTrackersTotal by viewModel.blockedTrackersTotal.collectAsState()
    val isBookmarked by viewModel.isActiveTabBookmarked.collectAsState()

    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val dbShortcuts by viewModel.dbShortcuts.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val showTabsOverview by viewModel.showTabsOverview.collectAsState()
    val showBookmarksHistory by viewModel.showBookmarksHistory.collectAsState()
    val showMenu by viewModel.showMenu.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showDownloads by viewModel.showDownloads.collectAsState()
    val showFindInPage by viewModel.showFindInPage.collectAsState()

    val findInPageQuery by viewModel.findInPageQuery.collectAsState()
    val findInPageMatchIndex by viewModel.findInPageMatchIndex.collectAsState()
    val findInPageTotalMatches by viewModel.findInPageTotalMatches.collectAsState()

    val currentTab = activeTab
    val isIncognito = currentTab?.isIncognito == true

    BrowserTheme(isIncognito = isIncognito) {
        Scaffold(
            topBar = {
                TopAddressBar(
                    currentTab = currentTab,
                    isBookmarked = isBookmarked,
                    tabCount = tabs.size,
                    onLoadUrl = { viewModel.loadUrl(it) },
                    onReload = { viewModel.reload() },
                    onToggleBookmark = { viewModel.toggleBookmark() },
                    onOpenTabsOverview = { viewModel.toggleTabsOverview(true) },
                    onOpenMenu = { viewModel.toggleMenu(true) }
                )
            },
            bottomBar = {
                BottomNavControlBar(
                    canGoBack = currentTab?.canGoBack == true,
                    canGoForward = currentTab?.canGoForward == true,
                    tabCount = tabs.size,
                    isIncognito = isIncognito,
                    onGoBack = { viewModel.goBack() },
                    onGoForward = { viewModel.goForward() },
                    onGoHome = {
                        val active = activeTab
                        if (active != null) {
                            viewModel.updateTab(active.id) { it.copy(url = "") }
                        }
                    },
                    onOpenTabsOverview = { viewModel.toggleTabsOverview(true) },
                    onOpenMenu = { viewModel.toggleMenu(true) }
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Loading Progress Indicator
                    if (currentTab?.isLoading == true) {
                        LinearProgressIndicator(
                            progress = { (currentTab.progress / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Main Content: New Tab Page or Active WebView
                    if (currentTab == null || currentTab.isHome) {
                        NewTabPage(
                            isIncognito = isIncognito,
                            currentSearchEngine = searchEngine,
                            blockedTrackersCount = blockedTrackersTotal,
                            isAdBlockerEnabled = isAdBlockerEnabled,
                            customShortcuts = dbShortcuts,
                            onSearch = { viewModel.loadUrl(it) },
                            onSelectShortcut = { viewModel.loadUrl(it) },
                            onSelectSearchEngine = { viewModel.setSearchEngine(it) },
                            onAddShortcut = { title, url -> viewModel.addCustomShortcut(title, url) },
                            onOpenBookmarks = { viewModel.toggleBookmarksHistory(true) },
                            onOpenHistory = { viewModel.toggleBookmarksHistory(true) },
                            onOpenDownloads = { viewModel.toggleDownloads(true) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        BrowserWebView(
                            tab = currentTab,
                            isAdBlockerEnabled = isAdBlockerEnabled,
                            onPageStarted = { url -> viewModel.onPageStarted(currentTab.id, url) },
                            onPageFinished = { url, title -> viewModel.onPageFinished(currentTab.id, url, title) },
                            onProgressChanged = { progress ->
                                viewModel.updateTab(currentTab.id) { it.copy(progress = progress) }
                            },
                            onReceivedTitle = { title ->
                                viewModel.updateTab(currentTab.id) { it.copy(title = title) }
                            },
                            onReceivedIcon = { bitmap ->
                                viewModel.updateTab(currentTab.id) { it.copy(favicon = bitmap) }
                            },
                            onTrackerBlocked = { viewModel.onTrackerBlocked() },
                            onWebViewCreated = { webView ->
                                viewModel.updateTab(currentTab.id) { it.copy(webView = webView) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Overlay: Find in Page
                AnimatedVisibility(
                    visible = showFindInPage,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    FindInPageBar(
                        query = findInPageQuery,
                        matchIndex = findInPageMatchIndex,
                        totalMatches = findInPageTotalMatches,
                        onQueryChange = { viewModel.updateFindInPageQuery(it) },
                        onNext = { forward -> viewModel.findNextInPage(forward) },
                        onClose = { viewModel.toggleFindInPage(false) }
                    )
                }
            }
        }

        // Sheets & Dialogs
        if (showTabsOverview) {
            TabsOverviewSheet(
                tabs = tabs,
                activeTabId = activeTabId,
                onSelectTab = { viewModel.switchTab(it) },
                onCloseTab = { viewModel.closeTab(it) },
                onNewTab = { isIncog -> viewModel.addNewTab("", isIncog) },
                onCloseAll = { viewModel.closeAllTabs() },
                onDismiss = { viewModel.toggleTabsOverview(false) }
            )
        }

        if (showBookmarksHistory) {
            BookmarksAndHistorySheet(
                bookmarks = bookmarks,
                history = history,
                onNavigateToUrl = { viewModel.loadUrl(it) },
                onDeleteBookmark = { viewModel.deleteBookmark(it) },
                onDeleteHistoryItem = { viewModel.deleteHistoryItem(it) },
                onClearAllHistory = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.toggleBookmarksHistory(false) }
            )
        }

        if (showMenu) {
            BrowserMenuBottomSheet(
                currentUrl = currentTab?.url ?: "",
                isBookmarked = isBookmarked,
                isDesktopMode = currentTab?.isDesktopMode == true,
                isAdBlockerEnabled = isAdBlockerEnabled,
                onNewTab = { isIncog -> viewModel.addNewTab("", isIncog) },
                onToggleBookmark = { viewModel.toggleBookmark() },
                onOpenBookmarks = { viewModel.toggleBookmarksHistory(true) },
                onOpenHistory = { viewModel.toggleBookmarksHistory(true) },
                onOpenDownloads = { viewModel.toggleDownloads(true) },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onToggleAdBlocker = { viewModel.toggleAdBlocker() },
                onFindInPage = { viewModel.toggleFindInPage(true) },
                onReload = { viewModel.reload() },
                onOpenSettings = { viewModel.toggleSettings(true) },
                onDismiss = { viewModel.toggleMenu(false) }
            )
        }

        if (showSettings) {
            SettingsDialog(
                currentEngine = searchEngine,
                isAdBlockerEnabled = isAdBlockerEnabled,
                isDesktopDefault = false,
                onSelectEngine = { viewModel.setSearchEngine(it) },
                onToggleAdBlocker = { viewModel.toggleAdBlocker() },
                onToggleDesktopDefault = {},
                onClearHistory = { viewModel.clearAllHistory() },
                onDismiss = { viewModel.toggleSettings(false) }
            )
        }

        if (showDownloads) {
            DownloadManagerSheet(
                downloads = downloads,
                onDeleteDownload = { viewModel.deleteDownload(it) },
                onDismiss = { viewModel.toggleDownloads(false) }
            )
        }
    }
}

@Composable
private fun TopAddressBar(
    currentTab: com.example.data.model.WebTab?,
    isBookmarked: Boolean,
    tabCount: Int,
    onLoadUrl: (String) -> Unit,
    onReload: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenTabsOverview: () -> Unit,
    onOpenMenu: () -> Unit
) {
    var textInput by remember(currentTab?.url) {
        mutableStateOf(currentTab?.url ?: "")
    }

    val isSecure = currentTab?.url?.startsWith("https://") == true

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Omnibox URL Container
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (currentTab?.isIncognito == true) Icons.Default.ShieldMoon
                        else if (isSecure) Icons.Default.Lock
                        else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (currentTab?.isIncognito == true) Color(0xFFBB86FC)
                        else if (isSecure) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Search or type URL", fontSize = 14.sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            if (textInput.isNotBlank()) onLoadUrl(textInput)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("omnibox_url_input")
                    )

                    if (currentTab != null && !currentTab.isHome) {
                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Page",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onReload,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload Page",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Tab Counter Badge Button
            IconButton(
                onClick = onOpenTabsOverview,
                modifier = Modifier.testTag("tab_counter_btn")
            ) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("$tabCount", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$tabCount",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // Menu Button
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.testTag("browser_menu_btn")
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Browser Menu")
            }
        }
    }
}

@Composable
private fun BottomNavControlBar(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    isIncognito: Boolean,
    onGoBack: () -> Unit,
    onGoForward: () -> Unit,
    onGoHome: () -> Unit,
    onOpenTabsOverview: () -> Unit,
    onOpenMenu: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onGoBack,
                enabled = canGoBack,
                modifier = Modifier.testTag("nav_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            IconButton(
                onClick = onGoForward,
                enabled = canGoForward,
                modifier = Modifier.testTag("nav_forward_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            IconButton(
                onClick = onGoHome,
                modifier = Modifier.testTag("nav_home_btn")
            ) {
                Icon(imageVector = Icons.Default.Home, contentDescription = "Home Page")
            }

            IconButton(
                onClick = onOpenTabsOverview,
                modifier = Modifier.testTag("nav_tabs_btn")
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(
                            width = 1.5.dp,
                            color = if (isIncognito) Color(0xFFBB86FC) else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = if (isIncognito) Color(0xFFBB86FC) else MaterialTheme.colorScheme.primary
                    )
                }
            }

            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier.testTag("nav_menu_btn")
            ) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu Options")
            }
        }
    }
}
