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
    val extensions = listOf(".kt", ".txt", ".java")
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
            onDismissRequest = { showSaveDialog.value = false },
            confirmButton = {
                TextButton(onClick = {

                    val finalName = if (fileName.value.endsWith(selectedExtension.value)) {
                        fileName.value
                    } else {
                        fileName.value + selectedExtension.value
                    }

                    onSaveFile(finalName)
                    fileName.value = ""
                    showSaveDialog.value = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog.value = false }) { Text("Cancel") }
            },
            title = { Text("Save the file")

            }

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
}