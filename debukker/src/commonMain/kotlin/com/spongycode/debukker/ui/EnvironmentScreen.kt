package com.spongycode.debukker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val currentEnv = envConfig.getCurrentEnvironment()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Environment Configuration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Environment.entries.forEach { env ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentEnv == env,
                    onClick = { DebugConfigManager.updateEnvironment(env) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        env.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (currentEnv == env) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        when (env) {
                            Environment.PRODUCTION -> envConfig.productionUrl
                            Environment.PRE_PRODUCTION -> envConfig.preProductionUrl
                            Environment.LOCAL -> envConfig.localUrl
                            Environment.CUSTOM -> envConfig.customBaseUrl.ifBlank { "Not set" }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (currentEnv == Environment.CUSTOM) {
            Spacer(modifier = Modifier.height(16.dp))
            
            var customUrl by remember { mutableStateOf(envConfig.customBaseUrl) }
            
            OutlinedTextField(
                value = customUrl,
                onValueChange = { customUrl = it },
                label = { Text("Custom Base URL") },
                placeholder = { Text("https://api.custom.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = { DebugConfigManager.setCustomBaseUrl(customUrl) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Save Custom URL")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Current Base URL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    envConfig.getBaseUrl(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
