package com.runeborn.core.services

import com.runeborn.api.TestingApi
import com.runeborn.core.tests.*
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger

/**
 * Testing service that manages service verification tests
 * Similar to DebuggerService but for testing functionality
 */
class TestingService(private val plugin: JavaPlugin) : TestingApi {
    private val logger: Logger = plugin.logger
    
    override fun runAllTests(): Boolean {
        logger.info("=== RunebornCore Services Test ===")
        var allPassed = true
        allPassed = runConfigTest() && allPassed
        allPassed = runCommandsTest() && allPassed
        allPassed = runDebuggerTest() && allPassed
        allPassed = runGuiTest() && allPassed
        allPassed = runServiceResolutionTest() && allPassed
        allPassed = runDatabaseTest() && allPassed
        allPassed = runTickerTest() && allPassed
        allPassed = runMetricsTest() && allPassed
        
        
        if (allPassed) {
            logger.info("=== All Tests PASSED ===")
        } else {
            logger.severe("=== Some Tests FAILED ===")
        }
        
        return allPassed
    }
    
    override fun runConfigTest(): Boolean {
        return ConfigServiceTests.run(logger)
    }
    
    override fun runCommandsTest(): Boolean {
        return CommandsServiceTests.run(logger)
    }
    
    override fun runDebuggerTest(): Boolean {
        return DebuggerServiceTests.run(logger)
    }
    
    override fun runGuiTest(): Boolean {
        return GuiServiceTests.run(logger)
    }
    
    override fun runServiceResolutionTest(): Boolean {
        return ServiceResolutionTests.run(logger)
    }

    fun runDatabaseTest(): Boolean = DatabaseServiceTests.run(logger)
    fun runTickerTest(): Boolean = TickerServiceTests.run(logger)
    private fun runMetricsTest(): Boolean = MetricsServiceTests.run(logger)
}
