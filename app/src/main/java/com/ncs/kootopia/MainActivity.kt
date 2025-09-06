package com.ncs.kootopia

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.WindowManager
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ncs.kootopia.ui.theme.KootopiaTheme
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.FlowPreview


/**
 * MainActivity: Hosts the code editor UI and handles file operations
 * 
 * COMPILATION TESTING GUIDE:
 * =========================
 * 
 * SETUP REQUIREMENTS:
 * 1. Desktop: Install kotlinc, javac, python3
 * 2. Desktop: Run 'python3 server.py' (keep running)
 * 3. Desktop: Run 'adb reverse tcp:8080 tcp:8080'
 * 4. Android: Connect device via USB with debugging enabled
 * 
 * TEST CASES:
 * ===========
 * 
 * 1. KOTLIN COMPILATION (.kt):
 *    - Change extension to .kt (should be default)
 *    - Enter test code:
 *      fun main() {
 *          println("Hello from Kotlin!")
 *      }
 *    - Press compile → Should show "Compilation successful!"
 * 
 * 2. JAVA COMPILATION (.java):
 *    - Change extension to .java via drawer menu
 *    - Enter test code:
 *      public class HelloWorld {
 *          public static void main(String[] args) {
 *              System.out.println("Hello from Java!");
 *          }
 *      }
 *    - Press compile → Should show "Compilation successful!"
 * 
 * 3. PYTHON COMPILATION (.py):
 *    - Change extension to .py via drawer menu
 *    - Enter test code:
 *      print("Hello from Python!")
 *    - Press compile → Should show "Compilation successful!"
 * 
 * 4. ERROR TESTING:
 *    - Try invalid syntax (missing quotes, semicolons)
 *    - Should show red error message and highlight error lines
 * 
 * 5. UNSUPPORTED EXTENSION:
 *    - Try .txt, .cpp, or other unsupported extensions
 *    - Compile button should be disabled
 *    - If somehow triggered, should show "Unsupported file type" message
 * 
 * FEATURES TO VERIFY:
 * - Extension change updates syntax highlighting immediately
 * - Compile button enabled/disabled based on extension
 * - Error lines highlighted in red background
 * - Success/failure messages color-coded (green/red)
 */
@OptIn(FlowPreview::class)
class MainActivity : ComponentActivity() {
    // Supported extensions for compilation (matching Python server COMPILERS dict)
    private val supportedExtensions = setOf(".kt", ".java", ".py")
    
    private lateinit var fileManager: FileManager
    private var currentFileName by mutableStateOf("Untitled")
    private var currentExtension by mutableStateOf(".kt") // Default extension
    private val editorState = TextEditorState()
    private var hasUnsavedChanges by mutableStateOf(false)
    private var autoSaveEnabled by mutableStateOf(true)
    private var isEditingConfig by mutableStateOf(false)
    private var lastSourceFileName by mutableStateOf("Untitled")
    private var lastSourceContent by mutableStateOf("")
    
    // File picker launcher with default directory
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                openFileFromUri(uri)
            }
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
            val syntaxRules = loadSyntaxRules(this, getConfigFileForExtension(currentExtension))
            var showMiniToolbar by remember { mutableStateOf(false) }
            var showFindReplace by remember { mutableStateOf(false) }
            var showCompilerInterface by remember { mutableStateOf(false) }
            var showConfigurationDialog by remember { mutableStateOf(false) }
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            var compileOutput by remember { mutableStateOf("") }
            var showCompilationDrawer by remember { mutableStateOf(false) }
            var isCompiling by remember { mutableStateOf(false) }
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
                            fileManager = fileManager,
                            isConfigFile = isEditingConfig,
                            hasUnsavedChanges = hasUnsavedChanges,
                            autoSaveEnabled = autoSaveEnabled,
                            onNewFile = { 
                                createNewFile(it)
                                scope.launch { drawerState.close() }
                            },
                            onNewUntitledFile = { 
                                createNewUntitledFile()
                                scope.launch { drawerState.close() }
                            },
                            onOpenFile = { launchFilePicker() },
                            onSaveFile = { saveFile(it) },
                            onToggleAutoSave = { autoSaveEnabled = !autoSaveEnabled },
                            onConfigure = { 
                                showConfigurationDialog = true
                            },
                            onSourceCodeClick = { switchToSourceCode() }
                        )
                    }
                ) {
                    // Use BackgroundClickHandler to hide keyboard when touching non-text areas
                    BackgroundClickHandler {
                        MainEditorScaffold(
                            currentFileName = currentFileName,
                            editorState = editorState,
                            fileManager = fileManager,
                            isConfigFile = isEditingConfig,
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onEditClick = { showMiniToolbar = !showMiniToolbar },
                            onUndoClick = { editorState.undo() },
                            onRedoClick = { editorState.redo() },
                            onFindClick = { showFindReplace = !showFindReplace },
                            onCompileClick = {
                                if (isCompilationSupported()) {
                                    compileCode(context, editorState.textField.value.text, fileManager, getCurrentFileNameWithExtension(), editorState) { output ->
                                        compileOutput = output
                                        showCompilerInterface = true
                                    }
                                }
                            },
                            onSaveClick = { fileName ->
                                saveFile(fileName)
                                currentFileName = fileName
                            },
                            onExecuteClick = {
                                if (isCompilationSupported()) {
                                    isCompiling = true
                                    showCompilationDrawer = true
                                    compileCode(context, editorState.textField.value.text, fileManager, getCurrentFileNameWithExtension(), editorState) { output ->
                                        compileOutput = output
                                        isCompiling = false
                                    }
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
                                // Code Editor
                                CodeEditor(
                                    modifier = Modifier.weight(1f),
                                    editorState = editorState,
                                    syntaxRules = syntaxRules
                                )
                                
                                // Compilation Result Panel (Bottom Split)
                                CompilationResultPanel(
                                    isVisible = showCompilationDrawer,
                                    isCompiling = isCompiling,
                                    result = compileOutput,
                                    onDismiss = { showCompilationDrawer = false }
                                )

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
        updateExtensionFromFileName(filename)
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
        updateExtensionFromFileName(filename)
        isEditingConfig = false
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Launch system file picker with default location
    private fun launchFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            
            // Try to set default directory to Documents folder
            try {
                // Create URI for Documents folder - this should open to Documents by default
                val documentsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADocuments")
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsUri)
                Log.d("MainActivity", "Set initial URI to Documents folder: $documentsUri")
            } catch (e: Exception) {
                Log.w("MainActivity", "Could not set initial directory: ${e.message}")
                // Fallback: try without initial URI
            }
        }
        
        filePickerLauncher.launch(intent)
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
                updateExtensionFromFileName(fileName)
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
        // Save current source code content before switching
        if (!isEditingConfig) {
            lastSourceFileName = currentFileName
            lastSourceContent = editorState.textField.value.text
        }
        
        val content = fileManager.loadConfiguration(configFileName)
        editorState.textField.value = TextFieldValue(content)
        currentFileName = configFileName
        isEditingConfig = true
        editorState.forceCommit()
        hasUnsavedChanges = false
    }

    // Create a new configuration file
    private fun createNewConfigFile(configFileName: String) {
        // Save current source code content before switching
        if (!isEditingConfig) {
            lastSourceFileName = currentFileName
            lastSourceContent = editorState.textField.value.text
        }
        
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

    // Switch to source code mode
    private fun switchToSourceCode() {
        if (isEditingConfig) {
            // Save current config content
            if (currentFileName.isNotEmpty()) {
                saveFile(currentFileName)
            }
            
            // Switch back to source code
            isEditingConfig = false
            currentFileName = lastSourceFileName
            editorState.textField.value = TextFieldValue(lastSourceContent)
            editorState.forceCommit()
            hasUnsavedChanges = false
        }
    }
    
    // ==========================================
    // EXTENSION MANAGEMENT HELPER FUNCTIONS
    // ==========================================
    
    /**
     * Get the current filename with proper extension for sending to server.
     * Examples: "untitled.kt", "MainActivity.java", "script.py"
     */
    private fun getCurrentFileNameWithExtension(): String {
        val result = if (currentFileName == "Untitled") {
            "untitled$currentExtension"  // e.g., "untitled.kt"
        } else {
            val nameWithoutExt = File(currentFileName).nameWithoutExtension
            "$nameWithoutExt$currentExtension"  // e.g., "MainActivity.java"
        }
        
        // DEBUG: Log the filename generation
        Log.d("MainActivity", "DEBUG: getCurrentFileNameWithExtension()")
        Log.d("MainActivity", "DEBUG: currentFileName = '$currentFileName'")
        Log.d("MainActivity", "DEBUG: currentExtension = '$currentExtension'")
        Log.d("MainActivity", "DEBUG: result = '$result'")
        
        return result
    }
    
    /**
     * Map file extensions to their corresponding syntax highlighting config files.
     * This enables automatic syntax highlighting when extension changes.
     */
    private fun getConfigFileForExtension(extension: String): String {
        return when (extension) {
            ".kt" -> "kotlin.json"      // Kotlin syntax highlighting
            ".java" -> "java.json"      // Java syntax highlighting  
            ".py" -> "python.json"      // Python syntax highlighting
            else -> "fallback.json"     // No highlighting for unsupported types
        }
    }
    
    /**
     * Check if the current file extension supports compilation.
     * Used to enable/disable compile button dynamically.
     */
    private fun isCompilationSupported(): Boolean {
        return supportedExtensions.contains(currentExtension)
    }
    
    /**
     * Change the current file extension and update filename if needed.
     * Triggers syntax highlighting refresh automatically via compose recomposition.
     */
    private fun changeFileExtension(newExtension: String) {
        Log.d("MainActivity", "DEBUG: changeFileExtension called")
        Log.d("MainActivity", "DEBUG: old currentExtension = '$currentExtension'")
        Log.d("MainActivity", "DEBUG: new extension = '$newExtension'")
        Log.d("MainActivity", "DEBUG: old currentFileName = '$currentFileName'")
        
        currentExtension = newExtension
        // Update filename if it's not the default "Untitled"
        if (currentFileName != "Untitled") {
            val nameWithoutExt = File(currentFileName).nameWithoutExtension
            currentFileName = "$nameWithoutExt$newExtension"
            Log.d("MainActivity", "DEBUG: updated currentFileName = '$currentFileName'")
        }
        
        Log.d("MainActivity", "DEBUG: final currentExtension = '$currentExtension'")
    }
    
    /**
     * Extract and set extension from a filename when opening files.
     * Ensures extension state stays synchronized with opened files.
     */
    private fun updateExtensionFromFileName(fileName: String) {
        val extension = File(fileName).extension
        if (extension.isNotEmpty()) {
            currentExtension = ".$extension"  // Add dot prefix for consistency
        }
    }


}


