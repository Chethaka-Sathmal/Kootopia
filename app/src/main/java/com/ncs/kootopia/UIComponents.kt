package com.ncs.kootopia

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ncs.kootopia.ui.theme.KootopiaColors
import kotlinx.coroutines.launch

/**
 * UIComponents - Modular UI components for the code editor
 * 
 * This file contains reusable UI components that can be used across the app
 */

/**
 * Main Editor Scaffold - The main layout structure
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScaffold(
    currentFileName: String,
    editorState: TextEditorState,
    fileManager: FileManager,
    isConfigFile: Boolean,
    onMenuClick: () -> Unit,
    onEditClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onFindClick: () -> Unit,
    onCompileClick: () -> Unit,
    onSaveClick: (String) -> Unit,
    onExecuteClick: () -> Unit,
    onRenameFile: (String) -> Unit,
    onCopyClick: (() -> Unit) -> Unit,
    onPasteClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var fileNameInput by remember { mutableStateOf("") }
    var fileNameError by remember { mutableStateOf("") }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var savedFileName by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }
    var renameError by remember { mutableStateOf("") }
    var showNoSelectionDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        // Main TopAppBar
        TopAppBar(
            title = { 
                Row {
                    Text(text = "Kootopia", color = KootopiaColors.textPrimary)
                    Text(
                        text = if (currentFileName.isNotEmpty()) {
                            if (currentFileName.length > 20) {
                                "${currentFileName.take(17)}..."
                            } else {
                                " - $currentFileName"
                            }
                        } else {
                            " - Untitled"
                        },
                        color = KootopiaColors.textSecondary,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (currentFileName.isNotEmpty()) {
                                    renameInput = currentFileName
                                    renameError = ""
                                    showRenameDialog = true
                                }
                            }
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Bar",
                        tint = KootopiaColors.textPrimary
                    )
                }
            },
            actions = {
                TextButton(
                    onClick = { 
                        if (currentFileName.isEmpty()) {
                            showSaveDialog = true
                            fileNameInput = ""
                            fileNameError = ""
                        } else {
                            onSaveClick(currentFileName)
                            savedFileName = currentFileName
                            showSaveConfirmation = true
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("Save")
                }
                TextButton(
                    onClick = onExecuteClick,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("Execute")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = KootopiaColors.surfaceDark
            )
        )
        
        // Secondary Header - Toolbar
        SecondaryHeader(
            onUndoClick = onUndoClick,
            onRedoClick = onRedoClick,
            onCopyClick = { onCopyClick { showNoSelectionDialog = true } },
            onPasteClick = onPasteClick,
            onFindClick = onFindClick
        )
        
        // Main content area
        Box(modifier = Modifier.weight(1f)) {
            content(PaddingValues(0.dp))
        }
        
        // Slim Bottom Status Bar
        SlimBottomStatusBar(
            editorState = editorState
        )
    }
    
    // Save Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSaveDialog = false
                fileNameError = ""
            },
            title = { Text("Save File", color = KootopiaColors.textPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = fileNameInput,
                        onValueChange = { 
                            fileNameInput = it
                            fileNameError = ""
                        },
                        label = { Text("File name", color = KootopiaColors.textSecondary) },
                        placeholder = { Text("e.g., myfile.txt", color = KootopiaColors.textSecondary) },
                        isError = fileNameError.isNotEmpty(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = KootopiaColors.textPrimary,
                            unfocusedTextColor = KootopiaColors.textPrimary,
                            focusedLabelColor = KootopiaColors.accentBlue,
                            unfocusedLabelColor = KootopiaColors.textSecondary,
                            focusedBorderColor = KootopiaColors.accentBlue,
                            unfocusedBorderColor = KootopiaColors.textSecondary
                        )
                    )
                    if (fileNameError.isNotEmpty()) {
                        Text(
                            text = fileNameError,
                            color = KootopiaColors.errorRed,
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fileName = fileNameInput.trim()
                        if (fileName.isEmpty()) {
                            fileNameError = "Please enter a file name"
                        } else if (!fileName.contains(".")) {
                            fileNameError = "Please include a file extension (e.g., .txt, .py, .kt)"
                        } else {
                            onSaveClick(fileName)
                            savedFileName = fileName
                            showSaveDialog = false
                            fileNameError = ""
                            showSaveConfirmation = true
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("Save", color = KootopiaColors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showSaveDialog = false
                        fileNameError = ""
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = KootopiaColors.surfaceDark
        )
    }
    
    // Save Confirmation Dialog
    if (showSaveConfirmation) {
        val saveLocation = fileManager.getSaveLocation(savedFileName, isConfigFile)
        AlertDialog(
            onDismissRequest = { showSaveConfirmation = false },
            title = { 
                Text(
                    "File Saved", 
                    color = KootopiaColors.textPrimary
                ) 
            },
            text = {
                Column {
                    Text(
                        "File '$savedFileName' has been saved successfully!",
                        color = KootopiaColors.textPrimary
                    )
                    Text(
                        "Location: $saveLocation",
                        color = KootopiaColors.textSecondary,
                        style = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSaveConfirmation = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("OK", color = KootopiaColors.textPrimary)
                }
            },
            containerColor = KootopiaColors.surfaceDark
        )
    }
    
    // Rename Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { 
                showRenameDialog = false
                renameError = ""
            },
            title = { Text("Rename File", color = KootopiaColors.textPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { 
                            renameInput = it
                            renameError = ""
                        },
                        label = { Text("New file name", color = KootopiaColors.textSecondary) },
                        placeholder = { Text("e.g., myfile.txt", color = KootopiaColors.textSecondary) },
                        isError = renameError.isNotEmpty(),
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = KootopiaColors.textPrimary,
                            unfocusedTextColor = KootopiaColors.textPrimary,
                            focusedLabelColor = KootopiaColors.accentBlue,
                            unfocusedLabelColor = KootopiaColors.textSecondary,
                            focusedBorderColor = KootopiaColors.accentBlue,
                            unfocusedBorderColor = KootopiaColors.textSecondary
                        )
                    )
                    if (renameError.isNotEmpty()) {
                        Text(
                            text = renameError,
                            color = KootopiaColors.errorRed,
                            style = TextStyle(fontSize = 12.sp),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newName = renameInput.trim()
                        if (newName.isEmpty()) {
                            renameError = "Please enter a file name"
                        } else if (!newName.contains(".")) {
                            renameError = "Please include a file extension (e.g., .txt, .py, .kt)"
                        } else if (newName.contains("/") || newName.contains("\\") || newName.contains(":") || 
                                  newName.contains("*") || newName.contains("?") || newName.contains("\"") || 
                                  newName.contains("<") || newName.contains(">") || newName.contains("|")) {
                            renameError = "File name contains invalid characters. Avoid: / \\ : * ? \" < > |"
                        } else if (newName.length > 255) {
                            renameError = "File name is too long (maximum 255 characters)"
                        } else {
                            onRenameFile(newName)
                            showRenameDialog = false
                            renameError = ""
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("Rename", color = KootopiaColors.textPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showRenameDialog = false
                        renameError = ""
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = KootopiaColors.textSecondary
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = KootopiaColors.surfaceDark
        )
    }
    
    // No Selection Dialog
    if (showNoSelectionDialog) {
        AlertDialog(
            onDismissRequest = { showNoSelectionDialog = false },
            title = { Text("No Text Selected", color = KootopiaColors.textPrimary) },
            text = {
                Text(
                    "Please select some text to copy.",
                    color = KootopiaColors.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showNoSelectionDialog = false },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = KootopiaColors.accentBlue
                    )
                ) {
                    Text("OK", color = KootopiaColors.textPrimary)
                }
            },
            containerColor = KootopiaColors.surfaceDark
        )
    }
}

/**
 * Kootopia Editor - The main text editing component
 */
@Composable
fun CodeEditor(
    modifier: Modifier,
    editorState: TextEditorState,
    syntaxRules: SyntaxRules
) {
    val editorText = editorState.textField.value
    val scrollState = rememberScrollState()
    val lines = editorText.text.split("\n").ifEmpty { listOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Request focus and ensure keyboard stays visible
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    
    // Maintain focus when the editor text changes
    LaunchedEffect(editorText) {
        if (!editorText.selection.collapsed) {
            // If there's a selection, ensure focus is maintained
            focusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KootopiaColors.primaryDark)
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        Row {
            Column(modifier = Modifier
                .width(50.dp)
                .padding(end = 4.dp)) {
                lines.forEachIndexed { i, _ ->
                    Text(
                        text = "${i + 1}",
                        style = TextStyle(fontSize = 16.sp, color = KootopiaColors.textSecondary),
                        modifier = Modifier
                            .height(24.dp)
                            .padding(vertical = 2.dp)
                    )
                }
            }
            BasicTextField(
                value = editorText,
                onValueChange = { editorState.onTextChange(it) },
                textStyle = TextStyle(fontSize = 16.sp, color = Color.Transparent, lineHeight = 24.sp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            keyboardController?.show()
                        }
                    },
                decorationBox = { innerTextField ->
                    Box {
                        androidx.compose.material3.Text(
                            text = highlightSyntax(editorText.text, syntaxRules),
                            style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = KootopiaColors.textPrimary)
                        )

                        if (editorText.text.isEmpty()) {
                            Text("Type here...", color = KootopiaColors.textSecondary, lineHeight = 24.sp)
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Background Click Handler - Handles clicks on non-text areas to hide keyboard
 */
@Composable
fun BackgroundClickHandler(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
    ) {
        content()
    }
}

/**
 * Highlight text syntax based on given rules (keywords, comments, strings)
 */
fun highlightSyntax(text: String, rules: SyntaxRules): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // Keywords (blue)
        rules.keywords.forEach { keyword ->
            "\\b$keyword\\b".toRegex().findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = KootopiaColors.accentBlue),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Comments (green)
        rules.comments.forEach { comment ->
            Regex("${Regex.escape(comment)}.*").findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = KootopiaColors.successGreen),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Strings (orange)
        val stringRegex = Regex("\".*?\"|'.*?'")
        stringRegex.findAll(text).forEach { match ->
            addStyle(
                SpanStyle(color = KootopiaColors.errorRed),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}

/**
 * Secondary Header - Toolbar with editing actions
 */
@Composable
fun SecondaryHeader(
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onCopyClick: () -> Unit,
    onPasteClick: () -> Unit,
    onFindClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KootopiaColors.surfaceDark)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        // Left side - Undo and Redo
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onUndoClick) {
                Icon(
                    painter = painterResource(id = R.drawable.undo),
                    contentDescription = "Undo",
                    tint = KootopiaColors.textPrimary
                )
            }
            IconButton(onClick = onRedoClick) {
                Icon(
                    painter = painterResource(id = R.drawable.redo),
                    contentDescription = "Redo",
                    tint = KootopiaColors.textPrimary
                )
            }
        }
        
        // Right side - Copy, Paste, and Replace
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onCopyClick) {
                Icon(
                    painter = painterResource(id = R.drawable.copy),
                    contentDescription = "Copy",
                    tint = KootopiaColors.textPrimary
                )
            }
            IconButton(onClick = onPasteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.paste),
                    contentDescription = "Paste",
                    tint = KootopiaColors.textPrimary
                )
            }
            IconButton(onClick = onFindClick) {
                Icon(
                    painter = painterResource(id = R.drawable.replace),
                    contentDescription = "Find and Replace",
                    tint = KootopiaColors.textPrimary
                )
            }
        }
    }
}

/**
 * Slim Bottom Status Bar - Shows character count and cursor position
 */
@Composable
fun SlimBottomStatusBar(
    editorState: TextEditorState
) {
    val editorText = editorState.textField.value
    val characterCount = editorText.text.length
    val (row, col) = calculateCursorPosition(editorText)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KootopiaColors.surfaceDark)
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 16.dp), // Add bottom padding to prevent it from being too low
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        // Character count
        Text(
            text = "Chars: $characterCount",
            color = KootopiaColors.textSecondary,
            style = TextStyle(fontSize = 12.sp)
        )
        
        // Cursor position
        Text(
            text = "Row: $row, Col: $col",
            color = KootopiaColors.textSecondary,
            style = TextStyle(fontSize = 12.sp)
        )
    }
}

/**
 * Calculate word count from text
 */
fun calculateWordCount(text: String): Int {
    if (text.isBlank()) return 0
    return text.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
}

/**
 * Calculate cursor position (row and column) from TextFieldValue
 */
fun calculateCursorPosition(textFieldValue: TextFieldValue): Pair<Int, Int> {
    val text = textFieldValue.text
    val cursorPosition = textFieldValue.selection.start
    
    val lines = text.substring(0, cursorPosition).split("\n")
    val row = lines.size
    val col = if (lines.isNotEmpty()) lines.last().length + 1 else 1
    
    return Pair(row, col)
}
