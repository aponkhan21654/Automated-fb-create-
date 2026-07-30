package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.example.automation.AutoFillScriptEngine
import com.example.data.model.WebTab
import com.example.ui.components.BrowserWebView
import com.example.ui.viewmodel.AutoFillViewModel

data class MirrorOption(val name: String, val url: String, val isFast: Boolean = false)

@Composable
fun BrowserAutomationScreen(
    viewModel: AutoFillViewModel
) {
    val context = LocalContext.current
    val savedPassword by viewModel.savedUserPassword.collectAsState()

    val DEFAULT_URL = "https://limited.facebook.com/reg/?logger_id&is_two_steps_login=0&cid=103&next=https%3A%2F%2Fm.facebook.com%2Fconfirmemail.php%3Fnext%3Dhttps%253A%252F%252Fdevelopers.facebook.com%252Fdocumentation%252Ffacebook-login%252Fios%252Flimited-login%26http_ref%3DeyJ0cyI6IjE3ODQ4ODUxNjQ0NjMiLCJyIjoiaHR0cHM6XC9cL3d3dy5nb29nbGUuY29tXC8ifQ%253D%253D%26cah%3D2%26rwtsid%3DVMsmUcMfLX80RBelV&refsrc=deprecated&soft=hjk"

    var currentUrl by remember { mutableStateOf(DEFAULT_URL) }
    var inputUrl by remember(currentUrl) { mutableStateOf(currentUrl) }
    var activeWebView by remember { mutableStateOf<WebView?>(null) }
    var isDesktopMode by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInputText by remember(savedPassword) { mutableStateOf(savedPassword) }
    var isPanelExpanded by remember { mutableStateOf(true) }
    var activeProfile by remember {
        mutableStateOf<com.example.automation.GeneratedProfile?>(
            com.example.automation.ProfileGenerator.generate(
                customPassword = savedPassword,
                passwordMode = com.example.automation.ProfileGenerator.PasswordMode.CUSTOM_FIXED
            )
        )
    }

    fun injectAutoFill() {
        val newProfile = com.example.automation.ProfileGenerator.generate(
            customPassword = savedPassword,
            passwordMode = com.example.automation.ProfileGenerator.PasswordMode.CUSTOM_FIXED
        )
        activeProfile = newProfile
        val randomUrl = AutoFillScriptEngine.getRandomTokenUrl()
        currentUrl = randomUrl
        inputUrl = randomUrl
        activeWebView?.loadUrl(randomUrl)

        val script = AutoFillScriptEngine.buildAutoFillScript(savedPassword, newProfile)
        activeWebView?.evaluateJavascript(script, null)
        Toast.makeText(context, "Opening Token Link & AutoFilling (${newProfile.fullName})...", Toast.LENGTH_SHORT).show()
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            icon = { Icon(imageVector = Icons.Default.Key, contentDescription = null) },
            title = { Text("Set Automation Password") },
            text = {
                Column {
                    Text("Enter the password to be automatically filled into registration forms:", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInputText,
                        onValueChange = { passwordInputText = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (passwordInputText.isNotBlank()) {
                            viewModel.updateSavedPassword(passwordInputText)
                            Toast.makeText(context, "Password Saved!", Toast.LENGTH_SHORT).show()
                        }
                        showPasswordDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val mirrorOptions = remember {
        listOf(
            MirrorOption("⚡ limited.facebook", DEFAULT_URL, isFast = true),
            MirrorOption("web.facebook Token", "https://web.facebook.com/mreg?e_token=Abm-pjJYTVRotRwUm2mvUNcnlg29yW2EJDhquFUbW0XUm_CVx_mxwEam6UMxnehHvuFGPLASa2pmgA&d_hash=FBA71FDC8239E901"),
            MirrorOption("m.facebook", "https://m.facebook.com/reg"),
            MirrorOption("mbasic.facebook", "https://mbasic.facebook.com/reg"),
            MirrorOption("facebook.com", "https://www.facebook.com/r.php")
        )
    }

    val dummyTab = remember(currentUrl, isDesktopMode) {
        WebTab(
            id = "auto_tab",
            url = currentUrl,
            title = "Browser Automation",
            isLoading = isLoading,
            progress = progress,
            isDesktopMode = isDesktopMode
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Minimal Top Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp)
            ) {
                // 6 Top Action Buttons Row (Desktop, Main Link, UID Copy, Password Save, Cookie Save, Clear Web)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // 1. Desktop mode on/off
                    ActionTileButton(
                        icon = Icons.Default.Computer,
                        contentDescription = "Desktop Mode",
                        isActive = isDesktopMode,
                        onClick = {
                            isDesktopMode = !isDesktopMode
                            Toast.makeText(
                                context,
                                if (isDesktopMode) "Desktop Mode ON 🖥️" else "Mobile Mode ON 📱",
                                Toast.LENGTH_SHORT
                            ).show()
                            activeWebView?.reload()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // 2. Main link open
                    ActionTileButton(
                        icon = Icons.Default.Language,
                        contentDescription = "Main Link Open",
                        onClick = {
                            currentUrl = DEFAULT_URL
                            inputUrl = DEFAULT_URL
                            activeWebView?.loadUrl(DEFAULT_URL)
                            Toast.makeText(context, "Opened Main Link 🌐", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // 3. UID copy
                    ActionTileButton(
                        icon = Icons.Default.Person,
                        contentDescription = "UID Copy",
                        onClick = { copyFacebookUidToClipboard(context, currentUrl) },
                        modifier = Modifier.weight(1f)
                    )

                    // 4. Password save
                    ActionTileButton(
                        icon = Icons.Default.Key,
                        contentDescription = "Password Save",
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.weight(1f)
                    )

                    // 5. Cookie save
                    ActionTileButton(
                        icon = Icons.Default.Cookie,
                        contentDescription = "Cookie Save",
                        onClick = { copyCookiesToClipboard(context, currentUrl) },
                        modifier = Modifier.weight(1f)
                    )

                    // 6. Clear web
                    ActionTileButton(
                        icon = Icons.Default.Delete,
                        contentDescription = "Clear Web",
                        onClick = {
                            activeProfile = null
                            clearBrowserData(context, activeWebView, currentUrl)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Mirrors Quick Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = "Mirrors:",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(mirrorOptions) { mirror ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (currentUrl == mirror.url) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    currentUrl = mirror.url
                                    inputUrl = mirror.url
                                    activeWebView?.loadUrl(mirror.url)
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    if (mirror.isFast) {
                                        Icon(
                                            imageVector = Icons.Default.ElectricBolt,
                                            contentDescription = null,
                                            tint = if (currentUrl == mirror.url) Color.Yellow else Color(0xFFFFB300),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                    }
                                    Text(
                                        text = mirror.name,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = if (currentUrl == mirror.url) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            LinearProgressIndicator(
                progress = { (progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // WebView Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            BrowserWebView(
                tab = dummyTab,
                isAdBlockerEnabled = false,
                onPageStarted = { url ->
                    isLoading = true
                    currentUrl = url
                },
                onPageFinished = { url, _ ->
                    isLoading = false
                    currentUrl = url
                    activeProfile?.let { prof ->
                        val script = AutoFillScriptEngine.buildAutoFillScript(savedPassword, prof)
                        activeWebView?.evaluateJavascript(script, null)
                    }
                },
                onProgressChanged = { prog -> progress = prog },
                onReceivedTitle = {},
                onReceivedIcon = {},
                onTrackerBlocked = {},
                onWebViewCreated = { webView -> activeWebView = webView },
                modifier = Modifier.fillMaxSize()
            )

            // Floating Bottom Row with AutoFill & Save Account Buttons
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { injectAutoFill() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("⚡ AutoFill", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        saveAccountToDb(context, currentUrl, savedPassword, activeProfile, viewModel)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("💾 Save Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ActionTileButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        modifier = modifier.height(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (isActive) Color.White else iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun clearBrowserData(context: Context, webView: WebView?, reloadUrl: String? = null) {
    try {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        webView?.apply {
            clearCache(true)
            clearHistory()
            clearFormData()
            clearSslPreferences()
            if (reloadUrl != null) {
                loadUrl(reloadUrl)
            }
        }
        Toast.makeText(context, "Browser cache, cookies & history cleared!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Cleared browser data!", Toast.LENGTH_SHORT).show()
    }
}

private fun formatUrl(url: String): String {
    val trimmed = url.trim()
    return when {
        trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
        trimmed.contains(".") -> "https://$trimmed"
        else -> "https://www.google.com/search?q=$trimmed"
    }
}

private fun copyCookiesToClipboard(context: Context, currentUrl: String) {
    val cookies = try {
        val cm = CookieManager.getInstance()
        var c = cm.getCookie(currentUrl) ?: ""
        if (c.isBlank()) {
            c = cm.getCookie("https://facebook.com")
                ?: cm.getCookie("https://m.facebook.com")
                ?: cm.getCookie("https://web.facebook.com")
                ?: cm.getCookie("https://limited.facebook.com")
                ?: ""
        }
        c
    } catch (e: Exception) {
        ""
    }

    if (cookies.isNotBlank()) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Browser Cookies", cookies)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Cookies copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "No cookies found for current page!", Toast.LENGTH_SHORT).show()
    }
}

private fun copyFacebookUidToClipboard(context: Context, currentUrl: String) {
    val cm = CookieManager.getInstance()
    val domains = listOf(
        currentUrl,
        "https://facebook.com",
        "https://m.facebook.com",
        "https://web.facebook.com",
        "https://limited.facebook.com",
        "https://mbasic.facebook.com"
    )
    val combinedCookies = domains.mapNotNull {
        try { cm.getCookie(it) } catch (e: Exception) { null }
    }.joinToString("; ")

    // 1. Check c_user in cookies
    var uid = Regex("""c_user=(\d+)""").find(combinedCookies)?.groupValues?.get(1)

    // 2. Check i_user in cookies
    if (uid == null) {
        uid = Regex("""i_user=(\d+)""").find(combinedCookies)?.groupValues?.get(1)
    }

    // 3. Check URL query parameters
    if (uid == null) {
        uid = Regex("""[?&](?:id|uid)=(\d+)""").find(currentUrl)?.groupValues?.get(1)
    }

    if (!uid.isNullOrBlank()) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Facebook UID", uid)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "FB UID: $uid copied! 🆔", Toast.LENGTH_SHORT).show()
    } else {
        Toast.makeText(context, "FB UID not found! Please log in first.", Toast.LENGTH_SHORT).show()
    }
}

private fun saveAccountToDb(
    context: Context,
    currentUrl: String,
    savedPassword: String,
    activeProfile: com.example.automation.GeneratedProfile?,
    viewModel: AutoFillViewModel
) {
    val cm = CookieManager.getInstance()
    val domains = listOf(
        currentUrl,
        "https://facebook.com",
        "https://m.facebook.com",
        "https://web.facebook.com",
        "https://limited.facebook.com",
        "https://mbasic.facebook.com"
    )
    val combinedCookies = domains.mapNotNull {
        try { cm.getCookie(it) } catch (e: Exception) { null }
    }.joinToString("; ")

    // Extract UID
    var uid = Regex("""c_user=(\d+)""").find(combinedCookies)?.groupValues?.get(1)
    if (uid == null) {
        uid = Regex("""i_user=(\d+)""").find(combinedCookies)?.groupValues?.get(1)
    }
    if (uid == null) {
        uid = Regex("""[?&](?:id|uid)=(\d+)""").find(currentUrl)?.groupValues?.get(1)
    }
    if (uid.isNullOrBlank()) {
        uid = ""
    }

    // Password
    val password = savedPassword.ifBlank { activeProfile?.password ?: "" }

    // Cookie
    var cookie = cm.getCookie(currentUrl) ?: ""
    if (cookie.isBlank()) {
        cookie = combinedCookies
    }

    viewModel.saveAccount(
        uid = uid,
        password = password,
        cookie = cookie,
        firstName = activeProfile?.firstName ?: "",
        lastName = activeProfile?.lastName ?: ""
    )

    val label = if (uid.isNotBlank()) "UID: $uid" else "Account"
    Toast.makeText(context, "$label Saved to Account Log! 💾", Toast.LENGTH_SHORT).show()
}
