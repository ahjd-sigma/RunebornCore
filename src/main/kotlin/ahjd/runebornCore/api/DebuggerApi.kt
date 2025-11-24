package com.runeborn.api

enum class Level { INFO, WARN, ERROR }

interface DebuggerApi {
    fun enabled(): Boolean
    fun log(level: Level, plugin: String, message: String)
    fun warn(plugin: String, message: String) = log(Level.WARN, plugin, message)
    fun error(plugin: String, message: String) = log(Level.ERROR, plugin, message)
    fun info(plugin: String, message: String) = log(Level.INFO, plugin, message)
    fun error(plugin: String, message: String, throwable: Throwable) {
        error(plugin, message)
        log(Level.ERROR, plugin, throwable.toString())
    }
}