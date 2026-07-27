package com.example.data.model

enum class SearchEngine(
    val displayName: String,
    val searchUrlPrefix: String,
    val homeUrl: String
) {
    GOOGLE(
        displayName = "Google",
        searchUrlPrefix = "https://www.google.com/search?q=",
        homeUrl = "https://www.google.com"
    ),
    DUCKDUCKGO(
        displayName = "DuckDuckGo",
        searchUrlPrefix = "https://duckduckgo.com/?q=",
        homeUrl = "https://duckduckgo.com"
    ),
    BING(
        displayName = "Bing",
        searchUrlPrefix = "https://www.bing.com/search?q=",
        homeUrl = "https://www.bing.com"
    ),
    BRAVE(
        displayName = "Brave",
        searchUrlPrefix = "https://search.brave.com/search?q=",
        homeUrl = "https://search.brave.com"
    ),
    ECOSIA(
        displayName = "Ecosia",
        searchUrlPrefix = "https://www.ecosia.org/search?q=",
        homeUrl = "https://www.ecosia.org"
    );

    fun buildQueryUrl(query: String): String {
        val trimmed = query.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        if (trimmed.contains(".") && !trimmed.contains(" ")) {
            return "https://$trimmed"
        }
        return "$searchUrlPrefix${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
    }
}
