package com.spongycode.debukker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.DebugModule
import com.spongycode.debukker.ui.theme.DebukkerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugMenu(
    isVisible: Boolean,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    val config by DebugConfigManager.config.collectAsState()
    val modules = config.modules

    var selectedModule by remember(modules) {
        mutableStateOf(modules.firstOrNull() ?: DebugModule.NETWORK_LOGS)
    }

    LaunchedEffect(modules) {
        if (selectedModule !in modules && modules.isNotEmpty()) {
            selectedModule = modules.first()
        }
    }

    DebukkerTheme {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = MaterialTheme.colorScheme.outline.copy(
                        alpha = 0.5f,
                    ),
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Debukker",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp),
                        ),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (modules.isNotEmpty()) {
                    val selectedIndex = modules.indexOf(selectedModule).coerceAtLeast(0)

                    ScrollableTabRow(
                        selectedTabIndex = selectedIndex,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 12.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            if (selectedIndex < tabPositions.size) {
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex])
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                                    height = 3.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    ) {
                        modules.forEach { module ->
                            Tab(
                                selected = selectedModule == module,
                                onClick = { selectedModule = module },
                                selectedContentColor = MaterialTheme.colorScheme.primary,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            module.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            module.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = if (selectedModule == module) FontWeight.Bold else FontWeight.Medium,
                                        )
                                    }
                                },
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        when (selectedModule) {
                            DebugModule.NETWORK_LOGS -> NetworkLogsScreen()
                            DebugModule.MOCK_CONFIG -> MockConfigScreen()
                            DebugModule.ENVIRONMENT -> EnvironmentScreen()
                            DebugModule.PREFERENCES -> PreferencesScreen()
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No modules configured", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
