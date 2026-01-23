package com.spongycode.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spongycode.debukker.debug.network.createDebugHttpClient
import com.spongycode.debukker.debug.ui.DebugMenu
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    var showDebugMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val httpClient = remember { createDebugHttpClient() }
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Debukker Sample",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Test Network Logging",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        httpClient.get("https://jsonplaceholder.typicode.com/posts/1")
                                    } catch (e: Exception) {
                                        println("API call failed: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test API Call 1 (GET)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        httpClient.get("https://jsonplaceholder.typicode.com/posts")
                                    } catch (e: Exception) {
                                        println("API call failed: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test API Call 2 (GET)")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        httpClient.post("https://jsonplaceholder.typicode.com/posts") {
                                            setBody("""{"title": "Test", "body": "Test body", "userId": 1}""")
                                        }
                                    } catch (e: Exception) {
                                        println("API call failed: ${e.message}")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Test API Call 3 (POST)")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            "Make some API calls, then tap the bug icon to view network logs!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = { showDebugMenu = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.BugReport, "Debug Menu")
            }

            DebugMenu(
                isVisible = showDebugMenu,
                onDismiss = { showDebugMenu = false }
            )
        }
    }
}