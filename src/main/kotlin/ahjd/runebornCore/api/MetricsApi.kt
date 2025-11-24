package com.runeborn.api

interface MetricsApi {
    data class Snapshot(val counters: Map<String, Long>, val timersNanos: Map<String, Long>)
    fun incrementCounter(name: String, delta: Long = 1)
    fun recordDuration(name: String, nanos: Long)
    fun getSnapshot(): Snapshot
    fun reset()
}

