package com.spongycode.debukker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.models.ResponseMock

@Composable
fun MockConfigScreen() {
    val config by DebugConfigManager.config.collectAsState()
    var throttleInput by remember { mutableStateOf(config.throttleMs.toString()) }
    var editingMock by remember { mutableStateOf<ResponseMock?>(null) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Response Mocks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Mock API responses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = config.isResponseMockingEnabled,
                        onCheckedChange = { DebugConfigManager.setResponseMockingEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider()

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${config.responseMocks.size} mock(s) configured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (config.responseMocks.isNotEmpty()) {
                        Button(
                            onClick = { DebugConfigManager.clearAllMocks() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear All")
                        }
                    }
                }

                if (config.responseMocks.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No mocks configured. Create mocks from the Network tab by tapping any API call.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.height(12.dp))

                    config.responseMocks.forEach { mock ->
                        MockItem(
                            mock = mock,
                            onToggle = { enabled ->
                                DebugConfigManager.updateResponseMock(
                                    mock.copy(isEnabled = enabled)
                                )
                            },
                            onDelete = {
                                DebugConfigManager.removeResponseMock(mock.id)
                            },
                            onEdit = {
                                editingMock = mock
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    editingMock?.let { mock ->
        EditMockDialog(
            mock = mock,
            onDismiss = { editingMock = null },
            onConfirm = { updatedMock ->
                DebugConfigManager.updateResponseMock(updatedMock)
                editingMock = null
            }
        )
    }
}

@Composable
fun MockItem(
    mock: ResponseMock,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (mock.isEnabled) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            mock.method ?: "ANY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        mock.statusCode?.let { code ->
                            Text(
                                "→ $code",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        mock.urlPattern,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 2
                    )

                    if (mock.delayMs > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "⏱ ${mock.delayMs}ms delay",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Switch(
                        checked = mock.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.size(40.dp)
                    )

                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditMockDialog(
    mock: ResponseMock,
    onDismiss: () -> Unit,
    onConfirm: (ResponseMock) -> Unit
) {
    var statusCode by remember { mutableStateOf(mock.statusCode?.toString() ?: "200") }
    var responseBody by remember { mutableStateOf(mock.bodyOverride ?: "") }
    var delayMs by remember { mutableStateOf(mock.delayMs.toString()) }
    var mockHeaders by remember { 
        mutableStateOf(mock.headerOverrides.entries.joinToString("\n") { "${it.key}:${it.value}" }) 
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Response Mock") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "URL: ${mock.urlPattern}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = statusCode,
                    onValueChange = { statusCode = it },
                    label = { Text("Status Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = responseBody,
                    onValueChange = { responseBody = it },
                    label = { Text("Response Body (JSON)") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10
                )

                OutlinedTextField(
                    value = mockHeaders,
                    onValueChange = { mockHeaders = it },
                    label = { Text("Headers (key:value, one per line)") },
                    placeholder = { Text("X-Mock: true\nContent-Type: application/json") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = delayMs,
                    onValueChange = { delayMs = it },
                    label = { Text("Delay (ms)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val headers = mockHeaders.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        }.toMap()

                    val updatedMock = mock.copy(
                        statusCode = statusCode.toIntOrNull() ?: 200,
                        bodyOverride = responseBody.ifBlank { null },
                        headerOverrides = headers,
                        delayMs = delayMs.toLongOrNull() ?: 0
                    )
                    onConfirm(updatedMock)
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
