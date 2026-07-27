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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.screens.AccountLogScreen
import com.example.ui.screens.BrowserAutomationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BrowserTheme
import com.example.ui.viewmodel.AutoFillViewModel

enum class NavigationTab { BROWSER, PROFILE, LOG }

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
    var selectedTab by remember { mutableStateOf(NavigationTab.BROWSER) }
    val profile by viewModel.currentProfile.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

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
                    selected = selectedTab == NavigationTab.PROFILE,
                    onClick = { selectedTab = NavigationTab.PROFILE },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Profile Builder") },
                    label = { Text("Profile Builder") }
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
                    viewModel = viewModel,
                    profile = profile
                )
                NavigationTab.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    profile = profile,
                    onNavigateToBrowser = { selectedTab = NavigationTab.BROWSER }
                )
                NavigationTab.LOG -> AccountLogScreen(
                    viewModel = viewModel,
                    accounts = accounts
                )
            }
        }
    }
}
