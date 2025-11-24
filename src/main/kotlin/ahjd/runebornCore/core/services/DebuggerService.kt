package com.runeborn.core.services

import com.runeborn.api.ConfigApi
import com.runeborn.api.DebuggerApi
import com.runeborn.api.Level
import com.runeborn.api.Services
import org.bukkit.plugin.java.JavaPlugin

class DebuggerService(private val plugin: JavaPlugin) : DebuggerApi {
    private fun cfg(): ConfigApi = Services.require(ConfigApi::class.java)
    private val module = "Core"
    private val file = "debugger.yml"
    private val logger = plugin.logger

    init {
        cfg().ensureWithDefaults(module, file)
        // Self-test: log that debugger is initialized
        if (enabled()) {
            info("Debugger", "Service initialized and enabled")
        }
    }

    override fun enabled(): Boolean {
        return cfg().getBoolean(module, file, "enabled") ?: false
    }

    override fun log(level: Level, plugin: String, message: String) {
        if (!enabled()) return
        val prefix = "[$plugin]"
        val msg = "$prefix $message"
        when (level) {
            Level.INFO -> logger.info(msg)
            Level.WARN -> logger.warning(msg)
            Level.ERROR -> logger.severe(msg)
        }
    }
}
