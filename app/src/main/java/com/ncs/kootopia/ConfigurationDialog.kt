package com.ncs.kootopia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfigurationDialog(
    fileManager: FileManager,
    onDismiss: () -> Unit,
    onEditConfig: (String) -> Unit,
    onCreateConfig: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newConfigName by remember { mutableStateOf("") }
    var configNameError by remember { mutableStateOf("") }
    val configFiles = remember { mutableStateOf(fileManager.listConfigurations()) }

    // Refresh config files when dialog opens
    LaunchedEffect(Unit) {
        configFiles.value = fileManager.listConfigurations()
    }

    if (!showCreateDialog) {
        // Main configuration dialog - choose edit or create
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Configuration Files",
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Choose an action:",
                        color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Create New Config Button
                    Button(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue
                        )
                    ) {
                        Text(
                            "Create New Configuration",
                            color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Edit Existing Config Section
                    if (configFiles.value.isNotEmpty()) {
                        Text(
                            "Edit Existing Configuration:",
                            color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(configFiles.value) { configFile ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = configFile,
                                        color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = { 
                                            onEditConfig(configFile)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
                                        ),
                                        modifier = Modifier.padding(start = 8.dp)
                                    ) {
                                        Text(
                                            "Edit",
                                            color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "No configuration files found.",
                            color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    } else {
        // Create new configuration dialog
        AlertDialog(
            onDismissRequest = { 
                showCreateDialog = false
                newConfigName = ""
                configNameError = ""
            },
            title = {
                Text(
                    "Create New Configuration",
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = newConfigName,
                        onValueChange = { 
                            newConfigName = it
                            configNameError = ""
                        },
                        label = { 
                            Text(
                                "Configuration file name",
                                color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                            )
                        },
                        placeholder = { 
                            Text(
                                "e.g., python.json, java.json",
                                color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                            )
                        },
                        isError = configNameError.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                            unfocusedTextColor = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                            focusedLabelColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue,
                            unfocusedLabelColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                            focusedBorderColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue,
                            unfocusedBorderColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                        )
                    )
                    
                    if (configNameError.isNotEmpty()) {
                        Text(
                            text = configNameError,
                            color = com.ncs.kootopia.ui.theme.KootopiaColors.errorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = newConfigName.trim()
                        when {
                            trimmedName.isEmpty() -> {
                                configNameError = "Please enter a configuration file name"
                            }
                            !trimmedName.endsWith(".json") -> {
                                configNameError = "Configuration file must end with .json"
                            }
                            configFiles.value.contains(trimmedName) -> {
                                configNameError = "A configuration file with this name already exists"
                            }
                            else -> {
                                onCreateConfig(trimmedName)
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue
                    )
                ) {
                    Text(
                        "Create",
                        color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCreateDialog = false
                        newConfigName = ""
                        configNameError = ""
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    }
}

