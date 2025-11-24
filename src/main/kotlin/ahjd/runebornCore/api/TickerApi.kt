package com.runeborn.api

interface TickerApi {
    data class TickerInfo(
        val id: String,
        val name: String,
        val periodTicks: Long,
        val async: Boolean,
        val running: Boolean,
        val paused: Boolean,
        val executions: Long,
        val lastRunNanos: Long,
        val totalRunNanos: Long
    )
    data class TickerSummary(
        val total: Int,
        val running: Int,
        val paused: Int,
        val stopped: Int,
        val totalExecutions: Long
    )

    fun createTicker(name: String, periodTicks: Long, async: Boolean, startImmediately: Boolean = true, runnable: (Long) -> Unit): String
    fun startTicker(id: String): Boolean
    fun pauseTicker(id: String): Boolean
    fun resumeTicker(id: String): Boolean
    fun stopTicker(id: String): Boolean
    fun getTickerInfo(id: String): TickerInfo?
    fun listTickers(): List<TickerInfo>
    fun listRunningTickers(): List<TickerInfo>
    fun listPausedTickers(): List<TickerInfo>
    fun getSummary(): TickerSummary
    fun updatePeriod(id: String, newPeriodTicks: Long): Boolean
    fun runOnce(delayTicks: Long, async: Boolean, runnable: () -> Unit): String
    fun cancelAll()
}
