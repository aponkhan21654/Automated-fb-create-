package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

    val dummyTab = remember(currentUrl) {
        WebTab(
            id = "auto_tab",
            url = currentUrl,
            title = "Browser Automation",
            isLoading = isLoading,
            progress = progress
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
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { activeWebView?.goBack() },
                        enabled = activeWebView?.canGoBack() == true,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { activeWebView?.goForward() },
                        enabled = activeWebView?.canGoForward() == true,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Forward",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Omnibox URL Bar
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            OutlinedTextField(
                                value = inputUrl,
                                onValueChange = { inputUrl = it },
                                placeholder = { Text("Enter URL", fontSize = 13.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                keyboardActions = KeyboardActions(onGo = {
                                    val formatted = formatUrl(inputUrl)
                                    currentUrl = formatted
                                    activeWebView?.loadUrl(formatted)
                                }),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("url_input_field")
                            )

                            if (inputUrl.isNotBlank()) {
                                IconButton(
                                    onClick = { inputUrl = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear URL",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { activeWebView?.reload() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reload",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { clearBrowserData(context, activeWebView, currentUrl) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear Browser",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/TeamWithApon"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram Channel",
                            tint = Color(0xFF229ED9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
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

            // Bottom Automation Overlay Card
            Card(
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    // Header Bar with Toggle / Hide Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isPanelExpanded = !isPanelExpanded }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Automation Control Panel",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { isPanelExpanded = !isPanelExpanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (isPanelExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isPanelExpanded) "Hide Panel" else "Show Panel",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = isPanelExpanded) {
                        Column {
                            Spacer(modifier = Modifier.height(4.dp))

                            // Password Status Row
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Automation Password",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = savedPassword,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { showPasswordDialog = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Set Automation Password",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // PRIMARY ACTION BUTTONS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. AutoFill Button
                                Button(
                                    onClick = { injectAutoFill() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(2f)
                                ) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("⚡ AutoFill", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }

                                // 2. Clear Browser Data
                                OutlinedButton(
                                    onClick = {
                                        activeProfile = null
                                        clearBrowserData(context, activeWebView, currentUrl)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "💡 Click ⚡ AutoFill to automatically fill First Name, Surname, DOB, Gender & Password.",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }
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
