package com.ncs.kootopia

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager

/**
 * KeyboardManager - A utility class for managing keyboard visibility
 * 
 * This class provides composable functions and utilities to:
 * - Show keyboard by default
 * - Hide keyboard when touching non-text areas
 * - Manage focus states
 */
object KeyboardManager {
    
    /**
     * Modifier that hides the keyboard when the area is clicked
     * Use this on non-text areas like backgrounds, buttons, etc.
     */
    @Composable
    fun hideKeyboardOnClick(): Modifier {
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        
        return Modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
    }
    
    /**
     * Modifier for text areas that should show keyboard when focused
     * Use this on text input fields
     */
    @Composable
    fun showKeyboardOnFocus(): Modifier {
        val keyboardController = LocalSoftwareKeyboardController.current
        
        return Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) {
            keyboardController?.show()
        }
    }
}
