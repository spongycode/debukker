package com.spongycode.debukker.debug.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.spongycode.debukker.debug.preferences.DebugPreferencesManager

@Composable
fun PreferencesScreen() {
    val preferences by DebugPreferencesManager.preferences.collectAsState()
    var showEditDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        DebugPreferencesManager.refresh()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Preferences",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${preferences.size} keys",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { showAddDialog = true }) {
                    Text("Add")
                }
                
                Button(
                    onClick = { DebugPreferencesManager.clearAll() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Clear All")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (preferences.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No preferences available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = preferences.entries.toList(),
                    key = { it.key }
                ) { (key, value) ->
                    PreferenceItem(
                        key = key,
                        value = value,
                        onEdit = { showEditDialog = key to value },
                        onDelete = { DebugPreferencesManager.deletePreference(key) }
                    )
                }
            }
        }
    }
    
    showEditDialog?.let { (key, value) ->
        PreferenceEditDialog(
            key = key,
            value = value,
            onDismiss = { showEditDialog = null },
            onSave = { newValue ->
                DebugPreferencesManager.updatePreference(key, newValue)
                showEditDialog = null
            }
        )
    }
    
    if (showAddDialog) {
        PreferenceAddDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { key, value ->
                DebugPreferencesManager.updatePreference(key, value)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PreferenceItem(
    key: String,
    value: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PreferenceEditDialog(
    key: String,
    value: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newValue by remember { mutableStateOf(value) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Preference") },
        text = {
            Column {
                Text(
                    "Key: $key",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(newValue) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PreferenceAddDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Preference") },
        text = {
            Column {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(key, value) },
                enabled = key.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
