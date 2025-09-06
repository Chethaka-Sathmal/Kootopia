package com.ncs.kootopia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
    private var hasUnsavedChanges by mutableStateOf(false)
    private var autoSaveEnabled by mutableStateOf(true)
    private var isEditingConfig by mutableStateOf(false)
    
    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            openFileFromUri(selectedUri)
        }
    }

    override fun onPause() {
        super.onPause()
        if (currentFileName.isNotEmpty() && currentFileName != "Untitled") {
            saveFile(currentFileName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Keep keyboard visible by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        
        setContent {
            val clipboardManager = LocalClipboardManager.current
            val syntaxRules = loadSyntaxRules(this, "kotlin.json")
            var showMiniToolbar by remember { mutableStateOf(false) }
            var showFindReplace by remember { mutableStateOf(false) }
            var showCompilerInterface by remember { mutableStateOf(false) }
            var showConfigurationDialog by remember { mutableStateOf(false) }
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            var compileOutput by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            // Track unsaved changes in real-time
            LaunchedEffect(editorState.textField.value) {
                hasUnsavedChanges = editorState.hasUnsavedChanges()
            }

            // Auto-save and commit changes (but don't update hasUnsavedChanges here)
            // Only auto-save if enabled
            LaunchedEffect(editorState.textField.value, autoSaveEnabled) {
                if (autoSaveEnabled) {
                    snapshotFlow { editorState.textField.value }
                        .debounce(2000) // 2 seconds
                        .collect {
                            editorState.commitChange()
                            saveFile(currentFileName)
                        }
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
                            hasUnsavedChanges = hasUnsavedChanges,
                            autoSaveEnabled = autoSaveEnabled,
                            onNewFile = { createNewFile(it) },
                            onNewUntitledFile = { createNewUntitledFile() },
                            onOpenFile = { launchFilePicker() },
                            onSaveFile = { saveFile(it) },
                            onToggleAutoSave = { autoSaveEnabled = !autoSaveEnabled },
                            onConfigure = { 
                                showConfigurationDialog = true
                            }
                        )
                    }
                ) {
                    // Use BackgroundClickHandler to hide keyboard when touching non-text areas
                    BackgroundClickHandler {
                        MainEditorScaffold(
                            currentFileName = currentFileName,
                            editorState = editorState,
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
                            },
                            onCopyClick = { showDialog ->
                                val start = editorState.textField.value.selection.start
                                val end = editorState.textField.value.selection.end
                                if (start == end) {
                                    // No text selected - show dialog
                                    showDialog()
                                } else {
                                    copyText(editorState.textField.value, clipboardManager)
                                }
                            },
                            onPasteClick = { pasteText(editorState.textField.value, { editorState.onTextChange(it) }, clipboardManager) }
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
                
                // Configuration Dialog
                if (showConfigurationDialog) {
                    ConfigurationDialog(
                        fileManager = fileManager,
                        onDismiss = { showConfigurationDialog = false },
                        onEditConfig = { configFileName ->
                            openConfigFile(configFileName)
                        },
                        onCreateConfig = { configFileName ->
                            createNewConfigFile(configFileName)
                        }
                    )
                }
            }
        }
    }

    // Create a new file and clear the editor (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)
    private fun createNewFile(filename: String) {
        val file = fileManager.createNewFile(filename)
        currentFileName = file
        isEditingConfig = false
        editorState.textField.value = TextFieldValue("")
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Create a new untitled file
    private fun createNewUntitledFile() {
        currentFileName = "Untitled"
        isEditingConfig = false
        editorState.textField.value = TextFieldValue("")
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Save current editor content to a file (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)
    private fun saveFile(filename: String ) {
        if (isEditingConfig) {
            fileManager.saveConfiguration(filename, editorState.textField.value.text)
        } else {
            fileManager.saveFile(filename, editorState.textField.value.text)
        }
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Open a file and load its content into the editor (This function takes lambda callbacks for New, Open, and Save actions in DrawerContent)
    private fun openFile(filename: String) {
        val content = fileManager.openFile(filename)
        editorState.textField.value = TextFieldValue(content)
        currentFileName = filename
        isEditingConfig = false
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Launch system file picker
    private fun launchFilePicker() {
        filePickerLauncher.launch("*/*")
    }

    // Open file from URI (from system file picker)
    private fun openFileFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val content = inputStream.bufferedReader().use { it.readText() }
                editorState.textField.value = TextFieldValue(content)
                
                // Extract filename from URI
                val fileName = getFileNameFromUri(uri) ?: "Unknown"
                currentFileName = fileName
                isEditingConfig = false
                editorState.forceCommit()
                hasUnsavedChanges = false
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error opening file from URI: ${e.message}")
        }
    }

    // Extract filename from URI
    private fun getFileNameFromUri(uri: Uri): String? {
        return when (uri.scheme) {
            "content" -> {
                contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex >= 0) {
                        cursor.getString(nameIndex)
                    } else null
                }
            }
            "file" -> uri.lastPathSegment
            else -> uri.lastPathSegment
        }
    }

    // Open an existing configuration file for editing
    private fun openConfigFile(configFileName: String) {
        val content = fileManager.loadConfiguration(configFileName)
        editorState.textField.value = TextFieldValue(content)
        currentFileName = configFileName
        isEditingConfig = true
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Create a new configuration file
    private fun createNewConfigFile(configFileName: String) {
        // Create a template configuration file
        val templateContent = """
{
  "keywords": ["keyword1", "keyword2", "keyword3"],
  "comments": ["//", "/*", "*/"],
  "strings": ["\"", "'"]
}
        """.trimIndent()
        
        editorState.textField.value = TextFieldValue(templateContent)
        currentFileName = configFileName
        isEditingConfig = true
        editorState.forceCommit()
        hasUnsavedChanges = false
    }


}

