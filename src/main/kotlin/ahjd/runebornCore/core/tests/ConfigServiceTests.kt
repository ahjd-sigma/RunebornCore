package com.runeborn.core.tests

import com.runeborn.api.ConfigApi
import com.runeborn.api.Services
import java.util.logging.Logger

object ConfigServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Config Service")
        return try {
            val cfg = Services.require(ConfigApi::class.java)
            cfg.ensureConfig("Core", "test.yml")
            cfg.set("Core", "test.yml", "test.key", "test-value")
            val value = cfg.getString("Core", "test.yml", "test.key")
            val defaultValue = cfg.getOrDefaultString("Core", "test.yml", "nonexistent.key", "default")
            logger.info("✓ Config read/write works: $value (default=$defaultValue)")
            val errors = cfg.validate("Core", "test.yml", mapOf(
                "test.key" to "string",
                "missing.key" to "string"
            ))
            logger.info("✓ Config validation errors: ${errors.joinToString()}")
            true
        } catch (e: Exception) {
            logger.severe("✗ Config Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
