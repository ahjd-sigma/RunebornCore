package com.runeborn.api

import com.runeborn.core.database.DataRepository

inline fun <reified T : Any> DatabaseApi.getRepository(tableName: String): DataRepository<T> = getRepository(tableName, T::class.java)
inline fun <reified T : Any> DatabaseApi.getRepositoryLazy(tableName: String): DataRepository<T> = getRepositoryLazy(tableName, T::class.java)
