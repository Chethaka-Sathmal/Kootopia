package com.ncs.kootopia

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.ncs.kootopia.ui.theme.KootopiaColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



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
    var showError = remember { mutableStateOf(false) }
    var errorMessage = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Request focus on the find text field when the dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Find and Replace", color = KootopiaColors.textPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = findText.value,
                    onValueChange = { findText.value = it },
                    label = { Text("Find", color = KootopiaColors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KootopiaColors.textPrimary,
                        unfocusedTextColor = KootopiaColors.textPrimary,
                        focusedLabelColor = KootopiaColors.accentBlue,
                        unfocusedLabelColor = KootopiaColors.textSecondary,
                        focusedBorderColor = KootopiaColors.accentBlue,
                        unfocusedBorderColor = KootopiaColors.textSecondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = replaceText.value,
                    onValueChange = { replaceText.value = it },
                    label = { Text("Replace", color = KootopiaColors.textSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = KootopiaColors.textPrimary,
                        unfocusedTextColor = KootopiaColors.textPrimary,
                        focusedLabelColor = KootopiaColors.accentBlue,
                        unfocusedLabelColor = KootopiaColors.textSecondary,
                        focusedBorderColor = KootopiaColors.accentBlue,
                        unfocusedBorderColor = KootopiaColors.textSecondary
                    )
                )
                
                if (showError.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage.value,
                        color = KootopiaColors.errorRed,
                        style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )
                }
            }
        },
        confirmButton = {
            Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        showError.value = false
                        val success = editorState.replaceAll(findText.value, replaceText.value)
                        if (!success) {
                            errorMessage.value = "Search term '${findText.value}' not found"
                            showError.value = true
                        } else {
                            onClose()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Replace All", color = KootopiaColors.textPrimary)
                }
                
                Button(
                    onClick = {
                        showError.value = false
                        val success = editorState.replace(findText.value, replaceText.value)
                        if (!success) {
                            errorMessage.value = "Search term '${findText.value}' not found"
                            showError.value = true
                        } else {
                            onClose()
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Replace", color = KootopiaColors.textPrimary)
                }
                
                TextButton(
                    onClick = { onClose() },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = KootopiaColors.textSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        },
        containerColor = KootopiaColors.surfaceDark
    )
}
