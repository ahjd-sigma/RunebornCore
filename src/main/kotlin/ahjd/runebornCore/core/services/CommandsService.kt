package com.runeborn.core.services

import com.runeborn.api.CommandsApi
import com.runeborn.core.commands.CommandMapHelper
import com.runeborn.core.commands.CommandRegistry
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class CommandsService() : CommandsApi {
    override fun registerDynamicCommand(namespace: String, name: String, executor: CommandExecutor) {
        val commandMap = CommandMapHelper.get()
        val dynamicCommand = object : Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return executor.onCommand(sender, this, commandLabel, args)
            }
            override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
                return if (executor is TabCompleter) {
                    executor.onTabComplete(sender, this, alias, args) ?: emptyList()
                } else {
                    super.tabComplete(sender, alias, args)
                }
            }
        }
        commandMap.register(namespace, dynamicCommand)
    }
    override fun registerMainSubcommand(
        name: String,
        description: String,
        permission: String?,
        executor: (CommandSender, Array<String>) -> Unit,
        tabCompleter: ((CommandSender, Array<String>) -> List<String>)?
    ) {
        val sub = object : com.runeborn.core.commands.SubCommand {
            override val name: String = name
            override val description: String = description
            override val permission: String? = permission
            override fun execute(sender: CommandSender, args: Array<String>) { executor(sender, args) }
            override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
                return tabCompleter?.invoke(sender, args) ?: emptyList()
            }
        }
        CommandRegistry.addSub("runeborn", sub)
    }
    override fun registerMainCommand(name: String, description: String, permission: String?) {
        CommandRegistry.createMain(name, description, permission)
    }
    override fun registerSubcommand(
        mainName: String,
        name: String,
        description: String,
        permission: String?,
        executor: (CommandSender, Array<String>) -> Unit,
        tabCompleter: ((CommandSender, Array<String>) -> List<String>)?
    ) {
        val sub = object : com.runeborn.core.commands.SubCommand {
            override val name: String = name
            override val description: String = description
            override val permission: String? = permission
            override fun execute(sender: CommandSender, args: Array<String>) { executor(sender, args) }
            override fun tabComplete(sender: CommandSender, args: Array<String>): List<String> {
                return tabCompleter?.invoke(sender, args) ?: emptyList()
            }
        }
        CommandRegistry.addSub(mainName, sub)
    }
}