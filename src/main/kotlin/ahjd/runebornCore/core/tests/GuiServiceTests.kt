package com.runeborn.core.tests

import com.runeborn.api.GuiApi
import com.runeborn.api.Services
import java.util.logging.Logger

object GuiServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] GUI Service")
        return try {
            Services.get(GuiApi::class.java) ?: run {
                logger.warning("GUI service not available")
                return true
            }
            logger.info("✓ GUI service resolution works")
            true
        } catch (e: Exception) {
            logger.severe("✗ GUI Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

