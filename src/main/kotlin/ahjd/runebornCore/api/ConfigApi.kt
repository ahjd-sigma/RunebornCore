package com.runeborn.api

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStream

interface ConfigApi {
    fun ensureConfig(module: String, file: String, defaults: InputStream? = null): File
    fun getConfig(module: String, file: String): YamlConfiguration
    fun get(module: String, file: String, path: String): Any?
    fun set(module: String, file: String, path: String, value: Any?)
    fun reload(module: String, file: String)
    fun listFiles(module: String): List<File>
    fun getString(module: String, file: String, path: String): String?
    fun getInt(module: String, file: String, path: String): Int?
    fun getBoolean(module: String, file: String, path: String): Boolean?
    fun getDouble(module: String, file: String, path: String): Double?
    fun getStringList(module: String, file: String, path: String): List<String>
    fun getSection(module: String, file: String, path: String): ConfigurationSection?
    fun getSectionMap(module: String, file: String, path: String): Map<String, Any?>
    fun setSectionFromMap(module: String, file: String, path: String, data: Map<String, Any?>)
    fun getOrDefaultString(module: String, file: String, path: String, default: String): String
    fun getOrDefaultInt(module: String, file: String, path: String, default: Int): Int
    fun getOrDefaultBoolean(module: String, file: String, path: String, default: Boolean): Boolean
    fun getOrDefaultDouble(module: String, file: String, path: String, default: Double): Double
    fun ensureSection(module: String, file: String, path: String): ConfigurationSection
    fun ensureWithDefaults(module: String, file: String): File
    fun validate(module: String, file: String, schema: Map<String, String>): List<String>
}
