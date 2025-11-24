package com.runeborn.core.services

import com.runeborn.api.TickerApi
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.UUID

class TickerService(private val plugin: JavaPlugin) : TickerApi {
    private data class InternalTicker(
        val id: String,
        val name: String,
        val periodTicks: Long,
        val async: Boolean,
        val runnable: (Long) -> Unit,
        var task: BukkitTask? = null,
        var running: Boolean = false,
        var paused: Boolean = false,
        var executions: Long = 0,
        var lastRunNanos: Long = 0,
        var totalRunNanos: Long = 0
    )

    private val tickers = mutableMapOf<String, InternalTicker>()
    private val oneShots = mutableMapOf<String, BukkitTask>()

    override fun createTicker(name: String, periodTicks: Long, async: Boolean, startImmediately: Boolean, runnable: (Long) -> Unit): String {
        val id = UUID.randomUUID().toString()
        val t = InternalTicker(id, name, periodTicks, async, runnable)
        tickers[id] = t
        if (startImmediately) startTicker(id)
        return id
    }

    override fun startTicker(id: String): Boolean {
        val t = tickers[id] ?: return false
        if (t.running && !t.paused) return true
        val wrap: () -> Unit = {
            val start = System.nanoTime()
            t.executions += 1
            t.runnable(t.executions)
            t.lastRunNanos = System.nanoTime() - start
            t.totalRunNanos += t.lastRunNanos
            com.runeborn.api.Services.get(com.runeborn.api.MetricsApi::class.java)?.apply {
                incrementCounter("ticker.exec")
                recordDuration("ticker.execTime", t.lastRunNanos)
            }
        }
        t.task = if (t.async) Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, wrap, 0L, t.periodTicks)
        else Bukkit.getScheduler().runTaskTimer(plugin, wrap, 0L, t.periodTicks)
        t.running = true
        t.paused = false
        return true
    }

    override fun pauseTicker(id: String): Boolean {
        val t = tickers[id] ?: return false
        if (!t.running || t.paused) return false
        t.task?.cancel()
        t.task = null
        t.paused = true
        t.running = true
        return true
    }

    override fun resumeTicker(id: String): Boolean {
        val t = tickers[id] ?: return false
        if (!t.running || !t.paused) return false
        return startTicker(id)
    }

    override fun stopTicker(id: String): Boolean {
        val t = tickers.remove(id) ?: return false
        t.task?.cancel()
        return true
    }

    override fun getTickerInfo(id: String): TickerApi.TickerInfo? {
        val t = tickers[id] ?: return null
        return TickerApi.TickerInfo(t.id, t.name, t.periodTicks, t.async, t.running && !t.paused, t.paused, t.executions, t.lastRunNanos, t.totalRunNanos)
    }

    override fun listTickers(): List<TickerApi.TickerInfo> {
        return tickers.values.map { TickerApi.TickerInfo(it.id, it.name, it.periodTicks, it.async, it.running && !it.paused, it.paused, it.executions, it.lastRunNanos, it.totalRunNanos) }
    }

    override fun listRunningTickers(): List<TickerApi.TickerInfo> {
        return tickers.values.filter { it.running && !it.paused }.map { TickerApi.TickerInfo(it.id, it.name, it.periodTicks, it.async, true,
            paused = false,
            executions = it.executions,
            lastRunNanos = it.lastRunNanos,
            totalRunNanos = it.totalRunNanos
        ) }
    }

    override fun listPausedTickers(): List<TickerApi.TickerInfo> {
        return tickers.values.filter { it.paused }.map { TickerApi.TickerInfo(it.id, it.name, it.periodTicks, it.async,
            running = false,
            paused = true,
            executions = it.executions,
            lastRunNanos = it.lastRunNanos,
            totalRunNanos = it.totalRunNanos
        ) }
    }

    override fun getSummary(): TickerApi.TickerSummary {
        val total = tickers.size
        val running = tickers.values.count { it.running && !it.paused }
        val paused = tickers.values.count { it.paused }
        val stopped = tickers.values.count { !it.running && !it.paused }
        val totalExecutions = tickers.values.fold(0L) { acc, t -> acc + t.executions }
        return TickerApi.TickerSummary(total, running, paused, stopped, totalExecutions)
    }

    override fun updatePeriod(id: String, newPeriodTicks: Long): Boolean {
        val t = tickers[id] ?: return false
        val updated = t.copy(periodTicks = newPeriodTicks)
        tickers[id] = updated
        if (t.running && !t.paused) {
            pauseTicker(id)
            return startTicker(id)
        }
        return true
    }

    override fun runOnce(delayTicks: Long, async: Boolean, runnable: () -> Unit): String {
        val id = UUID.randomUUID().toString()
        val task = if (async) Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks)
        else Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks)
        oneShots[id] = task
        return id
    }

    override fun cancelAll() {
        tickers.values.forEach { it.task?.cancel(); it.task = null; it.running = false; it.paused = false; it.executions = 0 }
        oneShots.values.forEach { it.cancel() }
        oneShots.clear()
    }
}
