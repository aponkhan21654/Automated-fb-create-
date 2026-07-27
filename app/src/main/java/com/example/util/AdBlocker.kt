package com.example.util

import android.net.Uri
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

object AdBlocker {

    private val BLOCKED_HOSTS = setOf(
        "doubleclick.net",
        "google-analytics.com",
        "googlesyndication.com",
        "adservice.google.com",
        "adnxs.com",
        "taboola.com",
        "outbrain.com",
        "popads.net",
        "adform.net",
        "scorecardresearch.com",
        "amazon-adsystem.com",
        "criteo.com",
        "rubiconproject.com",
        "openx.net",
        "pubmatic.com",
        "quantserve.com",
        "moatads.com",
        "casale-media.com",
        "exoclick.com",
        "adroll.com"
    )

    private val BLOCKED_KEYWORDS = listOf(
        "/pagead/",
        "/adservice/",
        "/adserver/",
        "/ads.js",
        "/telemetry/",
        "googleadservices",
        "smartadserver"
    )

    fun isAdOrTracker(url: String): Boolean {
        if (url.isBlank()) return false
        val lowerUrl = url.lowercase()
        val uri = try { Uri.parse(url) } catch (e: Exception) { null }
        val host = uri?.host?.lowercase() ?: ""

        // Check host match
        for (blockedHost in BLOCKED_HOSTS) {
            if (host == blockedHost || host.endsWith(".$blockedHost")) {
                return true
            }
        }

        // Check path keyword match
        for (keyword in BLOCKED_KEYWORDS) {
            if (lowerUrl.contains(keyword)) {
                return true
            }
        }

        return false
    }

    fun createEmptyResponse(): WebResourceResponse {
        return WebResourceResponse(
            "text/plain",
            "utf-8",
            ByteArrayInputStream(ByteArray(0))
        )
    }
}
