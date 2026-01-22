package com.spongycode.debukker.debug.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spongycode.debukker.debug.DebugConfigManager

@Composable
fun MockConfigScreen() {
    val config by DebugConfigManager.config.collectAsState()
    var throttleInput by remember { mutableStateOf(config.throttleMs.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Network Mocking",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (config.isOfflineMode) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Offline Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Block all network requests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = config.isOfflineMode,
                    onCheckedChange = { DebugConfigManager.setOfflineMode(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Network Throttle",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Add artificial delay to all requests",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = throttleInput,
                        onValueChange = { throttleInput = it },
                        label = { Text("Delay (ms)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            throttleInput.toLongOrNull()?.let { delay ->
                                DebugConfigManager.setThrottle(delay)
                            }
                        }
                    ) {
                        Text("Apply")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0L, 500L, 1000L, 3000L).forEach { delay ->
                        OutlinedButton(
                            onClick = {
                                throttleInput = delay.toString()
                                DebugConfigManager.setThrottle(delay)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${delay}ms", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (config.throttleMs > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "⏱ Current delay: ${config.throttleMs}ms",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Request & Response Mocks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Active Mocks: ${config.requestMocks.size + config.responseMocks.size}",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (config.requestMocks.isNotEmpty() || config.responseMocks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { DebugConfigManager.clearAllMocks() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Clear All Mocks")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Note: Mock creation UI coming soon. Mocks can be added programmatically using DebugConfigManager.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
