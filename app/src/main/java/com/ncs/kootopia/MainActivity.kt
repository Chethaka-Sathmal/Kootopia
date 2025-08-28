package com.ncs.kootopia

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ncs.kootopia.ui.theme.KootopiaTheme
import com.ncs.kootopia.ui.theme.KootopiaColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch


// MainActivity: Hosts the code editor UI and handles file operations
@OptIn(ExperimentalMaterial3Api::class)
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
                    drawerContent = { DrawerContent(
                        initialFileName = currentFileName,
                        context = this,
                        onNewFile = { createNewFile(it) },
                        onOpenFile = { openFile(it) },
                        onSaveFile = {saveFile(it)}
                    )
                    }
                ) {

// Scaffold: Provides the basic layout structure with top bar, bottom bar, and main content area

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(text = "Code Editor $currentFileName") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu Bar"
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showMiniToolbar = !showMiniToolbar }) {
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
                                    IconButton(onClick = { editorState.undo() }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.undo),
                                            contentDescription = "Undo"
                                        )
                                    }
                                    IconButton(onClick = { editorState.redo() }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.redo),
                                            contentDescription = "redo"
                                        )
                                    }
                                    IconButton(onClick ={ showFindReplace = !showFindReplace }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Find"
                                        )
                                    }
                                    IconButton(onClick ={
                                        compileCode(context,editorState.textField.value.text,fileManager,currentFileName) { output ->
                                            compileOutput = output
                                            showCompilerInterface = true
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBox,
                                            contentDescription = "Find"
                                        )
                                    }
                                }
                            )
                        }

                    ) { innerPadding ->
                        // Main content goes here
                        Column (modifier = Modifier.padding(innerPadding)){

                            if(showCompilerInterface){
                                CompilerInterface(clipboardManager,compileOutput, onClose = {showCompilerInterface =false})
                            }

                            if (showFindReplace) {
                                FindReplaceBar(editorState = editorState, onClose = {
                                    showFindReplace = false
                                })
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


// CodeEditor Composable: Displays editable text with line numbers and syntax highlighting
@Composable
fun CodeEditor(
    modifier: Modifier,
    editorState: TextEditorState,
    syntaxRules: SyntaxRules
) {
    val editorText = editorState.textField.value
    val scrollState = rememberScrollState()
    val lines = editorText.text.split("\n").ifEmpty { listOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(KootopiaColors.primaryDark) // Use custom background color
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
                        style = TextStyle(fontSize = 16.sp, color = KootopiaColors.textSecondary), // Use custom secondary text color
                        modifier = Modifier
                            .height(24.dp)
                            .padding(vertical = 2.dp)
                            .background(KootopiaColors.surfaceDark) // Use custom surface color
                    )
                }
            }
            BasicTextField(
                value = editorText,
                onValueChange = { editorState.onTextChange(it) },
                textStyle = TextStyle(fontSize = 16.sp,color = Color.Transparent, lineHeight = 24.sp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                decorationBox = { innerTextField ->
                    Box {
                        androidx.compose.material3.Text(
                            text = highlightSyntax(editorText.text, syntaxRules),
                            style = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, color = KootopiaColors.textPrimary)
                        )

                        Log.d("FileManager", " to ${highlightSyntax(editorText.text, syntaxRules)}")
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

//Highlight text syntax based on given rules (keywords, comments, strings)
fun highlightSyntax(text: String, rules: SyntaxRules): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // 🔹 Keywords (blue)
        rules.keywords.forEach { keyword ->
            "\\b$keyword\\b".toRegex().findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = KootopiaColors.accentBlue),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // 🔹 Comments (green)
        rules.comments.forEach { comment ->
            Regex("${Regex.escape(comment)}.*").findAll(text).forEach { match ->
                addStyle(
                    SpanStyle(color = KootopiaColors.successGreen),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // 🔹 Strings (orange)
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

