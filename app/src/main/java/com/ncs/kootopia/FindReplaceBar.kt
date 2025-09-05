package com.ncs.kootopia

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp



/**
 * FindReplaceBar Composable
 *
 * A dialog UI that allows the user to:
 * - Enter a search term (Find).
 * - Enter replacement text.
 * - Replace the first occurrence or all occurrences in the editor.
 *
 * Uses [TextEditorState] for performing replace operations.
 * [onClose] is called when the dialog is dismissed or after an action.
 */

@Composable
fun FindReplaceBar(
    editorState: TextEditorState,
    onClose: () -> Unit
) {
    var findText = remember { mutableStateOf("") }
    var replaceText = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Request focus on the find text field when the dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Find and Replace") },
        text = {
            Column {
                OutlinedTextField(
                    value = findText.value,
                    onValueChange = { findText.value = it },
                    label = { Text("Find") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = replaceText.value,
                    onValueChange = { replaceText.value = it },
                    label = { Text("Replace") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                editorState.replace(findText.value, replaceText.value)
                onClose()
            }) {
                Text("Replace")
            }
            Button(onClick = {
                editorState.replaceAll(findText.value, replaceText.value)
                onClose()
            }) {
                Text("Replace All")
            }
        }

    )
}
