package com.runeborn.core.commands

import org.bukkit.Bukkit
import org.bukkit.command.CommandMap

object CommandMapHelper {
    fun get(): CommandMap {
        val server = Bukkit.getServer()
        val m = server.javaClass.methods.firstOrNull { it.name == "getCommandMap" && it.parameterCount == 0 }
            ?: throw IllegalStateException("Cannot access CommandMap")
        return m.invoke(server) as CommandMap
    }
}