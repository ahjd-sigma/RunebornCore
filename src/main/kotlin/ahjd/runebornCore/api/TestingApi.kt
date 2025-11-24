package com.runeborn.api

/**
 * API for running service tests
 * TODO: add more utility for outside plugins
 */
interface TestingApi {
    fun runAllTests(): Boolean
    fun runConfigTest(): Boolean
    fun runCommandsTest(): Boolean
    fun runDebuggerTest(): Boolean
    fun runGuiTest(): Boolean
    fun runServiceResolutionTest(): Boolean
}
