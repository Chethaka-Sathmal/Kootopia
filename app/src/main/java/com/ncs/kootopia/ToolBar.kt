package com.ncs.kootopia

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ncs.kootopia.ui.theme.KootopiaColors
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
/**
 * MiniToolbar Composable
 *
 * A simple toolbar UI that provides buttons for text editing actions:
 * - Cut
 * - Copy
 * - Paste
 *
 * The actual behavior is passed in through the callbacks [onCut], [onCopy], and [onPaste].
 */
@Composable
fun MiniToolbar(
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    Log.d("FileManager", "Mini tool bar")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KootopiaColors.surfaceDark)
            .padding(8.dp),
    ) {
        TextButton(
            onClick = {
                onCut()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ) { Text("Cut", color = KootopiaColors.textPrimary) }
        TextButton(
            onClick = {
                onCopy()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ) { Text("Copy", color = KootopiaColors.textPrimary) }
        TextButton(
            onClick = {
                onPaste()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ) { Text("Paste", color = KootopiaColors.textPrimary) }
    }
}