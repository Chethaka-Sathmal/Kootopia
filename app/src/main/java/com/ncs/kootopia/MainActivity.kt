package com.ncs.kootopia

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import com.ncs.kootopia.ui.theme.KootopiaTheme
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview


// MainActivity: Hosts the code editor UI and handles file operations
@OptIn(FlowPreview::class)
class MainActivity : ComponentActivity() {
    private lateinit var fileManager: FileManager
    private var currentFileName by mutableStateOf("Untitled")
    private val editorState = TextEditorState()

    override fun onPause() {
        super.onPause()
        saveFile(currentFileName)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Keep keyboard visible by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        setContent {
            val clipboardManager = LocalClipboardManager.current
            val syntaxRules = loadSyntaxRules(this, "python.json")
            var showMiniToolbar by remember { mutableStateOf(false) }
            var showFindReplace by remember { mutableStateOf(false) }
            var showCompilerInterface by remember { mutableStateOf(false) }
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            var compileOutput by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            // Auto-save and commit changes
            LaunchedEffect(editorState.textField.value) {
                snapshotFlow { editorState.textField.value }
                    .debounce(500)
                    .collect {
                        editorState.commitChange()
                        saveFile(currentFileName)
                    }
            }

            fileManager = FileManager(context)
            KootopiaTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = { 
                        DrawerContent(
                            initialFileName = currentFileName,
                            context = this,
                            onNewFile = { createNewFile(it) },
                            onOpenFile = { openFile(it) },
                            onSaveFile = { saveFile(it) }
                        )
                    }
                ) {
                    // Use BackgroundClickHandler to hide keyboard when touching non-text areas
                    BackgroundClickHandler {
                        MainEditorScaffold(
                            currentFileName = currentFileName,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onEditClick = { showMiniToolbar = !showMiniToolbar },
                            onUndoClick = { editorState.undo() },
                            onRedoClick = { editorState.redo() },
                            onFindClick = { showFindReplace = !showFindReplace },
                            onCompileClick = {
                                compileCode(context, editorState.textField.value.text, fileManager, currentFileName) { output ->
                                    compileOutput = output
                                    showCompilerInterface = true
                                }
                            },
                            onSaveClick = { fileName ->
                                saveFile(fileName)
                                currentFileName = fileName
                            },
                            onExecuteClick = {
                                compileCode(context, editorState.textField.value.text, fileManager, currentFileName) { output ->
                                    compileOutput = output
                                    showCompilerInterface = true
                                }
                            },
                            onRenameFile = { newFileName ->
                                // Save current content with new filename
                                fileManager.saveFile(newFileName, editorState.textField.value.text)
                                // Update current filename
                                currentFileName = newFileName
                            }
                        ) { innerPadding ->
                            Column(modifier = Modifier.padding(innerPadding)) {
                                if (showCompilerInterface) {
                                    CompilerInterface(
                                        clipboardManager, 
                                        compileOutput, 
                                        onClose = { showCompilerInterface = false }
                                    )
                                }

                                if (showFindReplace) {
                                    FindReplaceBar(
                                        editorState = editorState, 
                                        onClose = { showFindReplace = false }
                                    )
                                }

                                if (showMiniToolbar) {
                                    MiniToolbar(
                                        onCut = { cutText(editorState.textField.value, { editorState.onTextChange(it) }, clipboardManager) },
                                        onCopy = { copyText(editorState.textField.value, clipboardManager) },
                                        onPaste = { pasteText(editorState.textField.value, { editorState.onTextChange(it) }, clipboardManager) }
                                    )
                                }

                                CodeEditor(
                                    modifier = Modifier.weight(1f),
                                    editorState = editorState,
                                    syntaxRules = syntaxRules
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Create a new file and clear the editor (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)
    private fun createNewFile(filename: String) {
        val file = fileManager.createNewFile(filename)
        currentFileName = file
        editorState.textField.value = TextFieldValue("")
    }

    // Save current editor content to a file (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)
    private fun saveFile(filename: String ) {
        fileManager.saveFile(filename, editorState.textField.value.text)
    }

    // Open a file and load its content into the editor (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)

    private fun openFile(filename: String) {
        val content = fileManager.openFile(filename)
        editorState.textField.value = TextFieldValue(content)
        currentFileName = filename
    }


}

