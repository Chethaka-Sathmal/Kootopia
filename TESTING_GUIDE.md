# 🧪 Kootopia Compilation Testing Guide

## 📋 Pre-Testing Setup

### 1. Desktop Requirements
```bash
# Install required compilers
sudo apt install default-jdk          # For Java (javac)
sudo snap install kotlin --classic     # For Kotlin (kotlinc)
python3 --version                      # Python (usually pre-installed)

# Verify installations
kotlinc -version
javac -version
python3 --version
```

### 2. Start the Server
```bash
cd /path/to/Kootopia
python3 server.py
```
**Expected Output:**
```
Kootopia Compilation Server started on localhost:8080
Supported file types:
  .kt: kotlinc
  .java: javac
  .py: python -m py_compile

Waiting for connections...
Make sure to run: adb reverse tcp:8080 tcp:8080
```

### 3. Setup ADB Connection
```bash
# Enable USB debugging on Android device first
adb devices                    # Verify device is connected
adb reverse tcp:8080 tcp:8080  # Create reverse tunnel
```

## 🧪 Test Cases

### ✅ Test Case 1: Kotlin Compilation (.kt)

**Steps:**
1. Open Kootopia app
2. Ensure extension is `.kt` (default)
3. Enter test code:
```kotlin
fun main() {
    println("Hello from Kotlin!")
    val name = "Kootopia"
    println("Welcome to $name")
}
```
4. Press "Compile" button

**Expected Results:**
- ✅ Compile button should be **enabled** (blue/active)
- ✅ "Compiling..." message appears briefly
- ✅ Result shows "Compilation successful!" in **green text**
- ✅ Server console shows connection and success

### ✅ Test Case 2: Java Compilation (.java)

**Steps:**
1. Open drawer menu (hamburger icon)
2. Select "Change Extension"
3. Choose `.java` from dialog
4. Verify syntax highlighting changes to Java colors
5. Enter test code:
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from Java!");
        String name = "Kootopia";
        System.out.println("Welcome to " + name);
    }
}
```
6. Press "Compile" button

**Expected Results:**
- ✅ Extension dialog shows `.java` option
- ✅ Syntax highlighting updates immediately
- ✅ Compile button remains **enabled**
- ✅ Result shows "Compilation successful!" in **green text**

### ✅ Test Case 3: Python Compilation (.py)

**Steps:**
1. Change extension to `.py` via drawer menu
2. Verify Python syntax highlighting (different keyword colors)
3. Enter test code:
```python
print("Hello from Python!")
name = "Kootopia"
print(f"Welcome to {name}")

def greet(user):
    return f"Hello, {user}!"

print(greet("Developer"))
```
4. Press "Compile" button

**Expected Results:**
- ✅ Python syntax highlighting active
- ✅ Compile button **enabled**
- ✅ Result shows "Compilation successful!" in **green text**

## ❌ Error Testing

### Test Case 4: Kotlin Syntax Error

**Steps:**
1. Set extension to `.kt`
2. Enter invalid code:
```kotlin
fun main() {
    println("Missing closing quote)
    val x = 5
    println(x)
}
```
3. Press "Compile"

**Expected Results:**
- ✅ Result shows "Compilation failed!" in **red text**
- ✅ Error message contains line number
- ✅ **Line 2** highlighted with **red background** in editor
- ✅ Error details shown in compiler interface

### Test Case 5: Java Syntax Error

**Steps:**
1. Set extension to `.java`
2. Enter invalid code:
```java
public class Test {
    public static void main(String[] args) {
        System.out.println("Missing semicolon")
        int x = 5;
        System.out.println(x);
    }
}
```
3. Press "Compile"

**Expected Results:**
- ✅ Result shows "Compilation failed!" in **red text**
- ✅ **Line 3** highlighted with **red background**
- ✅ Java compiler error message displayed

### Test Case 6: Python Syntax Error

**Steps:**
1. Set extension to `.py`
2. Enter invalid code:
```python
print("Missing closing quote)
x = 5
print(x)
```
3. Press "Compile"

**Expected Results:**
- ✅ Result shows "Compilation failed!" in **red text**
- ✅ **Line 1** highlighted with **red background**
- ✅ Python error message with line number

## 🚫 Unsupported Extension Testing

### Test Case 7: Unsupported Extension (.txt)

**Steps:**
1. Create new file with extension `.txt`
2. Enter any text content
3. Check compile button state

**Expected Results:**
- ✅ Compile button should be **disabled** (grayed out)
- ✅ No syntax highlighting (plain text)
- ✅ If somehow compile is triggered, shows "Unsupported file type: .txt"

### Test Case 8: No Extension

**Steps:**
1. Create file named just "test" (no extension)
2. Check behavior

**Expected Results:**
- ✅ Defaults to `.kt` extension
- ✅ Compile button **enabled**
- ✅ Kotlin syntax highlighting active

## 🔄 Dynamic Extension Changes

### Test Case 9: Extension Change Updates

**Steps:**
1. Start with `.kt` extension and Kotlin code
2. Change to `.java` extension
3. Observe immediate changes
4. Change to `.py` extension
5. Change back to `.kt`

**Expected Results:**
- ✅ Syntax highlighting updates **immediately** on each change
- ✅ Compile button state remains **enabled** for all supported types
- ✅ No lag or glitches in UI updates
- ✅ Extension dialog shows current selection highlighted

## 🌐 Network & Connection Testing

### Test Case 10: Server Connection Issues

**Steps:**
1. Stop the Python server
2. Try to compile code
3. Restart server and try again

**Expected Results:**
- ✅ Without server: "Error: Connection refused (Check ADB and server)"
- ✅ After restart: Normal compilation resumes

### Test Case 11: ADB Connection Issues

**Steps:**
1. Run `adb reverse --remove tcp:8080` to break tunnel
2. Try to compile
3. Restore with `adb reverse tcp:8080 tcp:8080`

**Expected Results:**
- ✅ Without tunnel: Connection error message
- ✅ After restore: Normal compilation resumes

## 📱 UI/UX Testing

### Test Case 12: File Operations Integration

**Steps:**
1. Create file "MainActivity.kt" with Kotlin code
2. Save and reopen file
3. Create "HelloWorld.java" with Java code
4. Switch between files

**Expected Results:**
- ✅ Extension auto-detected from filename
- ✅ Syntax highlighting switches automatically
- ✅ Compile button state updates correctly
- ✅ File content preserved correctly

## 🎯 Performance Testing

### Test Case 13: Large File Compilation

**Steps:**
1. Create large Kotlin file (100+ lines)
2. Compile and check performance
3. Try with syntax errors in large file

**Expected Results:**
- ✅ Compilation completes within 10 seconds
- ✅ Error highlighting works on large files
- ✅ UI remains responsive during compilation

## 📊 Success Criteria

**All tests should pass with these criteria:**

### ✅ Core Functionality
- [ ] All supported extensions compile successfully
- [ ] Error messages display correctly with line numbers
- [ ] Error lines highlighted in editor
- [ ] Unsupported extensions handled gracefully

### ✅ UI/UX Requirements  
- [ ] Extension changes update syntax highlighting immediately
- [ ] Compile button enabled/disabled based on extension
- [ ] Success messages in green, errors in red
- [ ] Extension dialog shows current selection

### ✅ Integration Requirements
- [ ] File operations maintain extension state
- [ ] No conflicts with existing features
- [ ] Clean, responsive user interface
- [ ] Proper error handling for network issues

### ✅ Performance Requirements
- [ ] Compilation completes within reasonable time
- [ ] No UI lag during extension changes
- [ ] Proper cleanup of resources
- [ ] Stable operation across multiple compilations

## 🐛 Troubleshooting

### Common Issues:

**"Check ADB and server" error:**
- Verify `adb devices` shows your device
- Restart ADB: `adb kill-server && adb start-server`
- Re-run: `adb reverse tcp:8080 tcp:8080`

**Compile button disabled:**
- Check current extension in title bar
- Try changing extension via drawer menu
- Verify extension is `.kt`, `.java`, or `.py`

**No syntax highlighting:**
- Check if config files exist in assets folder
- Verify extension mapping in `getConfigFileForExtension()`
- Try switching extensions to refresh

**Server not starting:**
- Check if port 8080 is already in use
- Install missing compilers (kotlinc, javac, python3)
- Run with `python3 -v server.py` for verbose output
