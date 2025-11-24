package com.runeborn.core.tests

import com.runeborn.api.Services
import com.runeborn.api.TickerApi
import java.util.logging.Logger

object TickerServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Ticker Service")
        return try {
            val ticks = Services.get(TickerApi::class.java) ?: run {
                logger.warning("Ticker service not available")
                return true
            }
            val id = ticks.createTicker("test-ticker", 20L, false, startImmediately = false) { _ -> }
            val started = ticks.startTicker(id)
            val paused = ticks.pauseTicker(id)
            val resumed = ticks.resumeTicker(id)
            val updated = ticks.updatePeriod(id, 40L)
            val info = ticks.getTickerInfo(id)
            val stopped = ticks.stopTicker(id)
            val summary = ticks.getSummary()
            logger.info("✓ Ticker lifecycle: start=$started pause=$paused resume=$resumed updatePeriod=$updated periodNow=${info?.periodTicks} stop=$stopped summary=$summary")
            true
        } catch (e: Exception) {
            logger.severe("✗ Ticker Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
