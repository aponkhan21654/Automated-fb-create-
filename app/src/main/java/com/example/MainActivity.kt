package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.security.AppIntegrityGuard
import com.example.ui.components.TamperProtectedLockScreen
import com.example.ui.components.TelegramChannelDialog
import com.example.ui.screens.AccountLogScreen
import com.example.ui.screens.BrowserAutomationScreen
import com.example.ui.theme.BrowserTheme
import com.example.ui.viewmodel.AutoFillViewModel

enum class NavigationTab { BROWSER, LOG }

class MainActivity : ComponentActivity() {

    private val viewModel: AutoFillViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrowserTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: AutoFillViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(NavigationTab.BROWSER) }
    var showTelegramDialog by remember { mutableStateOf(true) }
    var tamperReason by remember { mutableStateOf<String?>(null) }
    val accounts by viewModel.accounts.collectAsState()

    LaunchedEffect(Unit) {
        val integrity = AppIntegrityGuard.verifyIntegrity(context)
        if (integrity is AppIntegrityGuard.IntegrityResult.Tampered) {
            tamperReason = integrity.reason
        }
    }

    // Tamper protection check overlay
    tamperReason?.let { reason ->
        TamperProtectedLockScreen(reason = reason)
    }

    // Telegram Channel popup
    if (showTelegramDialog) {
        TelegramChannelDialog(
            onDismiss = { showTelegramDialog = false }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.BROWSER,
                    onClick = { selectedTab = NavigationTab.BROWSER },
                    icon = { Icon(Icons.Default.Language, contentDescription = "Browser Automation") },
                    label = { Text("Browser Automation") }
                )
                NavigationBarItem(
                    selected = selectedTab == NavigationTab.LOG,
                    onClick = { selectedTab = NavigationTab.LOG },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (accounts.isNotEmpty()) {
                                    Badge {
                                        Text("${accounts.size}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = "Account Log")
                        }
                    },
                    label = { Text("Account Log") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.BROWSER -> BrowserAutomationScreen(
                    viewModel = viewModel
                )
                NavigationTab.LOG -> AccountLogScreen(
                    viewModel = viewModel,
                    accounts = accounts
                )
            }
        }
    }
}

