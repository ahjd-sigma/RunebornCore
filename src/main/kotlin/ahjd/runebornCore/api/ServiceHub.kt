package com.runeborn.api

object ServiceHub {
    @Volatile
    private var initialized = false

    lateinit var config: ConfigApi
    lateinit var commands: CommandsApi
    lateinit var gui: GuiApi
    lateinit var debugger: DebuggerApi
    lateinit var testing: TestingApi
    lateinit var ticker: TickerApi
    lateinit var metrics: MetricsApi
    lateinit var database: DatabaseApi

    fun initialize() {
        if (initialized) return
        config = Services.require(ConfigApi::class.java)
        commands = Services.require(CommandsApi::class.java)
        gui = Services.require(GuiApi::class.java)
        debugger = Services.require(DebuggerApi::class.java)
        testing = Services.require(TestingApi::class.java)
        ticker = Services.require(TickerApi::class.java)
        metrics = Services.require(MetricsApi::class.java)
        database = Services.require(DatabaseApi::class.java)
        initialized = true
    }

    fun isReady(): Boolean = initialized

    fun tryConfig(): ConfigApi? = if (initialized) config else Services.get(ConfigApi::class.java)
    fun tryCommands(): CommandsApi? = if (initialized) commands else Services.get(CommandsApi::class.java)
    fun tryGui(): GuiApi? = if (initialized) gui else Services.get(GuiApi::class.java)
    fun tryDebugger(): DebuggerApi? = if (initialized) debugger else Services.get(DebuggerApi::class.java)
    fun tryTesting(): TestingApi? = if (initialized) testing else Services.get(TestingApi::class.java)
    fun tryTicker(): TickerApi? = if (initialized) ticker else Services.get(TickerApi::class.java)
    fun tryMetrics(): MetricsApi? = if (initialized) metrics else Services.get(MetricsApi::class.java)
    fun tryDatabase(): DatabaseApi? = if (initialized) database else Services.get(DatabaseApi::class.java)
}
