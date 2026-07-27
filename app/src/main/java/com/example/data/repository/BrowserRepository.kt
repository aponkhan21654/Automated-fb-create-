package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.db.BookmarkEntity
import com.example.data.db.BrowserDao
import com.example.data.db.DownloadEntity
import com.example.data.db.HistoryEntity
import com.example.data.db.ShortcutEntity
import com.example.data.model.SearchEngine
import kotlinx.coroutines.flow.Flow

class BrowserRepository(
    private val browserDao: BrowserDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("browser_settings", Context.MODE_PRIVATE)

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = browserDao.getAllBookmarks()

    suspend fun isBookmarked(url: String): Boolean {
        return browserDao.getBookmarkByUrl(url) != null
    }

    suspend fun addBookmark(title: String, url: String, faviconUrl: String? = null) {
        if (url.isBlank() || url == "about:blank") return
        browserDao.insertBookmark(
            BookmarkEntity(
                title = if (title.isBlank()) url else title,
                url = url,
                faviconUrl = faviconUrl
            )
        )
    }

    suspend fun removeBookmarkByUrl(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun deleteBookmarkById(id: Long) {
        browserDao.deleteBookmarkById(id)
    }

    // History
    val allHistory: Flow<List<HistoryEntity>> = browserDao.getAllHistory()

    fun searchHistory(query: String): Flow<List<HistoryEntity>> = browserDao.searchHistory(query)

    suspend fun addHistory(title: String, url: String, faviconUrl: String? = null) {
        if (url.isBlank() || url == "about:blank" || url.startsWith("chrome://")) return
        browserDao.insertHistory(
            HistoryEntity(
                title = if (title.isBlank()) url else title,
                url = url,
                faviconUrl = faviconUrl
            )
        )
    }

    suspend fun deleteHistoryById(id: Long) {
        browserDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        browserDao.clearAllHistory()
    }

    // Shortcuts
    val allShortcuts: Flow<List<ShortcutEntity>> = browserDao.getAllShortcuts()

    suspend fun addShortcut(title: String, url: String, iconName: String? = "globe") {
        browserDao.insertShortcut(
            ShortcutEntity(
                title = title,
                url = url,
                iconName = iconName,
                isCustom = true
            )
        )
    }

    suspend fun deleteShortcutById(id: Long) {
        browserDao.deleteShortcutById(id)
    }

    // Downloads
    val allDownloads: Flow<List<DownloadEntity>> = browserDao.getAllDownloads()

    suspend fun addDownload(fileName: String, url: String, mimeType: String?, totalBytes: Long) {
        browserDao.insertDownload(
            DownloadEntity(
                fileName = fileName,
                url = url,
                mimeType = mimeType,
                totalBytes = totalBytes,
                status = "COMPLETED"
            )
        )
    }

    suspend fun deleteDownloadById(id: Long) {
        browserDao.deleteDownloadById(id)
    }

    suspend fun clearDownloads() {
        browserDao.clearAllDownloads()
    }

    // Settings
    fun getSearchEngine(): SearchEngine {
        val name = prefs.getString("search_engine", SearchEngine.GOOGLE.name) ?: SearchEngine.GOOGLE.name
        return try { SearchEngine.valueOf(name) } catch (e: Exception) { SearchEngine.GOOGLE }
    }

    fun setSearchEngine(engine: SearchEngine) {
        prefs.edit().putString("search_engine", engine.name).apply()
    }

    fun isAdBlockerEnabled(): Boolean {
        return prefs.getBoolean("ad_blocker_enabled", true)
    }

    fun setAdBlockerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("ad_blocker_enabled", enabled).apply()
    }

    fun isDesktopModeDefault(): Boolean {
        return prefs.getBoolean("desktop_mode_default", false)
    }

    fun setDesktopModeDefault(enabled: Boolean) {
        prefs.edit().putBoolean("desktop_mode_default", enabled).apply()
    }

    fun getBlockedTrackersTotalCount(): Int {
        return prefs.getInt("blocked_trackers_count", 0)
    }

    fun incrementBlockedTrackersCount(byCount: Int = 1) {
        val current = getBlockedTrackersTotalCount()
        prefs.edit().putInt("blocked_trackers_count", current + byCount).apply()
    }
}
