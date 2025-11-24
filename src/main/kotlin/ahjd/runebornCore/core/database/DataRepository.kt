package com.runeborn.core.database

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.sql.ResultSet

class DataRepository<T : Any>(
    private val tableName: String,
    private val clazz: Class<T>,
    private val eagerLoad: Boolean = true
) {
    private val cache = mutableMapOf<String, T>()
    private val gson: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

    init {
        ensureTableExists()
        if (eagerLoad) loadAll()
    }

    fun get(id: String): T? = cache[id]

    fun save(id: String, obj: T) {
        cache[id] = obj
        com.runeborn.api.Services.get(com.runeborn.api.MetricsApi::class.java)?.incrementCounter("db.save")
        saveToDB(id, obj)
    }

    fun delete(id: String) {
        cache.remove(id)
        com.runeborn.api.Services.get(com.runeborn.api.MetricsApi::class.java)?.incrementCounter("db.delete")
        deleteFromDB(id)
    }

    fun getAll(): Map<String, T> = cache.toMap()

    fun exists(id: String): Boolean = cache.containsKey(id)

    fun getAllIds(): Set<String> = cache.keys.toSet()

    fun saveAll() { cache.forEach { (id, obj) -> saveToDB(id, obj) } }

    fun getPage(offset: Int, limit: Int): Map<String, T> {
        val sql = "SELECT id, data FROM $tableName LIMIT ? OFFSET ?"
        val page = mutableMapOf<String, T>()
        DatabaseCore.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setInt(1, limit)
            stmt.setInt(2, offset)
            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val id = rs.getString("id")
                val json = rs.getString("data")
                val obj = gson.fromJson(json, clazz)
                page[id] = obj
            }
        }
        com.runeborn.api.Services.get(com.runeborn.api.MetricsApi::class.java)?.incrementCounter("db.getPage")
        return page
    }

    fun reload() { cache.clear(); loadAll() }

    private fun ensureTableExists() {
        val sql = """
            CREATE TABLE IF NOT EXISTS $tableName (
                id TEXT PRIMARY KEY,
                data TEXT NOT NULL
            )
        """.trimIndent()
        DatabaseCore.getConnection()?.createStatement()?.use { stmt -> stmt.execute(sql) }
    }

    private fun saveToDB(id: String, obj: T) {
        val json = gson.toJson(obj)
        val sql = """
            INSERT OR REPLACE INTO $tableName (id, data)
            VALUES (?, ?)
        """.trimIndent()
        DatabaseCore.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, id)
            stmt.setString(2, json)
            stmt.executeUpdate()
        }
    }

    private fun loadAll() {
        val sql = "SELECT id, data FROM $tableName"
        var cleanedCount = 0
        DatabaseCore.getConnection()?.prepareStatement(sql)?.use { stmt ->
            val rs: ResultSet = stmt.executeQuery()
            while (rs.next()) {
                val id = rs.getString("id")
                val json = rs.getString("data")
                try {
                    val obj = gson.fromJson(json, clazz)
                    cache[id] = obj
                    val cleanedJson = gson.toJson(obj)
                    if (cleanedJson != json) {
                        saveToDB(id, obj)
                        cleanedCount++
                    }
                } catch (_: Exception) { }
            }
        }
        com.runeborn.api.Services.get(com.runeborn.api.MetricsApi::class.java)?.incrementCounter("db.loadAll")
    }

    fun saveAllTransactional() {
        val conn = DatabaseCore.getConnection() ?: return
        val prev = conn.autoCommit
        try {
            conn.autoCommit = false
            cache.forEach { (id, obj) -> saveToDB(id, obj) }
            conn.commit()
        } catch (_: Exception) {
            try { conn.rollback() } catch (_: Exception) {}
        } finally {
            conn.autoCommit = prev
        }
    }

    private fun deleteFromDB(id: String) {
        val sql = "DELETE FROM $tableName WHERE id = ?"
        DatabaseCore.getConnection()?.prepareStatement(sql)?.use { stmt ->
            stmt.setString(1, id)
            stmt.executeUpdate()
        }
    }
}
