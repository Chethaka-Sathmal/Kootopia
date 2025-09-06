# 🚀 Kootopia Quick Start Guide

## ⚡ 5-Minute Setup

### 1. Install Compilers (Desktop)
```bash
# Ubuntu/Debian
sudo apt install default-jdk
sudo snap install kotlin --classic

# Verify
kotlinc -version && javac -version && python3 --version
```

### 2. Start Server
```bash
cd /path/to/Kootopia
python3 server.py
# Keep this running!
```

### 3. Connect Device
```bash
adb reverse tcp:8080 tcp:8080
```

### 4. Test in App
1. Open Kootopia on Android
2. Paste test code:
```kotlin
fun main() {
    println("Hello from Kootopia!")
}
```
3. Press "Compile" → Should see green "Compilation successful!"

## 🧪 Quick Tests

### ✅ Success Test
**Kotlin (.kt):**
```kotlin
fun main() {
    println("Hello!")
}
```

**Java (.java):** (Change extension via drawer menu)
```java
public class Test {
    public static void main(String[] args) {
        System.out.println("Hello!");
    }
}
```

**Python (.py):** (Change extension via drawer menu)
```python
print("Hello!")
```

### ❌ Error Test
**Try invalid syntax:**
```kotlin
fun main() {
    println("missing quote)  // Should highlight this line in red
}
```

### 🚫 Unsupported Test
- Change extension to `.txt`
- Compile button should be **disabled**

## 🔧 Features to Test

- **Extension Changes:** Drawer menu → "Change Extension"
- **Syntax Highlighting:** Updates immediately when extension changes
- **Error Highlighting:** Red background on error lines
- **Smart Button:** Enabled only for .kt, .java, .py files
- **Color Coding:** Green for success, red for errors

## 🐛 Troubleshooting

**"Check ADB and server" error:**
```bash
adb devices  # Should show your device
adb reverse tcp:8080 tcp:8080  # Re-run if needed
```

**Compile button disabled:**
- Check extension in title bar
- Only .kt, .java, .py are supported

**No syntax highlighting:**
- Try changing extension to refresh
- Check if config files exist in app/src/main/assets/

## 📝 Test Checklist

- [ ] Kotlin compilation works (.kt)
- [ ] Java compilation works (.java)  
- [ ] Python compilation works (.py)
- [ ] Error lines highlighted in red
- [ ] Success/error messages color-coded
- [ ] Extension changes update syntax highlighting
- [ ] Unsupported extensions disable compile button
- [ ] File operations preserve extension state

## 🎯 Success Indicators

✅ **Server Console:** Shows connections and compilation results  
✅ **App UI:** Color-coded messages and error highlighting  
✅ **Extension Dialog:** Shows supported options with current selection  
✅ **Syntax Highlighting:** Updates immediately on extension change  
✅ **Smart Compilation:** Button enabled only for supported languages  

**🎉 If all tests pass, your Kootopia compilation system is working perfectly!**
