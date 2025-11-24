package com.runeborn.core.tests

import com.runeborn.api.ConfigApi
import com.runeborn.api.Services
import java.util.logging.Logger

object ServiceResolutionTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Service Resolution")
        return try {
            val configGet = Services.get(ConfigApi::class.java)
            val okGet = configGet != null
            Services.require(ConfigApi::class.java)
            val okRequire = true
            val throwsMissing = try { Services.require(Runnable::class.java); false } catch (_: IllegalStateException) { true }
            if (okGet && okRequire && throwsMissing) {
                logger.info("✓ Services get/require semantics OK")
                true
            } else {
                logger.severe("✗ Service resolution semantics failed")
                false
            }
        } catch (e: Exception) {
            logger.severe("✗ Service Resolution test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}

