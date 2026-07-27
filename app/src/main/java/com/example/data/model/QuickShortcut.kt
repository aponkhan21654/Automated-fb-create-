package com.example.data.model

data class QuickShortcut(
    val title: String,
    val url: String,
    val category: String = "Popular",
    val iconName: String = "globe"
) {
    companion object {
        val DEFAULT_SHORTCUTS = listOf(
            QuickShortcut("Google", "https://www.google.com", "Search", "search"),
            QuickShortcut("Wikipedia", "https://en.wikipedia.org", "Reference", "book"),
            QuickShortcut("YouTube", "https://m.youtube.com", "Video", "video"),
            QuickShortcut("Reddit", "https://www.reddit.com", "Social", "forum"),
            QuickShortcut("GitHub", "https://github.com", "Developer", "code"),
            QuickShortcut("BBC News", "https://www.bbc.com/news", "News", "newspaper"),
            QuickShortcut("StackOverflow", "https://stackoverflow.com", "Developer", "terminal"),
            QuickShortcut("Weather", "https://weather.com", "Utility", "cloud")
        )
    }
}
