package com.runeborn.core.services

import com.runeborn.api.DatabaseApi
import com.runeborn.core.database.DatabaseCore
import com.runeborn.core.database.DataRepository
import org.bukkit.plugin.java.JavaPlugin

class DatabaseService(private val plugin: JavaPlugin) : DatabaseApi {
    override fun initialize() { DatabaseCore.initialize(plugin.dataFolder) }
    override fun shutdown() { DatabaseCore.shutdown() }
    override fun <T : Any> getRepository(tableName: String, clazz: Class<T>): DataRepository<T> = DatabaseCore.getRepository(tableName, clazz)
    override fun <T : Any> getRepositoryLazy(tableName: String, clazz: Class<T>): DataRepository<T> = DatabaseCore.getRepositoryLazy(tableName, clazz)
    override fun performMaintenance(vacuum: Boolean, backup: Boolean): Boolean = DatabaseCore.performMaintenance(plugin.dataFolder, vacuum, backup)
}
