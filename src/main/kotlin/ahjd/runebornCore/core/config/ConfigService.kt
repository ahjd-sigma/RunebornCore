package com.runeborn.core.config

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStream

interface ConfigService {
    fun ensureConfig(module: String, file: String, defaults: InputStream?): File
    fun getConfig(module: String, file: String): YamlConfiguration
    fun get(module: String, file: String, path: String): Any?
    fun set(module: String, file: String, path: String, value: Any?)
    fun reload(module: String, file: String)
    fun listFiles(module: String): List<File>
}