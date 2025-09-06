package com.ncs.kootopia

import android.util.Log

/**
 * COMPILATION MODULE
 * ==================
 * 
 * This module handles code compilation via ADB reverse tunneling to a desktop Python server.
 * 
 * TESTING INSTRUCTIONS:
 * ====================
 * 
 * 1. SETUP (Desktop):
 *    - Install compilers: kotlinc, javac, python3
 *    - Run: python3 server.py (keep running)
 *    - Run: adb reverse tcp:8080 tcp:8080
 * 
 * 2. TEST SUCCESSFUL COMPILATION:
 *    Kotlin: fun main() { println("Hello!") }
 *    Java: public class Test { public static void main(String[] args) { System.out.println("Hello!"); } }
 *    Python: print("Hello!")
 * 
 * 3. TEST ERROR HIGHLIGHTING:
 *    Try invalid syntax like missing quotes or semicolons
 *    Should see red background on error lines
 * 
 * 4. TEST UNSUPPORTED EXTENSIONS:
 *    Change to .txt extension - compile button should be disabled
 * 
 * PROTOCOL:
 * =========
 * 1. Send filename + "END_OF_FILENAME"
 * 2. Send code + "END_OF_CODE"  
 * 3. Receive result until "END_OF_RESULT"
 * 4. Parse errors and highlight lines
 */

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.Socket

fun compileCode(
    context: Context,
    code: String,
    fileManager: FileManager,
    fileName: String,
    editorState: TextEditorState,
    onResult: (String) -> Unit
) {
    // ADB Setup Required:
    // User must run 'adb reverse tcp:8080 tcp:8080' on desktop with device connected via USB
    // This creates a reverse tunnel allowing the app to connect to localhost:8080 on the desktop
    
    // Set "Compiling..." message on main thread first
    onResult("Compiling...")
    
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Connect to localhost:8080 (via ADB reverse tunnel)
            val socket = Socket("localhost", 8080)
            val output = PrintWriter(socket.getOutputStream(), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            
            // Get filename (default to "untitled.kt" if null or empty)
            val fileNameToSend = if (fileName.isNullOrEmpty() || fileName == "Untitled") {
                "untitled.kt"
            } else {
                fileName
            }
            
            // DEBUG: Log what we're sending
            Log.d("CompileCode", "DEBUG: Sending filename to server: '$fileNameToSend'")
            Log.d("CompileCode", "DEBUG: Original fileName parameter: '$fileName'")
            
            // Send filename followed by sentinel
            output.println(fileNameToSend)
            output.println("END_OF_FILENAME")
            
            // Send code followed by sentinel
            output.println(code)
            output.println("END_OF_CODE")
            
            // Receive response until END_OF_RESULT
            val result = StringBuilder()
            var line: String?
            while (input.readLine().also { line = it } != null) {
                if (line == "END_OF_RESULT") break
                result.append(line).append("\n")
            }
            
            // Close connections
            input.close()
            output.close()
            socket.close()
            
            // Update UI on main thread
            withContext(Dispatchers.Main) {
                val resultText = result.toString().trim()
                onResult(resultText)
                
                // Parse for compiler errors and highlight erroneous lines
                highlightErrorLines(resultText, code, editorState)
            }
            
        } catch (e: Exception) {
            // Handle exceptions and show error message on main thread
            withContext(Dispatchers.Main) {
                onResult("Error: ${e.message} (Check ADB and server)")
            }
        }
    }
}

/**
 * Parse compiler errors and highlight erroneous lines in the editor with red background.
 * 
 * SUPPORTED ERROR FORMATS:
 * ========================
 * - Kotlin: "error: unresolved reference line 5"
 * - Java: "15:10: error: cannot find symbol"  
 * - Python: "File "script.py", line 3, in <module>"
 * - General: "syntax error at line 8"
 * 
 * TESTING:
 * ========
 * Try these error examples to see line highlighting:
 * 
 * Kotlin Error:
 *   fun main() {
 *       println("missing quote)  // Line 2 should be highlighted
 *   }
 * 
 * Java Error:
 *   public class Test {
 *       public static void main(String[] args) {
 *           System.out.println("missing semicolon")  // Line 3 highlighted
 *       }
 *   }
 * 
 * Python Error:
 *   print("missing quote)  // Line 1 should be highlighted
 */
private fun highlightErrorLines(result: String, code: String, editorState: TextEditorState) {
    try {
        // Regex patterns for different compiler error formats
        val errorPatterns = listOf(
            Regex("error:.* line (\\d+)", RegexOption.IGNORE_CASE),  // kotlinc: "error: ... line 5"
            Regex("(\\d+):\\d+: error:", RegexOption.IGNORE_CASE),   // javac: "15:10: error:"
            Regex("line (\\d+):", RegexOption.IGNORE_CASE),          // general: "line 8:"
            Regex("File \".*?\", line (\\d+)", RegexOption.IGNORE_CASE) // python: "File "x.py", line 3"
        )
        
        val errorLines = mutableSetOf<Int>()
        
        // Find all error line numbers
        for (pattern in errorPatterns) {
            pattern.findAll(result).forEach { match ->
                val lineNumber = match.groupValues[1].toIntOrNull()
                if (lineNumber != null && lineNumber > 0) {
                    errorLines.add(lineNumber - 1) // Convert to 0-based indexing
                }
            }
        }
        
        // If errors found, highlight the lines
        if (errorLines.isNotEmpty()) {
            val lines = code.split("\n")
            val annotatedString = buildAnnotatedString {
                var currentIndex = 0
                
                lines.forEachIndexed { lineIndex, line ->
                    if (errorLines.contains(lineIndex)) {
                        // Apply red background to error lines
                        withStyle(
                            SpanStyle(
                                background = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.3f)
                            )
                        ) {
                            append(line)
                        }
                    } else {
                        append(line)
                    }
                    
                    // Add newline except for last line
                    if (lineIndex < lines.size - 1) {
                        append("\n")
                    }
                    currentIndex += line.length + 1
                }
            }
            
            // Update editor with highlighted text
            val currentSelection = editorState.textField.value.selection
            editorState.onTextChange(
                TextFieldValue(
                    annotatedString = annotatedString,
                    selection = currentSelection
                )
            )
        }
        
    } catch (e: Exception) {
        // If parsing fails, silently continue without highlighting
        // This makes the highlighting optional as requested
    }
}