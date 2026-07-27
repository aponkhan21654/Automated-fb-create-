package com.example.data.model

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.UUID

data class WebTab(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "",
    val favicon: Bitmap? = null,
    val isIncognito: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val blockedCount: Int = 0,
    val isDesktopMode: Boolean = false,
    val webView: WebView? = null
) {
    val isHome: Boolean get() = url.isEmpty() || url == "about:blank" || url == "chrome://newtab"
}
