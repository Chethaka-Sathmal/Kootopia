package com.ncs.kootopia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
    onMenuClick: () -> Unit,
    onEditClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onFindClick: () -> Unit,
    onCompileClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Code Editor $currentFileName") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Bar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editing Option"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(onClick = onUndoClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.undo),
                            contentDescription = "Undo"
                        )
                    }
                    IconButton(onClick = onRedoClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.redo),
                            contentDescription = "Redo"
                        )
                    }
                    IconButton(onClick = onFindClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Find"
                        )
                    }
                    IconButton(onClick = onCompileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountBox,
                            contentDescription = "Compile"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

/**
 * Code Editor - The main text editing component
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
                        text = "${i + 1}.",
                        style = TextStyle(fontSize = 16.sp, color = KootopiaColors.textSecondary),
                        modifier = Modifier
                            .height(24.dp)
                            .padding(vertical = 2.dp)
                            .background(KootopiaColors.surfaceDark)
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
