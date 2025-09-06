package com.ncs.kootopia

import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import kotlinx.serialization.json.Json
import java.io.File

@kotlinx.serialization.Serializable
data class SyntaxRules(
    val keywords: List<String>,
    val comments: List<String>,
    val strings: List<String>
)

// Configuration loading result
sealed class ConfigLoadResult {
    data class Success(val syntaxRules: SyntaxRules) : ConfigLoadResult()
    data class Error(val message: String, val fallbackRules: SyntaxRules) : ConfigLoadResult()
}

// Load syntax rules from configs directory with fallback to assets
fun loadSyntaxRulesWithFallback(context: Context, filename: String): ConfigLoadResult {
    return try {
        // First try to load from configs directory
        val configsDir = getConfigsDirectory(context)
        val configFile = File(configsDir, filename)
        
        if (configFile.exists()) {
            val jsonString = configFile.readText()
            val syntaxRules = Json.decodeFromString<SyntaxRules>(jsonString)
            Log.d("SyntaxRules", "Loaded config from: ${configFile.absolutePath}")
            ConfigLoadResult.Success(syntaxRules)
        } else {
            // File doesn't exist in configs, try to load from assets and copy
            loadFromAssetsAndCopy(context, filename)
        }
    } catch (e: Exception) {
        Log.e("SyntaxRules", "Error loading config '$filename': ${e.message}")
        // Load fallback and return error
        val fallbackRules = loadFallbackConfig(context)
        ConfigLoadResult.Error(
            "Error loading configuration '$filename': ${e.message}. Using fallback configuration.",
            fallbackRules
        )
    }
}

// Load from assets and copy to configs directory
private fun loadFromAssetsAndCopy(context: Context, filename: String): ConfigLoadResult {
    return try {
        // Try to load from assets
        val jsonString = context.assets.open(filename).bufferedReader().use { it.readText() }
        val syntaxRules = Json.decodeFromString<SyntaxRules>(jsonString)
        
        // Copy to configs directory for future use
        val configsDir = getConfigsDirectory(context)
        val configFile = File(configsDir, filename)
        configFile.writeText(jsonString)
        
        Log.d("SyntaxRules", "Copied config from assets to: ${configFile.absolutePath}")
        ConfigLoadResult.Success(syntaxRules)
    } catch (e: Exception) {
        Log.e("SyntaxRules", "Error loading from assets '$filename': ${e.message}")
        val fallbackRules = loadFallbackConfig(context)
        ConfigLoadResult.Error(
            "Configuration file '$filename' not found. Using fallback configuration.",
            fallbackRules
        )
    }
}

// Load fallback configuration from assets
private fun loadFallbackConfig(context: Context): SyntaxRules {
    return try {
        // Try to load fallback.json as fallback
        val jsonString = context.assets.open("fallback.json").bufferedReader().use { it.readText() }
        Json.decodeFromString<SyntaxRules>(jsonString)
    } catch (e: Exception) {
        Log.e("SyntaxRules", "Even fallback config failed, using hardcoded fallback")
        // Hardcoded fallback as last resort
        SyntaxRules(
            keywords = emptyList(),
            comments = emptyList(),
            strings = emptyList()
        )
    }
}

// Get configs directory
private fun getConfigsDirectory(context: Context): File {
    val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
    val kootopiaDir = File(documentsDir, "Kootopia")
    val configsDir = File(kootopiaDir, "configs")
    if (!configsDir.exists()) {
        configsDir.mkdirs()
    }
    return configsDir
}

// Backward compatibility function
fun loadSyntaxRules(context: Context, filename: String): SyntaxRules {
    return when (val result = loadSyntaxRulesWithFallback(context, filename)) {
        is ConfigLoadResult.Success -> result.syntaxRules
        is ConfigLoadResult.Error -> {
            // Show error to user (you might want to handle this differently)
            Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            result.fallbackRules
        }
    }
}