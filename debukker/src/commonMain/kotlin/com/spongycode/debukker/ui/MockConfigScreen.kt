package com.spongycode.debukker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.models.RequestMock
import com.spongycode.debukker.models.ResponseMock

@Composable
fun MockConfigScreen() {
    val config by DebugConfigManager.config.collectAsState()
    var throttleInput by remember { mutableStateOf(config.throttleMs.toString()) }
    var editingMock by remember { mutableStateOf<ResponseMock?>(null) }
    var editingRequestMock by remember { mutableStateOf<RequestMock?>(null) }
    var showGlobalHeadersDialog by remember { mutableStateOf(false) }
    var showTimeoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ConfigSection("General Controls") {
            ControlCard(
                title = "Offline Mode",
                subtitle = "Block all outgoing network requests",
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                checked = config.isOfflineMode,
                onCheckedChange = { DebugConfigManager.setOfflineMode(it) },
                activeColor = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(12.dp))

            ControlCard(
                title = "Network Mocking",
                subtitle = "Enable or disable all response mocks",
                icon = Icons.Default.Dataset,
                checked = config.isResponseMockingEnabled,
                onCheckedChange = { DebugConfigManager.setResponseMockingEnabled(it) },
                activeColor = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSection("Network Throttle") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Add artificial delay (latency) to all requests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = throttleInput,
                            onValueChange = { throttleInput = it },
                            placeholder = { Text("0") },
                            suffix = { Text("ms") },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )

                        Button(
                            onClick = {
                                throttleInput.toLongOrNull()?.let { delay ->
                                    DebugConfigManager.setThrottle(delay)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(52.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                        ) {
                            Text("Set Delay")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(0L, 500L, 1000L, 3000L).forEach { delay ->
                            val isSelected = config.throttleMs == delay
                            Surface(
                                onClick = {
                                    throttleInput = delay.toString()
                                    DebugConfigManager.setThrottle(delay)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.5f,
                                ),
                                border = if (isSelected) BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.primary,
                                ) else null,
                            ) {
                                Text(
                                    "${delay}ms",
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSection("Network Timeouts") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                onClick = { showTimeoutDialog = true },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Timeout Configuration",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        val summary = buildString {
                            append("Req: ${if (config.requestTimeoutMs > 0) "${config.requestTimeoutMs}ms" else "30k"}")
                            append(" • ")
                            append("Conn: ${if (config.connectTimeoutMs > 0) "${config.connectTimeoutMs}ms" else "30k"}")
                            append(" • ")
                            append("Sock: ${if (config.socketTimeoutMs > 0) "${config.socketTimeoutMs}ms" else "30k"}")
                        }
                        Text(
                            summary,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSection(
            title = "Global Headers",
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                onClick = { showGlobalHeadersDialog = true },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.padding(10.dp).size(20.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Global Headers",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            if (config.globalHeaders.isEmpty()) "No global headers set" else "${config.globalHeaders.size} active global headers",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            }

            }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSection(
            title = "Active Mocks",
            action = {
                if (config.responseMocks.isNotEmpty() || config.requestMocks.isNotEmpty()) {
                    TextButton(onClick = { DebugConfigManager.clearAllMocks() }) {
                        Text(
                            "Clear All",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            },
        ) {
            if (config.responseMocks.isEmpty() && config.requestMocks.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                ) {
                    Text(
                        "No active mocks. Create mocks by tapping network logs.",
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    config.requestMocks.forEach { reqMock ->
                        RequestMockItem(
                            mock = reqMock,
                            onToggle = { enabled -> DebugConfigManager.updateRequestMock(reqMock.copy(isEnabled = enabled)) },
                            onDelete = { DebugConfigManager.removeRequestMock(reqMock.id) },
                            onEdit = { editingRequestMock = reqMock },
                        )
                    }
                    config.responseMocks.forEach { mock ->
                        MockItem(
                            mock = mock,
                            onToggle = { enabled ->
                                DebugConfigManager.updateResponseMock(mock.copy(isEnabled = enabled))
                            },
                            onDelete = {
                                DebugConfigManager.removeResponseMock(mock.id)
                            },
                            onEdit = {
                                editingMock = mock
                            },
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    editingMock?.let { mock ->
        EditMockDialog(
            mock = mock,
            onDismiss = { editingMock = null },
            onConfirm = { updatedMock ->
                DebugConfigManager.updateResponseMock(updatedMock)
                editingMock = null
            },
        )
    }

    if (showGlobalHeadersDialog) {
        GlobalHeadersDialog(
            headers = config.globalHeaders,
            onDismiss = { showGlobalHeadersDialog = false },
            onConfirm = { updatedHeaders ->
                DebugConfigManager.updateGlobalHeaders(updatedHeaders)
                showGlobalHeadersDialog = false
            },
        )
    }

    editingRequestMock?.let { mock ->
        EditRequestMockDialog(
            mock = mock,
            onDismiss = { editingRequestMock = null },
            onConfirm = { updatedMock ->
                if (config.requestMocks.any { it.id == updatedMock.id }) {
                    DebugConfigManager.updateRequestMock(updatedMock)
                } else {
                    DebugConfigManager.addRequestMock(updatedMock)
                }
                editingRequestMock = null
            },
        )
    }

    if (showTimeoutDialog) {
        TimeoutDialog(
            currentRequest = config.requestTimeoutMs,
            currentConnect = config.connectTimeoutMs,
            currentSocket = config.socketTimeoutMs,
            onDismiss = { showTimeoutDialog = false },
            onConfirm = { timeout ->
                DebugConfigManager.setTimeoutOverrides(timeout, timeout, timeout)
                showTimeoutDialog = false
            },
            onReset = {
                DebugConfigManager.setTimeoutOverrides(0, 0, 0)
                showTimeoutDialog = false
            },
        )
    }
}

@Composable
fun ConfigSection(
    title: String,
    action: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
            )
            action?.invoke()
        }
        content()
    }
}

@Composable
fun ControlCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    activeColor: Color,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = if (checked) activeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp).size(20.dp),
                    tint = if (checked) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeColor,
                ),
            )
        }
    }
}

@Composable
fun MockItem(
    mock: ResponseMock,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        "${mock.method ?: "ANY"} • ${mock.statusCode ?: 200}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                if (mock.delayMs > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                    ) {
                        Text(
                            "${mock.delayMs}ms",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Switch(
                    checked = mock.isEnabled,
                    onCheckedChange = onToggle,
                    scale = 0.7f,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                mock.urlPattern,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (mock.headerOverrides.isNotEmpty()) {
                    Text(
                        "${mock.headerOverrides.size} headers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
fun Switch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, scale: Float) {
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = Modifier.scale(scale),
    )
}

@Composable
fun EditMockDialog(
    mock: ResponseMock,
    onDismiss: () -> Unit,
    onConfirm: (ResponseMock) -> Unit,
) {
    var statusCode by remember { mutableStateOf(mock.statusCode?.toString() ?: "200") }
    var responseBody by remember { mutableStateOf(mock.bodyOverride ?: "") }
    var delayMs by remember { mutableStateOf(mock.delayMs.toString()) }
    var mockHeaders by remember {
        mutableStateOf(mock.headerOverrides.entries.joinToString("\n") { "${it.key}:${it.value}" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Response", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        mock.urlPattern,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                OutlinedTextField(
                    value = statusCode,
                    onValueChange = { statusCode = it },
                    label = { Text("Status Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = responseBody,
                    onValueChange = { responseBody = it },
                    label = { Text("Response Body") },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(10.dp),
                )

                OutlinedTextField(
                    value = mockHeaders,
                    onValueChange = { mockHeaders = it },
                    label = { Text("Headers (Key:Value)") },
                    placeholder = { Text("Content-Type: application/json") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                )

                OutlinedTextField(
                    value = delayMs,
                    onValueChange = { delayMs = it },
                    label = { Text("Delay (ms)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val headers = mockHeaders.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        }.toMap()

                    onConfirm(
                        mock.copy(
                            statusCode = statusCode.toIntOrNull() ?: 200,
                            bodyOverride = responseBody.ifBlank { null },
                            headerOverrides = headers,
                            delayMs = delayMs.toLongOrNull() ?: 0,
                        ),
                    )
                },
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun GlobalHeadersDialog(
    headers: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit,
) {
    var headersText by remember {
        mutableStateOf(headers.entries.joinToString("\n") { "${it.key}:${it.value}" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Global Headers", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "These headers will be added to every outgoing network request.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                OutlinedTextField(
                    value = headersText,
                    onValueChange = { headersText = it },
                    label = { Text("Headers (Key:Value)") },
                    placeholder = { Text("Authorization: Bearer token\nX-App-Version: 1.0.0") },
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedHeaders = headersText.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        }.toMap()
                    onConfirm(updatedHeaders)
                },
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun EditRequestMockDialog(
    mock: RequestMock,
    onDismiss: () -> Unit,
    onConfirm: (RequestMock) -> Unit,
) {
    var urlPattern by remember { mutableStateOf(mock.urlPattern) }
    var mockHeaders by remember {
        mutableStateOf(mock.headerOverrides.entries.joinToString("\n") { "${it.key}:${it.value}" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Modifier", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = urlPattern,
                    onValueChange = { urlPattern = it },
                    label = { Text("URL Pattern (Regex supported)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = mockHeaders,
                    onValueChange = { mockHeaders = it },
                    label = { Text("Headers (Key:Value)") },
                    placeholder = { Text("X-Mock-Request: true") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    shape = RoundedCornerShape(10.dp),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val headers = mockHeaders.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        }.toMap()

                    onConfirm(
                        mock.copy(
                            urlPattern = urlPattern,
                            headerOverrides = headers,
                        ),
                    )
                },
                shape = RoundedCornerShape(10.dp),
                enabled = urlPattern.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
fun RequestMockItem(
    mock: RequestMock,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        "REQUEST MODIFIER",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Switch(
                    checked = mock.isEnabled,
                    onCheckedChange = onToggle,
                    scale = 0.7f,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                mock.urlPattern,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (mock.headerOverrides.isNotEmpty()) {
                    Text(
                        "${mock.headerOverrides.size} headers",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}

@Composable
fun TimeoutDialog(
    currentRequest: Long,
    currentConnect: Long,
    currentSocket: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    onReset: () -> Unit,
) {
    var timeoutInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Network Timeouts", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TimeoutDisplayRow("Request Timeout", currentRequest)
                TimeoutDisplayRow("Connection Timeout", currentConnect)
                TimeoutDisplayRow("Socket Timeout", currentSocket)

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )

                OutlinedTextField(
                    value = timeoutInput,
                    onValueChange = { timeoutInput = it },
                    label = { Text("Set All Timeouts (ms)") },
                    placeholder = { Text("e.g. 5000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val timeout = timeoutInput.toLongOrNull() ?: 0L
                    onConfirm(timeout)
                },
                shape = RoundedCornerShape(10.dp),
                enabled = timeoutInput.isNotBlank(),
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onReset) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
    )
}

@Composable
private fun TimeoutDisplayRow(label: String, value: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (value > 0) "${value}ms" else "30000ms (Default)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (value > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
