package com.runeborn.core.config

import com.runeborn.api.ConfigApi
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class ConfigManager(private val plugin: JavaPlugin) : ConfigService, ConfigApi {
    private val cache = ConcurrentHashMap<String, YamlConfiguration>()
    private val root = plugin.dataFolder
    override fun ensureConfig(module: String, file: String, defaults: InputStream?): File {
        val dir = File(root, module)
        if (!dir.exists()) dir.mkdirs()
        val target = File(dir, file)
        if (!target.exists()) {
            if (defaults != null) {
                target.outputStream().use { defaults.copyTo(it) }
            } else {
                target.writeText("configVersion: 1\n")
            }
        }
        val key = "$module/$file"
        val cfg = YamlConfiguration.loadConfiguration(target)
        cache[key] = cfg
        return target
    }
    override fun getConfig(module: String, file: String): YamlConfiguration {
        val key = "$module/$file"
        val cfg = cache[key]
        if (cfg != null) return cfg
        val target = File(File(root, module), file)
        val loaded = YamlConfiguration.loadConfiguration(target)
        cache[key] = loaded
        return loaded
    }
    override fun get(module: String, file: String, path: String): Any? { return getConfig(module, file).get(path) }
    override fun set(module: String, file: String, path: String, value: Any?) {
        val cfg = getConfig(module, file)
        cfg.set(path, value)
        val target = File(File(root, module), file)
        cfg.save(target)
    }
    override fun reload(module: String, file: String) {
        val target = File(File(root, module), file)
        val loaded = YamlConfiguration.loadConfiguration(target)
        cache["$module/$file"] = loaded
    }
    override fun listFiles(module: String): List<File> {
        val dir = File(root, module)
        if (!dir.exists()) return emptyList()
        return dir.listFiles()?.filter { it.isFile && it.extension.lowercase() == "yml" } ?: emptyList()
    }
    override fun ensureWithDefaults(module: String, file: String): File {
        val resourcePath = "defaults/$module/$file"
        val stream = plugin.getResource(resourcePath)
        return ensureConfig(module, file, stream)
    }
    override fun getString(module: String, file: String, path: String): String? = getConfig(module, file).getString(path)
    override fun getInt(module: String, file: String, path: String): Int = getConfig(module, file).getInt(path)
    override fun getBoolean(module: String, file: String, path: String): Boolean = getConfig(module, file).getBoolean(path)
    override fun getDouble(module: String, file: String, path: String): Double = getConfig(module, file).getDouble(path)
    override fun getStringList(module: String, file: String, path: String): List<String> = getConfig(module, file).getStringList(path)
    override fun getSection(module: String, file: String, path: String): ConfigurationSection? = getConfig(module, file).getConfigurationSection(path)
    override fun getSectionMap(module: String, file: String, path: String): Map<String, Any?> {
        val sec = getSection(module, file, path) ?: return emptyMap()
        val result = mutableMapOf<String, Any?>()
        sec.getKeys(false).forEach { key -> result[key] = sec.get(key) }
        return result
    }
    override fun setSectionFromMap(module: String, file: String, path: String, data: Map<String, Any?>) {
        val cfg = getConfig(module, file)
        data.forEach { (k, v) -> cfg.set("$path.$k", v) }
        val target = File(File(root, module), file)
        cfg.save(target)
    }
    override fun getOrDefaultString(module: String, file: String, path: String, default: String): String = getString(module, file, path) ?: default
    override fun getOrDefaultInt(module: String, file: String, path: String, default: Int): Int =
        getInt(module, file, path)
    override fun getOrDefaultBoolean(module: String, file: String, path: String, default: Boolean): Boolean =
        getBoolean(module, file, path)
    override fun getOrDefaultDouble(module: String, file: String, path: String, default: Double): Double =
        getDouble(module, file, path)
    override fun ensureSection(module: String, file: String, path: String): ConfigurationSection {
        val cfg = getConfig(module, file)
        val sec = cfg.getConfigurationSection(path)
        if (sec != null) return sec
        cfg.createSection(path)
        val target = File(File(root, module), file)
        cfg.save(target)
        return cfg.getConfigurationSection(path)!!
    }
    override fun validate(module: String, file: String, schema: Map<String, String>): List<String> {
        val errors = mutableListOf<String>()
        val cfg = getConfig(module, file)
        schema.forEach { (path, type) ->
            val v = cfg.get(path)
            when (type.lowercase()) {
                "string" -> if (v !is String) errors.add("$path: expected string")
                "int" -> if (v !is Int) errors.add("$path: expected int")
                "boolean" -> if (v !is Boolean) errors.add("$path: expected boolean")
                "double" -> if (v !is Double && v !is Float) errors.add("$path: expected double")
                "list" -> if (v !is List<*>) errors.add("$path: expected list")
                else -> if (v == null) errors.add("$path: missing")
            }
        }
        return errors
    }
}
