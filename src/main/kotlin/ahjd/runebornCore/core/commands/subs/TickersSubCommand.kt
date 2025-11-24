package com.runeborn.core.commands.subs

import com.runeborn.api.Services
import com.runeborn.api.TickerApi
import com.runeborn.core.commands.SubCommand
import org.bukkit.command.CommandSender

class TickersSubCommand : SubCommand {
    override val name: String = "tickers"
    override val description: String = "List tickers [all|running|paused|summary]"
    override fun execute(sender: CommandSender, args: Array<String>) {
        val svc = Services.get(TickerApi::class.java)
        if (svc == null) {
            com.runeborn.core.utils.Msg.send(sender, "<red>✖ Ticker service not available</red>")
            return
        }
        when (args.firstOrNull()?.lowercase()) {
            null, "all" -> {
                val list = svc.listTickers()
                if (list.isEmpty()) com.runeborn.core.utils.Msg.send(sender, "<gray>( no tickers registered )</gray>") else {
                    com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Tickers <gray>(all)</gray>"))
                    list.forEach { com.runeborn.core.utils.Msg.sendRaw(sender, "<yellow>•</yellow> <aqua>${it.name}</aqua> <gray>|</gray> <white>id</white>=<gray>${it.id}</gray> <gray>|</gray> <white>period</white>=<gray>${it.periodTicks}</gray> <gray>|</gray> <white>async</white>=<gray>${it.async}</gray> <gray>|</gray> <white>running</white>=<gray>${it.running}</gray> <gray>|</gray> <white>paused</white>=<gray>${it.paused}</gray> <gray>|</gray> <white>exec</white>=<green>${it.executions}</green> <gray>|</gray> <white>last</white>=<green>${it.lastRunNanos}ns</green> <gray>|</gray> <white>total</white>=<green>${it.totalRunNanos}ns</green>") }
                }
            }
            "running" -> {
                val list = svc.listRunningTickers()
                if (list.isEmpty()) com.runeborn.core.utils.Msg.send(sender, "<gray>( no running tickers )</gray>") else {
                    com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Tickers <gray>(running)</gray>"))
                    list.forEach { com.runeborn.core.utils.Msg.sendRaw(sender, "<yellow>•</yellow> <aqua>${it.name}</aqua> <gray>|</gray> <white>id</white>=<gray>${it.id}</gray> <gray>|</gray> <white>period</white>=<gray>${it.periodTicks}</gray> <gray>|</gray> <white>async</white>=<gray>${it.async}</gray> <gray>|</gray> <white>exec</white>=<green>${it.executions}</green> <gray>|</gray> <white>last</white>=<green>${it.lastRunNanos}ns</green> <gray>|</gray> <white>total</white>=<green>${it.totalRunNanos}ns</green>") }
                }
            }
            "paused" -> {
                val list = svc.listPausedTickers()
                if (list.isEmpty()) com.runeborn.core.utils.Msg.send(sender, "<gray>( no paused tickers )</gray>") else {
                    com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Tickers <gray>(paused)</gray>"))
                    list.forEach { com.runeborn.core.utils.Msg.sendRaw(sender, "<yellow>•</yellow> <aqua>${it.name}</aqua> <gray>|</gray> <white>id</white>=<gray>${it.id}</gray> <gray>|</gray> <white>period</white>=<gray>${it.periodTicks}</gray> <gray>|</gray> <white>async</white>=<gray>${it.async}</gray> <gray>|</gray> <white>exec</white>=<green>${it.executions}</green> <gray>|</gray> <white>last</white>=<green>${it.lastRunNanos}ns</green> <gray>|</gray> <white>total</white>=<green>${it.totalRunNanos}ns</green>") }
                }
            }
            "summary" -> {
                val s = svc.getSummary()
                com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Ticker Summary"))
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gray>total:</gray> <white>${s.total}</white>"))
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gray>running:</gray> <green>${s.running}</green>"))
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gray>paused:</gray> <white>${s.paused}</white>"))
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gray>stopped:</gray> <white>${s.stopped}</white>"))
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gray>executions:</gray> <green>${s.totalExecutions}</green>"))
            }
            else -> com.runeborn.core.utils.Msg.sendRaw(sender, "<gray>Usage:</gray> <yellow>/runeborn tickers [all|running|paused|summary]</yellow>")
        }
    }
    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        val opts = listOf("all", "running", "paused", "summary")
        return if (args.size <= 1) opts.filter { it.startsWith(args.firstOrNull() ?: "", true) } else emptyList()
    }
}
