package com.spongycode.debukker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (!isVisible) return
    
    var selectedTab by remember { mutableStateOf(DebugTab.NETWORK_LOGS) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                DebugTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.displayName) }
                    )
                }
            }

            when (selectedTab) {
                DebugTab.NETWORK_LOGS -> NetworkLogsScreen()
                DebugTab.MOCK_CONFIG -> MockConfigScreen()
                DebugTab.ENVIRONMENT -> EnvironmentScreen()
                DebugTab.PREFERENCES -> PreferencesScreen()
            }
        }
    }
}

enum class DebugTab(val displayName: String) {
    NETWORK_LOGS("Network"),
    MOCK_CONFIG("Mocking"),
    ENVIRONMENT("Environment"),
    PREFERENCES("Preferences")
}
