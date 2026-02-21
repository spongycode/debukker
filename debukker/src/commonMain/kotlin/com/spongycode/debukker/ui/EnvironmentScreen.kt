package com.spongycode.debukker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spongycode.debukker.DebugConfigManager
import com.spongycode.debukker.models.Environment

@Composable
fun EnvironmentScreen() {
    val config by DebugConfigManager.config.collectAsState()
    val envConfig = config.environmentConfig

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Dns,
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp).size(24.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        "Current Base URL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        envConfig.getBaseUrl(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ConfigSection("Select Environment") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Environment.entries.forEach { env ->
                    if (env != Environment.CUSTOM) {
                        val isSelected = envConfig.currentEnvironment == env.name
                        val url = when(env) {
                            Environment.PRODUCTION -> envConfig.productionUrl
                            Environment.PRE_PRODUCTION -> envConfig.preProductionUrl
                            Environment.LOCAL -> envConfig.localUrl
                            else -> envConfig.productionUrl
                        }
                        EnvironmentItem(
                            name = env.displayName,
                            url = url,
                            isSelected = isSelected,
                            onClick = { DebugConfigManager.updateEnvironment(env) }
                        )
                    }
                }

                val isCustomSelected = envConfig.currentEnvironment == Environment.CUSTOM.name
                EnvironmentItem(
                    name = Environment.CUSTOM.displayName,
                    url = envConfig.customBaseUrl.ifBlank { "Tap to configure" },
                    isSelected = isCustomSelected,
                    onClick = { DebugConfigManager.updateEnvironment(Environment.CUSTOM) }
                )
            }
        }

        if (envConfig.currentEnvironment == Environment.CUSTOM.name) {
            Spacer(modifier = Modifier.height(24.dp))

            ConfigSection("Configure Custom URL") {
                var customUrl by remember { mutableStateOf(envConfig.customBaseUrl) }

                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    placeholder = { Text("https://api.custom.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(
                            onClick = { DebugConfigManager.setCustomBaseUrl(customUrl) },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Text("SAVE")
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EnvironmentItem(
    name: String,
    url: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    url,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
