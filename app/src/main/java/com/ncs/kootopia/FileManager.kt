package com.ncs.kootopia

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File

class FileManager(private val context: Context) {

    // Get the Kootopia directory in Documents
    private fun getKootopiaDirectory(): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val kootopiaDir = File(documentsDir, "Kootopia")
        if (!kootopiaDir.exists()) {
            kootopiaDir.mkdirs()
        }
        return kootopiaDir
    }

    // Create a new file (if not exists) and return its name
    fun createNewFile(fileName: String): String {
        val file = File(getKootopiaDirectory(), fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        return file.name
    }

    // Save text content to a file (creates it if missing)
    fun saveFile(fileName: String, content: String) {
        val file = File(getKootopiaDirectory(), fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText(content)
        Log.d("FileManager", "Saved to ${file.absolutePath}")
    }

    // Open a file and return its content (empty if not exists)
    fun openFile(fileName: String): String {
        val file = File(getKootopiaDirectory(), fileName)
        return if (file.exists()) file.readText() else ""
    }

    // Get the Kootopia directory path for external access
    fun getKootopiaDirectoryPath(): String {
        return getKootopiaDirectory().absolutePath
    }

    // Get the configs subdirectory in Kootopia
    private fun getConfigsDirectory(): File {
        val kootopiaDir = getKootopiaDirectory()
        val configsDir = File(kootopiaDir, "configs")
        if (!configsDir.exists()) {
            configsDir.mkdirs()
        }
        return configsDir
    }

    // Save a configuration JSON file
    fun saveConfiguration(fileName: String, jsonContent: String) {
        val file = File(getConfigsDirectory(), fileName)
        file.writeText(jsonContent)
        Log.d("FileManager", "Config saved to ${file.absolutePath}")
    }

    // Load a configuration JSON file
    fun loadConfiguration(fileName: String): String {
        val file = File(getConfigsDirectory(), fileName)
        return if (file.exists()) file.readText() else ""
    }

    // List all configuration files
    fun listConfigurations(): List<String> {
        return getConfigsDirectory().listFiles()?.map { it.name } ?: emptyList()
    }

    // Delete a configuration file
    fun deleteConfiguration(fileName: String): Boolean {
        val file = File(getConfigsDirectory(), fileName)
        return file.delete()
    }

    // Get the configs directory path for external access
    fun getConfigsDirectoryPath(): String {
        return getConfigsDirectory().absolutePath
    }

    // Get save location based on file type
    fun getSaveLocation(fileName: String, isConfigFile: Boolean): String {
        return if (isConfigFile) {
            File(getConfigsDirectory(), fileName).absolutePath
        } else {
            File(getKootopiaDirectory(), fileName).absolutePath
        }
    }

}