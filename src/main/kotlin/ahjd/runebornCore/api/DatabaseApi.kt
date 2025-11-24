package com.runeborn.api

interface DatabaseApi {
    fun <T : Any> getRepository(tableName: String, clazz: Class<T>): com.runeborn.core.database.DataRepository<T>
    fun <T : Any> getRepositoryLazy(tableName: String, clazz: Class<T>): com.runeborn.core.database.DataRepository<T>
    fun initialize()
    fun shutdown()
    fun performMaintenance(vacuum: Boolean = true, backup: Boolean = true): Boolean
}
