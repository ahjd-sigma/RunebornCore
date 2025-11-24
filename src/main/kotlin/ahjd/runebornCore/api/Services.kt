package com.runeborn.api

import org.bukkit.Bukkit

object Services {
    fun <T : Any> get(clazz: Class<T>): T? {
        val reg = Bukkit.getServer().servicesManager.getRegistration(clazz) ?: return null
        return reg.provider
    }
    fun <T : Any> require(clazz: Class<T>): T {
        val reg = Bukkit.getServer().servicesManager.getRegistration(clazz)
            ?: throw IllegalStateException("Missing required service: ${clazz.name}")
        return reg.provider
    }
}