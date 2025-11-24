package com.runeborn.core.database

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

object DatabaseCore {
    private var connection: Connection? = null
    private val repositories = mutableMapOf<String, DataRepository<*>>()

    fun initialize(dataFolder: File) {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        val dbFile = File(dataFolder, "database.db")
        connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
        connection?.createStatement()?.use { stmt ->
            stmt.execute("PRAGMA foreign_keys = ON")
            stmt.execute("PRAGMA journal_mode = WAL")
        }
    }

    fun shutdown() {
        repositories.values.forEach { it.saveAll() }
        connection?.close()
    }
    fun performMaintenance(dataFolder: File, vacuum: Boolean, backup: Boolean): Boolean {
        return try {
            if (vacuum) connection?.createStatement()?.use { it.execute("VACUUM") }
            if (backup) {
                val dbFile = File(dataFolder, "database.db")
                if (dbFile.exists()) {
                    val backupFile = File(dataFolder, "backup_${System.currentTimeMillis()}.db")
                    dbFile.copyTo(backupFile, overwrite = true)
                }
            }
            true
        } catch (_: Exception) { false }
    }

    fun getConnection(): Connection? = connection

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getRepository(tableName: String, clazz: Class<T>): DataRepository<T> {
        return repositories.getOrPut(tableName) { DataRepository(tableName, clazz, eagerLoad = true) } as DataRepository<T>
    }
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getRepositoryLazy(tableName: String, clazz: Class<T>): DataRepository<T> {
        return repositories.getOrPut(tableName) { DataRepository(tableName, clazz, eagerLoad = false) } as DataRepository<T>
    }
}
