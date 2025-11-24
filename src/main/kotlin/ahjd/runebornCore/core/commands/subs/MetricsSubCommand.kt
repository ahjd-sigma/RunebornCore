package com.runeborn.core.commands.subs

import com.runeborn.api.MetricsApi
import com.runeborn.api.Services
import com.runeborn.core.commands.SubCommand
import org.bukkit.command.CommandSender

class MetricsSubCommand : SubCommand {
    override val name: String = "metrics"
    override val description: String = "Show metrics snapshot"
    override fun execute(sender: CommandSender, args: Array<String>) {
        val m = Services.get(MetricsApi::class.java)
        if (m == null) {
            com.runeborn.core.utils.Msg.send(sender, "<red>✖ Metrics not available</red>")
            return
        }
        val s = m.getSnapshot()
        com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Metrics Snapshot"))
        if (s.counters.isEmpty()) com.runeborn.core.utils.Msg.sendRaw(sender, "<gray>( no counters recorded )</gray>") else {
            com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gold>Counters</gold>:"))
            s.counters.entries.take(20).forEach { com.runeborn.core.utils.Msg.sendRaw(sender, "<white>  -</white> <aqua>${it.key}</aqua> <gray>=</gray> <green>${it.value}</green>") }
        }
        if (s.timersNanos.isEmpty()) com.runeborn.core.utils.Msg.sendRaw(sender, "<gray>( no timers recorded )</gray>") else {
            com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<gold>Timers (nanos)</gold>:"))
            s.timersNanos.entries.take(20).forEach { com.runeborn.core.utils.Msg.sendRaw(sender, "<white>  -</white> <aqua>${it.key}</aqua> <gray>=</gray> <green>${it.value}</green>") }
        }
    }
}
