# Kootopia Desktop Compilation Server

This Python server enables remote compilation for the Kootopia Android text editor app via ADB reverse tunneling.

## Setup Instructions

### 1. Prerequisites
Make sure you have the required compilers installed:
- **Kotlin**: `kotlinc` (install Kotlin compiler)
- **Java**: `javac` (install JDK)
- **Python**: `python` (usually pre-installed)

### 2. Connect Android Device
1. Enable USB debugging on your Android device
2. Connect device via USB cable
3. Accept any debugging permission prompts

### 3. Set up ADB Reverse Tunnel
```bash
adb reverse tcp:8080 tcp:8080
```
This creates a tunnel allowing the Android app to connect to `localhost:8080` on your desktop.

### 4. Start the Server
```bash
python3 server.py
```

You should see output like:
```
Kootopia Desktop Compilation Server
===================================
Kootopia Compilation Server started on localhost:8080
Supported file types:
  .kt: kotlinc
  .java: javac
  .py: python -m py_compile

Waiting for connections...
Make sure to run: adb reverse tcp:8080 tcp:8080
Press Ctrl+C to stop the server
```

## Usage

1. Open the Kootopia app on your Android device
2. Write some code (Kotlin, Java, or Python)
3. Press the "Compile" button
4. The server will:
   - Receive your code and filename
   - Save it to a temporary file
   - Run the appropriate compiler
   - Send results back to the app
   - Clean up temporary files

## Supported File Types

| Extension | Compiler Command | Notes |
|-----------|------------------|-------|
| `.kt` | `kotlinc filename.kt` | Kotlin compiler |
| `.java` | `javac filename.java` | Java compiler |
| `.py` | `python -m py_compile filename.py` | Python syntax checker |

## Adding New Compilers

To support additional file types, edit the `COMPILERS` dictionary in `server.py`:

```python
COMPILERS = {
    '.kt': ['kotlinc'],
    '.java': ['javac'],
    '.py': ['python', '-m', 'py_compile'],
    '.cpp': ['g++', '-c'],  # Add C++ support
    '.rs': ['rustc', '--crate-type', 'bin'],  # Add Rust support
}
```

## Troubleshooting

### "Compiler not found" errors
- Install the missing compiler for your file type
- Make sure it's in your system PATH
- Test by running the compiler command manually

### Connection issues
- Verify ADB is working: `adb devices`
- Re-run the reverse tunnel: `adb reverse tcp:8080 tcp:8080`
- Check that no other process is using port 8080
- Restart the server

### Android app shows "Check ADB and server"
- Make sure the server is running
- Verify the ADB reverse tunnel is active
- Check your USB connection

## Example Session

```
$ python3 server.py
Kootopia Compilation Server started on localhost:8080
Waiting for connections...

Connection from ('127.0.0.1', 45678)
Received filename: test.kt
Received code (85 characters)
Created temporary file: /tmp/tmpxyz123.kt
Compilation result: Compilation successful!
Cleaned up temporary file: /tmp/tmpxyz123.kt
```

## Security Notes

- The server only listens on `localhost` (127.0.0.1)
- Temporary files are automatically cleaned up
- Connection is only possible via ADB tunnel (device must be physically connected)
- 30-second timeout prevents hanging processes
