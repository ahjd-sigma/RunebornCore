package com.runeborn.core.tests

import com.runeborn.api.DebuggerApi
import com.runeborn.api.Services
import java.util.logging.Logger

object DebuggerServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Debugger Service")
        return try {
            val dbg = Services.get(DebuggerApi::class.java) ?: run {
                logger.warning("Debugger service not available")
                return true
            }
            dbg.info("Tests", "Info message test")
            dbg.warn("Tests", "Warning message test")
            dbg.error("Tests", "Error message test")
            try { throw RuntimeException("Test exception") } catch (e: Exception) { dbg.error("Tests", "Exception test", e) }
            logger.info("✓ Debugger logging works")
            true
        } catch (e: Exception) {
            logger.severe("✗ Debugger Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

