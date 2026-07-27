package com.example.ui.browser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BookmarkEntity
import com.example.data.db.BrowserDatabase
import com.example.data.db.DownloadEntity
import com.example.data.db.HistoryEntity
import com.example.data.db.ShortcutEntity
import com.example.data.model.QuickShortcut
import com.example.data.model.SearchEngine
import com.example.data.model.WebTab
import com.example.data.repository.BrowserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository

    init {
        val database = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(database.browserDao(), application)
    }

    // Tabs state
    private val _tabs = MutableStateFlow<List<WebTab>>(emptyList())
    val tabs: StateFlow<List<WebTab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String>("")
    val activeTabId: StateFlow<String> = _activeTabId.asStateFlow()

    // Derived active tab
    val activeTab: StateFlow<WebTab?> = combine(_tabs, _activeTabId) { tabs, activeId ->
        tabs.find { it.id == activeId } ?: tabs.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Repository flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbShortcuts: StateFlow<List<ShortcutEntity>> = repository.allShortcuts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Settings
    private val _searchEngine = MutableStateFlow(repository.getSearchEngine())
    val searchEngine: StateFlow<SearchEngine> = _searchEngine.asStateFlow()

    private val _isAdBlockerEnabled = MutableStateFlow(repository.isAdBlockerEnabled())
    val isAdBlockerEnabled: StateFlow<Boolean> = _isAdBlockerEnabled.asStateFlow()

    private val _blockedTrackersTotal = MutableStateFlow(repository.getBlockedTrackersTotalCount())
    val blockedTrackersTotal: StateFlow<Int> = _blockedTrackersTotal.asStateFlow()

    // Active Tab Bookmark State
    private val _isActiveTabBookmarked = MutableStateFlow(false)
    val isActiveTabBookmarked: StateFlow<Boolean> = _isActiveTabBookmarked.asStateFlow()

    // UI overlays
    private val _showTabsOverview = MutableStateFlow(false)
    val showTabsOverview: StateFlow<Boolean> = _showTabsOverview.asStateFlow()

    private val _showBookmarksHistory = MutableStateFlow(false)
    val showBookmarksHistory: StateFlow<Boolean> = _showBookmarksHistory.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu: StateFlow<Boolean> = _showMenu.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    private val _showDownloads = MutableStateFlow(false)
    val showDownloads: StateFlow<Boolean> = _showDownloads.asStateFlow()

    private val _showFindInPage = MutableStateFlow(false)
    val showFindInPage: StateFlow<Boolean> = _showFindInPage.asStateFlow()

    private val _findInPageQuery = MutableStateFlow("")
    val findInPageQuery: StateFlow<String> = _findInPageQuery.asStateFlow()

    private val _findInPageMatchIndex = MutableStateFlow(0)
    val findInPageMatchIndex: StateFlow<Int> = _findInPageMatchIndex.asStateFlow()

    private val _findInPageTotalMatches = MutableStateFlow(0)
    val findInPageTotalMatches: StateFlow<Int> = _findInPageTotalMatches.asStateFlow()

    init {
        // Initialize with default home tab
        addNewTab(url = "", isIncognito = false)
    }

    // Tab Management
    fun addNewTab(url: String = "", isIncognito: Boolean = false) {
        val newTab = WebTab(
            url = url,
            isIncognito = isIncognito,
            isDesktopMode = repository.isDesktopModeDefault()
        )
        _tabs.update { it + newTab }
        _activeTabId.value = newTab.id
        checkBookmarkState(newTab.url)
    }

    fun closeTab(tabId: String) {
        val currentTabs = _tabs.value
        if (currentTabs.size <= 1) {
            // If closing last tab, replace with clean new tab
            val replacement = WebTab(isDesktopMode = repository.isDesktopModeDefault())
            _tabs.value = listOf(replacement)
            _activeTabId.value = replacement.id
            checkBookmarkState(replacement.url)
            return
        }

        val closingIndex = currentTabs.indexOfFirst { it.id == tabId }
        val newTabs = currentTabs.filterNot { it.id == tabId }
        _tabs.value = newTabs

        if (_activeTabId.value == tabId) {
            val nextIndex = (closingIndex - 1).coerceAtLeast(0).coerceAtMost(newTabs.lastIndex)
            _activeTabId.value = newTabs[nextIndex].id
            checkBookmarkState(newTabs[nextIndex].url)
        }
    }

    fun switchTab(tabId: String) {
        _activeTabId.value = tabId
        _showTabsOverview.value = false
        val tab = _tabs.value.find { it.id == tabId }
        if (tab != null) {
            checkBookmarkState(tab.url)
        }
    }

    fun closeAllTabs() {
        val newTab = WebTab(isDesktopMode = repository.isDesktopModeDefault())
        _tabs.value = listOf(newTab)
        _activeTabId.value = newTab.id
        _showTabsOverview.value = false
        checkBookmarkState("")
    }

    fun updateTab(tabId: String, transform: (WebTab) -> WebTab) {
        _tabs.update { currentTabs ->
            currentTabs.map { tab ->
                if (tab.id == tabId) {
                    val updated = transform(tab)
                    if (tab.id == _activeTabId.value && updated.url != tab.url) {
                        checkBookmarkState(updated.url)
                    }
                    updated
                } else {
                    tab
                }
            }
        }
    }

    // Navigation & URL loading
    fun loadUrl(input: String) {
        val active = activeTab.value ?: return
        val targetUrl = searchEngine.value.buildQueryUrl(input)

        updateTab(active.id) { tab ->
            tab.copy(url = targetUrl, isLoading = true, progress = 10)
        }

        active.webView?.loadUrl(targetUrl)
        checkBookmarkState(targetUrl)
    }

    fun goBack() {
        val active = activeTab.value ?: return
        if (active.webView?.canGoBack() == true) {
            active.webView?.goBack()
        }
    }

    fun goForward() {
        val active = activeTab.value ?: return
        if (active.webView?.canGoForward() == true) {
            active.webView?.goForward()
        }
    }

    fun reload() {
        val active = activeTab.value ?: return
        active.webView?.reload()
    }

    // History & Bookmarks logic
    fun onPageStarted(tabId: String, url: String) {
        updateTab(tabId) { tab ->
            tab.copy(url = url, isLoading = true, progress = 15)
        }
    }

    private fun isBlankTitle(str: String?): Boolean {
        return str.isNullOrBlank() || str == "about:blank"
    }

    fun onPageFinished(tabId: String, url: String, title: String?) {
        val tabTitle = if (!isBlankTitle(title)) title!! else url
        updateTab(tabId) { tab ->
            tab.copy(
                url = url,
                title = tabTitle,
                isLoading = false,
                progress = 100,
                canGoBack = tab.webView?.canGoBack() ?: false,
                canGoForward = tab.webView?.canGoForward() ?: false
            )
        }

        val tab = _tabs.value.find { it.id == tabId }
        if (tab != null && !tab.isIncognito && url.isNotBlank() && !url.startsWith("chrome://")) {
            viewModelScope.launch {
                repository.addHistory(title = tabTitle, url = url)
            }
        }

        if (tabId == _activeTabId.value) {
            checkBookmarkState(url)
        }
    }

    fun checkBookmarkState(url: String) {
        if (url.isBlank() || url == "about:blank") {
            _isActiveTabBookmarked.value = false
            return
        }
        viewModelScope.launch {
            _isActiveTabBookmarked.value = repository.isBookmarked(url)
        }
    }

    fun toggleBookmark() {
        val active = activeTab.value ?: return
        if (active.url.isBlank() || active.url == "about:blank") return

        viewModelScope.launch {
            if (_isActiveTabBookmarked.value) {
                repository.removeBookmarkByUrl(active.url)
                _isActiveTabBookmarked.value = false
            } else {
                repository.addBookmark(
                    title = if (active.title.isBlank()) active.url else active.title,
                    url = active.url
                )
                _isActiveTabBookmarked.value = true
            }
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            repository.deleteBookmarkById(id)
            val active = activeTab.value
            if (active != null) checkBookmarkState(active.url)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Ad blocker
    fun toggleAdBlocker() {
        val current = _isAdBlockerEnabled.value
        val newState = !current
        _isAdBlockerEnabled.value = newState
        repository.setAdBlockerEnabled(newState)
    }

    fun onTrackerBlocked() {
        val updated = _blockedTrackersTotal.value + 1
        _blockedTrackersTotal.value = updated
        repository.incrementBlockedTrackersCount(1)

        val activeId = _activeTabId.value
        if (activeId.isNotEmpty()) {
            updateTab(activeId) { tab ->
                tab.copy(blockedCount = tab.blockedCount + 1)
            }
        }
    }

    // Desktop Mode
    fun toggleDesktopMode() {
        val active = activeTab.value ?: return
        val newDesktopState = !active.isDesktopMode
        updateTab(active.id) { tab ->
            tab.copy(isDesktopMode = newDesktopState)
        }
        active.webView?.let { webView ->
            val settings = webView.settings
            val desktopUA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
            val mobileUA = settings.userAgentString.replace("X11; Linux x86_64", "Android")
            settings.userAgentString = if (newDesktopState) desktopUA else null
            settings.useWideViewPort = newDesktopState
            settings.loadWithOverviewMode = newDesktopState
            webView.reload()
        }
    }

    // Search Engine
    fun setSearchEngine(engine: SearchEngine) {
        _searchEngine.value = engine
        repository.setSearchEngine(engine)
    }

    // Shortcuts
    fun addCustomShortcut(title: String, url: String) {
        viewModelScope.launch {
            repository.addShortcut(title, url, "globe")
        }
    }

    fun deleteShortcut(id: Long) {
        viewModelScope.launch {
            repository.deleteShortcutById(id)
        }
    }

    // Downloads
    fun recordDownload(fileName: String, url: String, mimeType: String?, sizeBytes: Long) {
        viewModelScope.launch {
            repository.addDownload(fileName, url, mimeType, sizeBytes)
        }
    }

    fun deleteDownload(id: Long) {
        viewModelScope.launch {
            repository.deleteDownloadById(id)
        }
    }

    // Find in page
    fun toggleFindInPage(show: Boolean) {
        _showFindInPage.value = show
        if (!show) {
            _findInPageQuery.value = ""
            activeTab.value?.webView?.clearMatches()
        }
    }

    fun updateFindInPageQuery(query: String) {
        _findInPageQuery.value = query
        val webView = activeTab.value?.webView ?: return
        if (query.isBlank()) {
            webView.clearMatches()
            _findInPageMatchIndex.value = 0
            _findInPageTotalMatches.value = 0
        } else {
            webView.findAllAsync(query)
        }
    }

    fun findNextInPage(forward: Boolean) {
        activeTab.value?.webView?.findNext(forward)
    }

    fun onFindMatchesFound(activeMatchOrdinal: Int, numberOfMatches: Int) {
        _findInPageMatchIndex.value = activeMatchOrdinal
        _findInPageTotalMatches.value = numberOfMatches
    }

    // Overlay Toggles
    fun toggleTabsOverview(show: Boolean) { _showTabsOverview.value = show }
    fun toggleBookmarksHistory(show: Boolean) { _showBookmarksHistory.value = show }
    fun toggleMenu(show: Boolean) { _showMenu.value = show }
    fun toggleSettings(show: Boolean) { _showSettings.value = show }
    fun toggleDownloads(show: Boolean) { _showDownloads.value = show }
}
