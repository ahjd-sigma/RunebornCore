package com.runeborn.core.tests

import com.runeborn.api.MetricsApi
import com.runeborn.api.Services
import java.util.logging.Logger

object MetricsServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Metrics Service")
        return try {
            val m = Services.get(MetricsApi::class.java) ?: run {
                logger.warning("Metrics not available")
                return true
            }
            m.incrementCounter("test.counter", 2)
            m.recordDuration("test.timer", 12345)
            val s = m.getSnapshot()
            val ok = (s.counters["test.counter"] ?: 0L) >= 2L && (s.timersNanos["test.timer"] ?: 0L) >= 12345L
            logger.info("✓ Metrics snapshot: counters=${s.counters.size} timers=${s.timersNanos.size}")
            ok
        } catch (e: Exception) {
            logger.severe("✗ Metrics Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

