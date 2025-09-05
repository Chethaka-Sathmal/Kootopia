package com.ncs.kootopia

import android.R.attr.text
import android.content.Context
import android.util.Log
import com.ncs.kootopia.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Composable drawer UI with options to create, open, and save files
@Composable
fun DrawerContent(
    initialFileName: String,
    context: Context,
    hasUnsavedChanges: Boolean,
    autoSaveEnabled: Boolean,
    onNewFile: (String) -> Unit,
    onNewUntitledFile: () -> Unit,
    onOpenFile: (String) -> Unit,
    onSaveFile: (String) -> Unit,
    onToggleAutoSave: () -> Unit,
    onConfigure: () -> Unit = {}
) {
    var fileName = remember { mutableStateOf(initialFileName) }
    var showSaveDialog = remember { mutableStateOf(false) }
    var showOpenDialog = remember { mutableStateOf(false) }
    var fileNameError = remember { mutableStateOf("") }
    var expanded = remember { mutableStateOf(false) }
    var showSaveConfirmation = remember { mutableStateOf(false) }
    var savedFileName = remember { mutableStateOf("") }
    var showUnsavedChangesDialog = remember { mutableStateOf(false) }
    val extensions = listOf(".kt", ".txt", ".java", ".py", ".js", ".html", ".css", ".xml")
    var selectedExtension = remember { mutableStateOf(extensions.first()) }


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
            .background(com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // App Name Header
        Text(
            text = "Kootopia",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // New File Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    Log.d("DrawerContent", "New File clicked! hasUnsavedChanges: $hasUnsavedChanges")
                    if (hasUnsavedChanges) {
                        Log.d("DrawerContent", "Showing unsaved changes dialog")
                        showUnsavedChangesDialog.value = true
                    } else {
                        Log.d("DrawerContent", "Creating new untitled file directly")
                        onNewUntitledFile()
                    }
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.file_plus),
                contentDescription = "New File",
                tint = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "New File",
                fontSize = 18.sp,
                color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
            )
        }
        
        // Open File Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showOpenDialog.value = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.open_arrow),
                contentDescription = "Open File",
                tint = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Open File",
                fontSize = 18.sp,
                color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
            )
        }
        
        // Configure Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onConfigure() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.configure_icon),
                contentDescription = "Configure",
                tint = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Configure",
                fontSize = 18.sp,
                color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Auto-save Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Auto-save",
                    fontSize = 18.sp,
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (autoSaveEnabled) "ON" else "OFF",
                    fontSize = 14.sp,
                    color = if (autoSaveEnabled) 
                        com.ncs.kootopia.ui.theme.KootopiaColors.successGreen 
                    else 
                        com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                )
            }
            Switch(
                checked = autoSaveEnabled,
                onCheckedChange = { 
                    Log.d("DrawerContent", "Auto-save toggled: ${!autoSaveEnabled}")
                    onToggleAutoSave() 
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue,
                    checkedTrackColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue.copy(alpha = 0.5f),
                    uncheckedThumbColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                    uncheckedTrackColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
                )
            )
        }
    }


    // Show "Save File" dialog
    if (showSaveDialog.value) {
        AlertDialog(
            onDismissRequest = { 
                showSaveDialog.value = false
                fileNameError.value = ""
            },
            title = { Text("Save File", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileName.value,
                        onValueChange = { 
                            fileName.value = it
                            fileNameError.value = ""
                        },
                        label = { Text("File name", color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary) },
                        placeholder = { Text("e.g., myfile.txt", color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary) },
                        isError = fileNameError.value.isNotEmpty(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                            unfocusedTextColor = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                            focusedLabelColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue,
                            unfocusedLabelColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                            focusedBorderColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue,
                            unfocusedBorderColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                        )
                    )
                    
                    // Extension selection
                    Button(
                        onClick = { expanded.value = !expanded.value },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
                        )
                    ) {
                        Text("Select extension: ${selectedExtension.value}", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Arrow Down",
                            tint = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                        )
                    }
                    
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        extensions.forEach { ext ->
                            DropdownMenuItem(
                                text = { Text(ext, color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary) },
                                onClick = {
                                    selectedExtension.value = ext
                                    expanded.value = false
                                }
                            )
                        }
                    }
                    
                    if (fileNameError.value.isNotEmpty()) {
                        Text(
                            text = fileNameError.value,
                            color = com.ncs.kootopia.ui.theme.KootopiaColors.errorRed,
                            style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = fileName.value.trim()
                        if (trimmedName.isEmpty()) {
                            fileNameError.value = "Please enter a file name"
                        } else if (!trimmedName.contains(".")) {
                            fileNameError.value = "Please include a file extension (e.g., .txt, .py, .kt)"
                        } else {
                            val finalName = if (trimmedName.endsWith(selectedExtension.value)) {
                                trimmedName
                            } else {
                                trimmedName + selectedExtension.value
                            }
                            onSaveFile(finalName)
                            savedFileName.value = finalName
                            fileName.value = ""
                            fileNameError.value = ""
                            showSaveDialog.value = false
                            showSaveConfirmation.value = true
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue
                    )
                ) {
                    Text("Save", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSaveDialog.value = false
                        fileNameError.value = ""
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    }
    // Show "Open File" dialog
    if (showOpenDialog.value) {
        val files = context.filesDir.listFiles()?.toList()?.filter { it.isFile } ?: emptyList()
        AlertDialog(
            onDismissRequest = { showOpenDialog.value = false },
            title = { 
                Text(
                    "Open File", 
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                ) 
            },
            text = {
                if (files.isEmpty()) {
                    Text(
                        "No files found in the project directory.",
                        color = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(files.size) { index ->
                            val file = files[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onOpenFile(file.name)
                                        showOpenDialog.value = false
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.open_arrow),
                                    contentDescription = "Open File",
                                    tint = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = file.name,
                                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showOpenDialog.value = false },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = com.ncs.kootopia.ui.theme.KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    }
    
    // Save Confirmation Dialog
    if (showSaveConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation.value = false },
            title = { 
                Text(
                    "File Saved", 
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                ) 
            },
            text = {
                Text(
                    "File '${savedFileName.value}' has been saved successfully!",
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSaveConfirmation.value = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue
                    )
                ) {
                    Text("OK", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary)
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    }

    // Unsaved Changes Dialog - only show if there are actually unsaved changes
    if (showUnsavedChangesDialog.value && hasUnsavedChanges) {
        AlertDialog(
            onDismissRequest = { showUnsavedChangesDialog.value = false },
            title = { 
                Text(
                    "File Not Saved", 
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                ) 
            },
            text = {
                Text(
                    "The current file is not saved. Creating a new file will discard these unsaved changes.",
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onNewUntitledFile()
                        showUnsavedChangesDialog.value = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.accentBlue
                    )
                ) {
                    Text("Create Anyway", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnsavedChangesDialog.value = false },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
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