package com.runeborn.core.commands

import com.runeborn.core.commands.tabcompletion.TabCompleteConfig
import org.bukkit.command.CommandSender

interface SubCommand {
    val name: String
    val description: String
    val permission: String? get() = null
    val tabComplete: TabCompleteConfig get() = TabCompleteConfig.EMPTY
    fun execute(sender: CommandSender, args: Array<String>)
    fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
        return tabComplete.getCompletions(sender, args)
    }
}