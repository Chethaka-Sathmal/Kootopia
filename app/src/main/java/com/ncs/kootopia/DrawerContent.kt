package com.ncs.kootopia

import android.R.attr.text
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Composable drawer UI with options to create, open, and save files
@Composable
fun DrawerContent(
    initialFileName: String,
    context: Context,
    onNewFile: (String) -> Unit,
    onOpenFile: (String) -> Unit,
    onSaveFile: (String) -> Unit
) {
    var fileName = remember { mutableStateOf(initialFileName) }
    var showDialog = remember { mutableStateOf(false) }
    var showSaveDialog = remember { mutableStateOf(false) }
    var showOpenDialog = remember { mutableStateOf(false) }
    var fileNameError = remember { mutableStateOf("") }
    var expanded = remember { mutableStateOf(false) }
    var showSaveConfirmation = remember { mutableStateOf(false) }
    var savedFileName = remember { mutableStateOf("") }
    val extensions = listOf(".kt", ".txt", ".java", ".py", ".js", ".html", ".css", ".xml")
    var selectedExtension = remember { mutableStateOf(extensions.first()) }


    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
            .background(Color.Gray)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        TextButton(onClick = { showDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
            Text("New File", fontSize = 18.sp)
        }

        TextButton(onClick = { showOpenDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Open", fontSize = 18.sp)
        }

        TextButton(onClick = { showSaveDialog.value = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Save", fontSize = 18.sp)
        }
    }

    // Show "New File" dialog
    if (showDialog.value) {

        var expanded = remember { mutableStateOf(false) }


        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val finalName = if (fileName.value.endsWith(selectedExtension.value)) {
                        fileName.value
                    } else {
                        fileName.value + selectedExtension.value
                    }
                    onNewFile(finalName)
                    showDialog.value = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog.value = false }) { Text("Cancel") }
            },
            title = { Text("Create New File") },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileName.value ,
                        onValueChange = { fileName.value = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        trailingIcon = { Text(selectedExtension.value) }
                    )
                    Button(onClick = {expanded.value = !expanded.value}) {
                        Text("Select extention")
                        Icon( Icons.Default.ArrowDropDown,
                            contentDescription = "Arrow Down")
                    }
                    DropdownMenu(
                        expanded = expanded.value,
                        onDismissRequest = { expanded.value = false }
                    ) {
                        extensions.forEach { ext ->
                            DropdownMenuItem(
                                text = { Text(ext) },
                                onClick = {
                                    selectedExtension.value = ext
                                    expanded.value = false
                                }
                            )
                        }
                    }
                }


            }
        )
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
        val files = context.filesDir.listFiles()?.toList() ?: emptyList()
        AlertDialog(
            onDismissRequest = { showOpenDialog.value = false },
            title = { Text("Select a file") },
            text = {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(files.size) { index ->
                        val file = files[index]
                        Text(
                            text = file.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    onOpenFile(file.name)
                                    showOpenDialog.value = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOpenDialog.value = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Save Confirmation Dialog
    if (showSaveConfirmation.value) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmation.value = false },
            title = { 
                Text(
                    "File Saved", 
                    color = com.ncs.kootopia.ui.theme.KootopiaColors.successGreen
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
                        containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.successGreen
                    )
                ) {
                    Text("OK", color = com.ncs.kootopia.ui.theme.KootopiaColors.textPrimary)
                }
            },
            containerColor = com.ncs.kootopia.ui.theme.KootopiaColors.surfaceDark
        )
    }
}