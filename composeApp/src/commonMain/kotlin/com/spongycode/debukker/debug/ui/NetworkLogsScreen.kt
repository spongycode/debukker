package com.spongycode.debukker.debug.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spongycode.debukker.debug.models.NetworkTransaction
import com.spongycode.debukker.debug.network.NetworkLogger
import com.spongycode.debukker.debug.network.generateCurlCommand
import com.spongycode.debukker.debug.models.NetworkRequest
import com.spongycode.debukker.debug.models.NetworkResponse

@Composable
fun NetworkLogsScreen() {
    val transactions by NetworkLogger.transactions.collectAsState()
    var filterText by remember { mutableStateOf("") }
    var selectedTransaction by remember { mutableStateOf<NetworkTransaction?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Network Logs (${transactions.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { NetworkLogger.clearLogs() }) {
                    Icon(Icons.Default.Delete, "Clear logs")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = filterText,
            onValueChange = { filterText = it },
            placeholder = { Text("Filter by URL (supports regex)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No network requests captured yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = (if (filterText.isBlank()) {
                        transactions
                    } else {
                        NetworkLogger.filterByUrl(filterText)
                    }).reversed(),
                    key = { it.id }
                ) { transaction ->
                    NetworkTransactionItem(
                        transaction = transaction,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (transaction.error != null) {
                MaterialTheme.colorScheme.errorContainer
            } else if (transaction.response?.statusCode in 200..299) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    transaction.request.method,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                transaction.response?.let {
                    Text(
                        "${it.statusCode} ${it.statusMessage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (it.statusCode in 200..299) {
                            Color(0xFF388E3C)
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                transaction.request.url,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                transaction.duration?.let {
                    Text(
                        "⏱ ${it}ms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                transaction.response?.let {
                    Text(
                        "${it.bodySize} bytes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (transaction.isMocked) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "🎭 MOCKED",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
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
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Transaction Details") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Request") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Response") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> RequestDetailsContent(transaction.request)
                    1 -> transaction.response?.let { ResponseDetailsContent(it) }
                        ?: Text("No response available")
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val curlCommand = generateCurlCommand(transaction.request)
                        clipboardManager.setText(AnnotatedString(text = curlCommand))
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy cURL")
                }

                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun RequestDetailsContent(request: NetworkRequest) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            DetailSection("URL") {
                Text(request.url, fontFamily = FontFamily.Monospace)
            }
        }

        item {
            DetailSection("Method") {
                Text(request.method, fontWeight = FontWeight.Bold)
            }
        }

        if (request.headers.isNotEmpty()) {
            item {
                DetailSection("Headers") {
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
                DetailSection("Query Parameters") {
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
                DetailSection("Body") {
                    Text(
                        body,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }

        item {
            DetailSection("Req time") {
                Text(
                    request.requestTime.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
        }
    }
}

@Composable
fun ResponseDetailsContent(response: NetworkResponse) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        item {
            DetailSection("Status") {
                Text(
                    "${response.statusCode} ${response.statusMessage}",
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
                DetailSection("Headers") {
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
                DetailSection("Body") {
                    Text(
                        body,
                        fontFamily = FontFamily.Monospace,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize
                    )
                }
            }
        }

        item {
            DetailSection("Res time") {
                Text(
                    response.responseTime.toString(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize
                )
            }
        }
    }
}

@Composable
fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}
