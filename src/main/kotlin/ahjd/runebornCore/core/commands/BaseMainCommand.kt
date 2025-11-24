package com.runeborn.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

abstract class BaseMainCommand : CommandExecutor, TabCompleter {
    protected val subCommands = mutableMapOf<String, SubCommand>()
    fun addSubCommand(sub: SubCommand) {
        subCommands[sub.name.lowercase()] = sub
    }
    fun addSubCommand(vararg subs: SubCommand) {
        subs.forEach { sub ->
            subCommands[sub.name.lowercase()] = sub
        }
    }
    protected open fun sendDefaultHeader(sender: CommandSender) {
        com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header("Available subcommands"))
    }
    protected open fun sendUnknownHeader(sender: CommandSender) {
        com.runeborn.core.utils.Msg.send(sender, "<red>Unknown subcommand.</red> <gray>Use</gray> <yellow>/runeborn help</yellow> <gray>to see options</gray>")
    }
    protected open fun sendNoPermission(sender: CommandSender) {
        com.runeborn.core.utils.Msg.send(sender, "<red>You do not have permission to use this subcommand.</red>")
    }
    protected fun getAvailableSubCommands(sender: CommandSender): List<SubCommand> {
        return subCommands.values.filter { s ->
            val perm = s.permission
            perm == null || sender.hasPermission(perm)
        }
    }
    fun listSubCommands(sender: CommandSender): List<SubCommand> = getAvailableSubCommands(sender)
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sendDefaultHeader(sender)
            getAvailableSubCommands(sender).forEach { s ->
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<aqua>${s.name}</aqua> <gray>–</gray> <white>${s.description}</white>"))
            }
            com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.footer())
            return true
        }
        val sub = subCommands[args[0].lowercase()]
        if (sub == null) {
            sendUnknownHeader(sender)
            getAvailableSubCommands(sender).forEach { s ->
                com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<aqua>${s.name}</aqua> <gray>–</gray> <white>${s.description}</white>"))
            }
            com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.footer())
            return true
        }
        val perm = sub.permission
        if (perm != null && !sender.hasPermission(perm)) {
            sendNoPermission(sender)
            return true
        }
        sub.execute(sender, args.drop(1).toTypedArray())
        return true
    }
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        return try {
            when {
                args.isEmpty() -> getAvailableSubCommands(sender).map { it.name }
                args.size == 1 -> getAvailableSubCommands(sender).map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
                else -> {
                    val sub = subCommands[args[0].lowercase()] ?: return emptyList()
                    val perm = sub.permission
                    if (perm == null || sender.hasPermission(perm)) {
                        sub.tabComplete(sender, args.drop(1).toTypedArray())
                    } else emptyList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
