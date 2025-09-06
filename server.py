#!/usr/bin/env python3
"""
Desktop Compilation and Execution Server for Kootopia Android Text Editor
Listens on localhost:8080 for compilation requests via ADB reverse tunnel.

SETUP INSTRUCTIONS:
===================

1. INSTALL REQUIRED COMPILERS:
   - Kotlin: Download from https://kotlinlang.org/docs/command-line.html
   - Java: Install JDK (sudo apt install default-jdk on Ubuntu)
   - Python: Usually pre-installed (python3 --version to check)

2. SETUP ADB CONNECTION:
   - Enable USB debugging on Android device
   - Connect device via USB cable
   - Run: adb reverse tcp:8080 tcp:8080

3. START THE SERVER:
   - Run: python3 server.py
   - Server will start on localhost:8080
   - Keep this running while using the app

4. TEST THE APP:
   - Open Kootopia app on Android device
   - Write test code (see examples below)
   - Press "Compile" button
   - View compilation AND execution results in the compiler interface

COMPILATION AND EXECUTION PROCESS:
=================================

For Kotlin (.kt):
1. Compiles with: kotlinc <file>.kt -include-runtime -d <file>.jar
2. Executes with: java -jar <file>.jar

For Java (.java):
1. Compiles with: javac <file>.java
2. Executes with: java -cp <directory> <classname>

For Python (.py):
1. Syntax check with: python3 -m py_compile <file>.py
2. Executes with: python3 <file>.py

TESTING EXAMPLES:
================

Kotlin (.kt):
```kotlin
fun main() {
    println("Hello from Kotlin!")
    println("Today is a great day to code!")
}
```

Java (.java):
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from Java!");
        System.out.println("Compilation and execution successful!");
    }
}
```

Python (.py):
```python
print("Hello from Python!")
print("This will be both compiled and executed!")
```

ERROR TESTING:
=============
Try invalid syntax to test error highlighting:

Kotlin Error:
```kotlin
fun main() {
    println("Missing quote)
}
```

Java Error:
```java
public class Test {
    public static void main(String[] args) {
        System.out.println("Missing semicolon")
    }
}
```

UNSUPPORTED EXTENSION TESTING:
=============================
Try changing extension to .txt or .cpp to see "Unsupported file type" message.

Supported file types: .kt (Kotlin), .java (Java), .py (Python)
"""

import socket
import tempfile
import subprocess
import os
import sys
from typing import Dict, List, Tuple

# ====================================
# COMPILER CONFIGURATION (Modular Design)
# ====================================
# 
# To add support for new languages, simply add entries here:
# Each entry contains: [compile_command, execute_command]
# Use None for execute_command if no execution is needed
#
# The Android app's supportedExtensions set should match these keys.
#
COMPILERS: Dict[str, Dict[str, any]] = {
    '.kt': {
        'compile': lambda file_path: ['kotlinc', file_path, '-include-runtime', '-d', file_path.replace('.kt', '.jar')],
        'execute': lambda file_path: ['java', '-jar', file_path.replace('.kt', '.jar')],
        'description': 'Kotlin compiler and executor'
    },
    '.java': {
        'compile': lambda file_path: ['javac', file_path],
        'execute': lambda file_path: ['java', '-cp', os.path.dirname(file_path), os.path.basename(file_path).replace('.java', '')],
        'description': 'Java compiler and executor'
    },
    '.py': {
        'compile': lambda file_path: ['python3', '-m', 'py_compile', file_path],
        'execute': lambda file_path: ['python3', file_path],
        'description': 'Python syntax checker and executor'
    }
}

def run_command(command: List[str], step_name: str, timeout: int = 30) -> Tuple[bool, str]:
    """
    Run a command and return success status and output.
    
    Args:
        command: Command to execute as list of strings
        step_name: Name of the step for logging
        timeout: Timeout in seconds
    
    Returns:
        Tuple of (success, output)
    """
    try:
        print(f"🔧 EXECUTING {step_name}: {' '.join(command)}")
        
        result = subprocess.run(
            command,
            capture_output=True,
            text=True,
            timeout=timeout
        )
        
        print(f"📊 {step_name} EXIT CODE: {result.returncode}")
        print(f"📤 STDOUT LENGTH: {len(result.stdout) if result.stdout else 0} characters")
        print(f"📤 STDERR LENGTH: {len(result.stderr) if result.stderr else 0} characters")
        
        # Combine stdout and stderr for complete output
        output = ""
        if result.stdout:
            output += result.stdout
            print(f"📤 STDOUT:\n{result.stdout}")
        if result.stderr:
            if output:
                output += "\n"
            output += result.stderr
            print(f"📤 STDERR:\n{result.stderr}")
        
        success = result.returncode == 0
        print(f"{'✅' if success else '❌'} {step_name} {'SUCCESSFUL' if success else 'FAILED'}")
        print("=" * 50)
        
        return success, output
        
    except subprocess.TimeoutExpired:
        error_msg = f"{step_name} failed! Timeout: Process took too long to complete."
        print(f"⏰ TIMEOUT: {error_msg}")
        return False, error_msg
    except FileNotFoundError:
        compiler_name = command[0] if command else "unknown"
        error_msg = f"{step_name} failed! Command '{compiler_name}' not found. Please install it."
        print(f"🚫 COMMAND NOT FOUND: {error_msg}")
        return False, error_msg
    except Exception as e:
        error_msg = f"{step_name} failed! Error: {str(e)}"
        print(f"💥 EXCEPTION: {error_msg}")
        return False, error_msg

def handle_compilation(file_path: str, extension: str) -> str:
    """
    Handle compilation and execution of a file based on its extension.
    
    Args:
        file_path: Path to the temporary file to compile and execute
        extension: File extension (e.g., '.kt', '.java', '.py')
    
    Returns:
        Combined compilation and execution result message
    """
    if extension not in COMPILERS:
        return f"Unsupported file type: {extension}. Please configure the script."
    
    print(f"📁 FILE PATH: {file_path}")
    print(f"📄 FILE EXTENSION: {extension}")
    print("=" * 50)
    
    compiler_config = COMPILERS[extension]
    all_output = []
    
    # Step 1: Compilation
    compile_command = compiler_config['compile'](file_path)
    compile_success, compile_output = run_command(compile_command, "COMPILATION")
    
    if compile_output.strip():
        all_output.append(f"=== COMPILATION OUTPUT ===\n{compile_output}")
    
    if not compile_success:
        final_result = f"Compilation failed!\n{compile_output}" if compile_output else "Compilation failed!"
        print(f"❌ FINAL RESULT: {final_result}")
        return final_result
    
    # Step 2: Execution (only if compilation succeeded)
    if 'execute' in compiler_config:
        execute_command = compiler_config['execute'](file_path)
        execute_success, execute_output = run_command(execute_command, "EXECUTION")
        
        if execute_output.strip():
            all_output.append(f"=== EXECUTION OUTPUT ===\n{execute_output}")
        
        if not execute_success:
            final_result = f"Compilation successful, but execution failed!\n" + "\n\n".join(all_output)
            print(f"⚠️ FINAL RESULT: {final_result}")
            return final_result
    
    # Success case
    if all_output:
        final_result = f"Compilation and execution successful!\n\n" + "\n\n".join(all_output)
    else:
        final_result = "Compilation and execution successful!"
    
    print(f"✅ FINAL RESULT: {final_result}")
    return final_result

def handle_client_connection(client_socket: socket.socket, client_address: Tuple[str, int]) -> None:
    """
    Handle a single client connection and compilation request.
    
    Args:
        client_socket: Connected client socket
        client_address: Client address tuple (host, port)
    """
    temp_file_path = None
    
    try:
        print(f"\n🔌 NEW CONNECTION from {client_address}")
        print("=" * 60)
        print("🚀 DEBUG: Starting client connection handling")
        
        # Read all data from client in one go
        print("📥 DEBUG: Starting to read all data from client...")
        all_data = ""
        attempts = 0
        
        while True:
            attempts += 1
            print(f"📥 DEBUG: Data read attempt #{attempts}")
            
            try:
                data = client_socket.recv(1024)
                print(f"📥 DEBUG: Received raw data: {data}")
                
                if not data:
                    print("📥 DEBUG: No more data received")
                    break
                    
                decoded_data = data.decode('utf-8')
                print(f"📥 DEBUG: Decoded data: '{decoded_data}'")
                all_data += decoded_data
                
                # Check if we have both markers
                if "END_OF_FILENAME" in all_data and "END_OF_CODE" in all_data:
                    print("📥 DEBUG: Found both END_OF_FILENAME and END_OF_CODE markers")
                    break
                    
            except Exception as e:
                print(f"📥 DEBUG: Exception reading data: {e}")
                break
                
            if attempts > 10:
                print("📥 DEBUG: Too many data read attempts, breaking")
                break
        
        print(f"📥 DEBUG: All data reading complete. Total length: {len(all_data)}")
        print(f"📥 DEBUG: All data content: '{all_data}'")
        
        # Parse filename
        if "END_OF_FILENAME" not in all_data:
            print("❌ ERROR: END_OF_FILENAME marker not found")
            error_msg = b"Error: END_OF_FILENAME marker not found\nEND_OF_RESULT\n"
            print(f"📤 DEBUG: Sending error: {error_msg}")
            client_socket.send(error_msg)
            return
        
        filename_section, rest = all_data.split("END_OF_FILENAME", 1)
        filename = filename_section.strip()
        print(f"📥 DEBUG: Extracted filename: '{filename}'")
        
        if not filename:
            print("❌ ERROR: No filename received")
            error_msg = b"Error: No filename received\nEND_OF_RESULT\n"
            print(f"📤 DEBUG: Sending error: {error_msg}")
            client_socket.send(error_msg)
            return
            
        print(f"📄 FILENAME: {filename}")
        
        # Parse code
        if "END_OF_CODE" not in rest:
            print("❌ ERROR: END_OF_CODE marker not found")
            error_msg = b"Error: END_OF_CODE marker not found\nEND_OF_RESULT\n"
            print(f"📤 DEBUG: Sending error: {error_msg}")
            client_socket.send(error_msg)
            return
        
        code_section, _ = rest.split("END_OF_CODE", 1)
        code = code_section.strip()
        print(f"📝 DEBUG: Extracted code: '{code}'")
        print(f"📝 CODE RECEIVED: {len(code)} characters")
        print(f"📝 CODE PREVIEW: {code[:100]}{'...' if len(code) > 100 else ''}")
        
        if not code:
            print("❌ ERROR: No code received")
            error_msg = b"Error: No code received\nEND_OF_RESULT\n"
            print(f"📤 DEBUG: Sending error: {error_msg}")
            client_socket.send(error_msg)
            return
        
        print("🔧 DEBUG: Starting file processing...")
        
        # Extract file extension
        _, extension = os.path.splitext(filename)
        print(f"🔧 DEBUG: Extracted extension: '{extension}' from filename: '{filename}'")
        
        if not extension:
            extension = '.kt'  # Default to Kotlin if no extension
            print(f"🔧 DEBUG: No extension found, defaulting to: '{extension}'")
            
        print(f"🔧 FILE EXTENSION: {extension}")
        
        # Create temporary file with proper extension
        print("💾 DEBUG: Creating temporary file...")
        try:
            with tempfile.NamedTemporaryFile(mode='w', suffix=extension, delete=False) as temp_file:
                print(f"💾 DEBUG: Writing {len(code)} characters to temp file")
                temp_file.write(code)
                temp_file_path = temp_file.name
                print(f"💾 DEBUG: Temp file written successfully")
                
            print(f"💾 TEMP FILE CREATED: {temp_file_path}")
            
            # Verify file was created and contains data
            if os.path.exists(temp_file_path):
                file_size = os.path.getsize(temp_file_path)
                print(f"💾 DEBUG: Temp file exists, size: {file_size} bytes")
            else:
                print("💾 DEBUG: ERROR - Temp file does not exist!")
                
        except Exception as e:
            print(f"💾 DEBUG: Exception creating temp file: {e}")
            raise
        
        # Compile the file
        print(f"\n🚀 STARTING COMPILATION...")
        print(f"🚀 DEBUG: About to call handle_compilation with: {temp_file_path}, {extension}")
        
        try:
            result = handle_compilation(temp_file_path, extension)
            print(f"🚀 DEBUG: handle_compilation returned: {len(result)} characters")
        except Exception as e:
            print(f"🚀 DEBUG: Exception in handle_compilation: {e}")
            result = f"Compilation failed with exception: {e}"
        
        print(f"\n📤 SENDING RESULT TO CLIENT...")
        print(f"📤 RESULT LENGTH: {len(result)} characters")
        print(f"📤 DEBUG: Result preview: {result[:200]}{'...' if len(result) > 200 else ''}")
        
        # Send result back to client
        response = result + "\nEND_OF_RESULT\n"
        print(f"📤 DEBUG: Full response length: {len(response)} characters")
        
        try:
            client_socket.send(response.encode('utf-8'))
            print(f"📤 DEBUG: Response sent successfully via socket")
        except Exception as e:
            print(f"📤 DEBUG: Exception sending response: {e}")
            raise
        
        print(f"✅ RESPONSE SENT SUCCESSFULLY")
        print("=" * 60)
        
    except Exception as e:
        print(f"💥 DEBUG: Exception caught in main try block: {e}")
        print(f"💥 DEBUG: Exception type: {type(e)}")
        import traceback
        print(f"💥 DEBUG: Full traceback:")
        traceback.print_exc()
        
        error_msg = f"Server error: {str(e)}\nEND_OF_RESULT\n"
        print(f"💥 SERVER ERROR: {e}")
        
        try:
            print(f"📤 DEBUG: Attempting to send error to client: {error_msg}")
            client_socket.send(error_msg.encode('utf-8'))
            print(f"📤 ERROR SENT TO CLIENT")
        except Exception as send_error:
            print(f"❌ DEBUG: Exception sending error to client: {send_error}")
            print(f"❌ FAILED TO SEND ERROR TO CLIENT (client disconnected)")
        
    finally:
        print("🧹 DEBUG: Entering finally block for cleanup")
        
        # Clean up temporary file
        if temp_file_path:
            print(f"🧹 DEBUG: temp_file_path is set to: {temp_file_path}")
            if os.path.exists(temp_file_path):
                try:
                    os.unlink(temp_file_path)
                    print(f"🗑️ CLEANED UP TEMP FILE: {temp_file_path}")
                except Exception as e:
                    print(f"⚠️ WARNING: Could not delete temporary file {temp_file_path}: {e}")
            else:
                print(f"🧹 DEBUG: Temp file does not exist: {temp_file_path}")
        else:
            print("🧹 DEBUG: No temp_file_path to clean up")
        
        # Close client connection
        try:
            print("🧹 DEBUG: Attempting to close client socket")
            client_socket.close()
            print(f"🔌 CLIENT CONNECTION CLOSED")
        except Exception as close_error:
            print(f"⚠️ DEBUG: Exception closing client connection: {close_error}")
            print(f"⚠️ WARNING: Could not close client connection")
        
        print("🧹 DEBUG: Cleanup complete")
        print("=" * 60)

def start_server(host: str = 'localhost', port: int = 8080) -> None:
    """
    Start the TCP server and listen for connections.
    
    Args:
        host: Server host address
        port: Server port number
    """
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    try:
        server_socket.bind((host, port))
        server_socket.listen(5)  # Allow up to 5 pending connections
        
        print(f"Kootopia Compilation Server started on {host}:{port}")
        print("Supported file types:")
        for ext, config in COMPILERS.items():
            print(f"  {ext}: {config['description']}")
        print("\nWaiting for connections...")
        print("Make sure to run: adb reverse tcp:8080 tcp:8080")
        print("Press Ctrl+C to stop the server\n")
        
        while True:
            try:
                print("🔄 DEBUG: Waiting for client connection...")
                client_socket, client_address = server_socket.accept()
                print(f"🔄 DEBUG: Accepted connection from {client_address}")
                
                # Set socket timeout to prevent hanging
                client_socket.settimeout(60.0)  # 60 second timeout
                print("🔄 DEBUG: Set client socket timeout to 60 seconds")
                
                handle_client_connection(client_socket, client_address)
                
            except KeyboardInterrupt:
                print("\nShutting down server...")
                break
            except Exception as e:
                print(f"Error accepting connection: {e}")
                continue
                
    except Exception as e:
        print(f"Failed to start server: {e}")
        sys.exit(1)
        
    finally:
        server_socket.close()
        print("Server stopped.")

def main():
    """Main entry point"""
    print("Kootopia Desktop Compilation Server")
    print("===================================")
    
    try:
        start_server()
    except KeyboardInterrupt:
        print("\nServer interrupted by user")
    except Exception as e:
        print(f"Server error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
