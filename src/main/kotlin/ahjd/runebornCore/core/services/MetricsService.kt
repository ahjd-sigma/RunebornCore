package com.runeborn.core.services

import com.runeborn.api.MetricsApi
import java.util.concurrent.ConcurrentHashMap

class MetricsService : MetricsApi {
    private val counters = ConcurrentHashMap<String, Long>()
    private val timers = ConcurrentHashMap<String, Long>()
    override fun incrementCounter(name: String, delta: Long) { counters.merge(name, delta) { a, b -> a + b } }
    override fun recordDuration(name: String, nanos: Long) { timers.merge(name, nanos) { a, b -> a + b } }
    override fun getSnapshot(): MetricsApi.Snapshot = MetricsApi.Snapshot(counters.toMap(), timers.toMap())
    override fun reset() { counters.clear(); timers.clear() }
}

