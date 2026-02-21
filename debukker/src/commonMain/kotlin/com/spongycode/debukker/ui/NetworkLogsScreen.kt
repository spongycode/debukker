package com.spongycode.debukker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularNoSim
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.models.NetworkRequest
import com.spongycode.debukker.models.NetworkResponse
import com.spongycode.debukker.models.NetworkTransaction
import com.spongycode.debukker.models.ResponseMock
import com.spongycode.debukker.network.NetworkLogger
import com.spongycode.debukker.network.generateCurlCommand
import kotlin.time.Clock

@Composable
fun NetworkLogsScreen() {
    val transactions by NetworkLogger.transactions.collectAsState()
    var filterText by remember { mutableStateOf("") }
    var selectedTransaction by remember { mutableStateOf<NetworkTransaction?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            placeholder = { Text("Search logs...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (filterText.isNotEmpty()) {
                    IconButton(onClick = { filterText = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Total: ${transactions.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TextButton(
                onClick = { NetworkLogger.clearLogs() },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
            ) {
                Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Clear All", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(top = 100.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SignalCellularNoSim,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No requests captured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val filtered = if (filterText.isBlank()) {
                    transactions
                } else {
                    NetworkLogger.filterByUrl(filterText)
                }

                items(
                    items = filtered.reversed(),
                    key = { it.id }
                ) { transaction ->
                    NetworkTransactionItem(
                        transaction = transaction,
                        searchQuery = filterText,
                        onClick = { selectedTransaction = transaction }
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction = null }
        )
    }
}

@Composable
fun NetworkTransactionItem(
    transaction: NetworkTransaction,
    searchQuery: String = "",
    onClick: () -> Unit
) {
    val isPending = transaction.response == null && transaction.error == null
    val statusCode = transaction.response?.statusCode ?: 0
    val isSuccess = statusCode in 200..299
    val isError = transaction.error != null || (statusCode >= 400 && statusCode != 0)

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        transaction.request.method,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val statusColor = when {
                    isPending -> MaterialTheme.colorScheme.tertiary
                    isSuccess -> MaterialTheme.colorScheme.secondary
                    isError -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        when {
                            isPending -> "WAITING"
                            isError -> if (transaction.error != null) "ERROR" else "$statusCode"
                            else -> "$statusCode"
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (transaction.isMocked) {
                        Text(
                            "MOCKED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    Text(
                        (transaction.duration?.let { "${it}ms" } ?: "") +
                        (transaction.response?.bodySize?.let { " • ${formatBytes(it)}" } ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = getHighlightedText(transaction.request.url, searchQuery),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                fontWeight = FontWeight.Medium
            )

            if (isError && transaction.error != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val errorFirstLine = transaction.error.lines().firstOrNull() ?: transaction.error
                val hasMoreLines = transaction.error.lines().size > 1
                Text(
                    if (hasMoreLines) "$errorFirstLine ..." else errorFirstLine,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailDialog(
    transaction: NetworkTransaction,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showResponseMockDialog by remember { mutableStateOf(false) }
    var showRequestMockDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Transaction Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        transaction.request.method + " • " + (transaction.response?.statusCode ?: "PENDING"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    onClick = {
                        val curlCommand = generateCurlCommand(transaction.request)
                        clipboardManager.setText(AnnotatedString(text = curlCommand))
                    },
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CURL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val tabs = buildList {
                    add("Request")
                    add("Response")
                    if (transaction.error != null) add("Error")
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]).clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                            height = 3.dp,
                            color = if (tabs.getOrNull(selectedTab) == "Error") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            selectedContentColor = if (title == "Error") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            text = { Text(title, fontSize = 14.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    when (tabs.getOrNull(selectedTab)) {
                        "Request" -> RequestDetailsContent(transaction.request)
                        "Response" -> transaction.response?.let { ResponseDetailsContent(it) }
                            ?: Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No response available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        "Error" -> transaction.error?.let { ErrorDetailsContent(it) }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = {
                        if (selectedTab == 0) showRequestMockDialog = true
                        else showResponseMockDialog = true
                    },
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Dataset, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (selectedTab == 0) "MOCK REQ" else "MOCK RES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("CLOSE")
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )

    if (showResponseMockDialog) {
        CreateMockDialog(
            transaction = transaction,
            onDismiss = { showResponseMockDialog = false },
            onConfirm = { mock ->
                DebugConfigManager.addResponseMock(mock)
                showResponseMockDialog = false
                onDismiss()
            }
        )
    }

    if (showRequestMockDialog) {
        CreateRequestModifierDialog(
            transaction = transaction,
            onDismiss = { showRequestMockDialog = false },
            onConfirm = { mock ->
                DebugConfigManager.addRequestMock(mock)
                showRequestMockDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun CreateRequestModifierDialog(
    transaction: NetworkTransaction,
    onDismiss: () -> Unit,
    onConfirm: (com.spongycode.debukker.models.RequestMock) -> Unit
) {
    var urlPattern by remember { mutableStateOf(transaction.request.url.substringBefore('?')) }
    var mockHeaders by remember {
        mutableStateOf(transaction.request.headers.entries.joinToString("\n") { "${it.key}:${it.value}" })
    }
    var mockQueryParams by remember {
        mutableStateOf(transaction.request.queryParams.entries.joinToString("\n") { "${it.key}:${it.value}" })
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Request Modifier", style = MaterialTheme.typography.titleMedium) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = urlPattern,
                        onValueChange = { urlPattern = it },
                        label = { Text("URL Pattern (Regex)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = mockHeaders,
                        onValueChange = { mockHeaders = it },
                        label = { Text("Headers (Key:Value)") },
                        placeholder = { Text("X-Mock-Request: true") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = mockQueryParams,
                        onValueChange = { mockQueryParams = it },
                        label = { Text("Query Params (Key:Value)") },
                        placeholder = { Text("test: mock_value") },
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
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

                    val queryParams = mockQueryParams.lines()
                        .filter { it.isNotBlank() }
                        .mapNotNull { line ->
                            val parts = line.split(":", limit = 2)
                            if (parts.size == 2) {
                                parts[0].trim() to parts[1].trim()
                            } else null
                        }.toMap()

                    val mock = com.spongycode.debukker.models.RequestMock(
                        id = "req-mock-${Clock.System.now().toEpochMilliseconds()}",
                        urlPattern = urlPattern,
                        method = transaction.request.method,
                        headerOverrides = headers,
                        isEnabled = true
                    )
                    onConfirm(mock)
                },
                shape = RoundedCornerShape(10.dp),
                enabled = urlPattern.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun CreateMockDialog(
    transaction: NetworkTransaction,
    onDismiss: () -> Unit,
    onConfirm: (ResponseMock) -> Unit
) {
    var statusCode by remember { mutableStateOf(transaction.response?.statusCode?.toString() ?: "200") }
    var responseBody by remember { mutableStateOf(transaction.response?.body ?: "") }
    var delayMs by remember { mutableStateOf("0") }
    var mockHeaders by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Mock", style = MaterialTheme.typography.titleMedium) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            transaction.request.url,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = statusCode,
                        onValueChange = { statusCode = it },
                        label = { Text("Status Code") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = responseBody,
                        onValueChange = { responseBody = it },
                        label = { Text("Response Body") },
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = mockHeaders,
                        onValueChange = { mockHeaders = it },
                        label = { Text("Headers (Key:Value)") },
                        placeholder = { Text("Content-Type: application/json") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = delayMs,
                        onValueChange = { delayMs = it },
                        label = { Text("Delay (ms)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
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

                    val mock = ResponseMock(
                        id = "mock-${Clock.System.now().toEpochMilliseconds()}",
                        urlPattern = transaction.request.url.substringBefore('?'),
                        method = transaction.request.method,
                        statusCode = statusCode.toIntOrNull() ?: 200,
                        bodyOverride = responseBody.ifBlank { null },
                        headerOverrides = headers,
                        delayMs = delayMs.toLongOrNull() ?: 0,
                        isEnabled = true
                    )
                    onConfirm(mock)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}


@Composable
fun RequestDetailsContent(request: NetworkRequest) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            DetailSection("URL", copyValue = request.url) {
                Text(request.url, fontFamily = FontFamily.Monospace)
            }
        }

        item {
            DetailSection("Method", copyValue = request.method) {
                Text(request.method, fontWeight = FontWeight.Bold)
            }
        }

        if (request.headers.isNotEmpty()) {
            item {
                val headersText = request.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                DetailSection("Headers", copyValue = headersText) {
                    request.headers.forEach { (key, value) ->
                        Text(
                            "$key: $value",
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                }
            }
        }

        if (request.queryParams.isNotEmpty()) {
            item {
                val queryParamsText = request.queryParams.entries.joinToString("\n") { "${it.key} = ${it.value}" }
                DetailSection("Query Parameters", copyValue = queryParamsText) {
                    request.queryParams.forEach { (key, value) ->
                        Text(
                            "$key = $value",
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                }
            }
        }

        request.body?.let { body ->
            item {
                DetailSection("Size") {
                    Text(formatBytes(request.bodySize), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                DetailSection("Body", copyValue = body) {
                    Text(
                        body,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }
    }
}

@Composable
fun ResponseDetailsContent(response: NetworkResponse) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            val statusText = "${response.statusCode} ${response.statusMessage}"
            DetailSection("Status", copyValue = statusText) {
                Text(
                    statusText,
                    fontWeight = FontWeight.Bold,
                    color = if (response.statusCode in 200..299) {
                        Color(0xFF388E3C)
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }

        if (response.headers.isNotEmpty()) {
            item {
                val headersText = response.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                DetailSection("Headers", copyValue = headersText) {
                    response.headers.forEach { (key, value) ->
                        Text(
                            "$key: $value",
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                }
            }
        }

        response.body?.let { body ->
            item {
                DetailSection("Size") {
                    Text(formatBytes(response.bodySize), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                DetailSection("Body", copyValue = body) {
                    SelectionContainer {
                        Text(
                            body,
                            fontFamily = FontFamily.Monospace,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorDetailsContent(error: String) {
    val lines = error.lines()
    val exceptionLine = lines.firstOrNull() ?: error
    var isExpanded by remember { mutableStateOf(false) }
    val hasMoreContent = lines.size > 1

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            DetailSection("Exception", copyValue = error) {
                SelectionContainer {
                    Column {
                        Text(
                            exceptionLine,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )

                        if (hasMoreContent) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isExpanded) {
                                lines.drop(1).forEach { line ->
                                    val trimmedLine = line.trim()
                                    val isAtLine = trimmedLine.startsWith("at ")
                                    val isCausedBy = trimmedLine.startsWith("Caused by:")

                                    Text(
                                        line,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                        color = when {
                                            isCausedBy -> MaterialTheme.colorScheme.error
                                            isAtLine -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        fontWeight = if (isCausedBy) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                if (hasMoreContent) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = { isExpanded = !isExpanded },
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            if (isExpanded) "▲ Collapse Stack Trace" else "▼ Expand Stack Trace (${lines.size - 1} lines)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    copyValue: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )

                if (copyValue != null) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(copyValue))
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy $title",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

