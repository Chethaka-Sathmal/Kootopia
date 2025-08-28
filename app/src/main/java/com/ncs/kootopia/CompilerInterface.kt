package com.ncs.kootopia

import androidx.compose.ui.platform.ClipboardManager

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CompilerInterface(
    clipboardManager: ClipboardManager,
    compileOutput: String,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Compiler Result") },
        text = { Text(compileOutput) },
        confirmButton = {
            Row {
                Button(onClick = {
                    val pathOnly = compileOutput.substringAfter("Kotlin file saved at ").trim()
                    if (pathOnly.isNotEmpty()) {
                        clipboardManager.setText(AnnotatedString(pathOnly))
                    }
                }) {
                    Text("Copy")
                }

                Button(onClick = { onClose() }) {
                    Text("OK")
                }
            }
        }
    )
}