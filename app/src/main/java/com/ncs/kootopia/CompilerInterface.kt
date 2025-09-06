package com.ncs.kootopia

import androidx.compose.ui.platform.ClipboardManager

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

@Composable
fun CompilerInterface(
    clipboardManager: ClipboardManager,
    compileOutput: String,
    onClose: () -> Unit
) {
    // Determine text color based on result content
    val textColor = if (compileOutput.contains("failed", ignoreCase = true) || 
                       compileOutput.contains("error", ignoreCase = true)) {
        Color.Red
    } else {
        Color.Green
    }
    
    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Compiler Result") },
        text = { Text(compileOutput, color = textColor) },
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