package com.runeborn.api

import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

interface CommandsApi {
    fun registerDynamicCommand(namespace: String, name: String, executor: CommandExecutor)
    fun registerMainSubcommand(
        name: String,
        description: String,
        permission: String? = null,
        executor: (CommandSender, Array<String>) -> Unit,
        tabCompleter: ((CommandSender, Array<String>) -> List<String>)? = null
    )
    fun registerMainCommand(
        name: String,
        description: String,
        permission: String? = null
    )
    fun registerSubcommand(
        mainName: String,
        name: String,
        description: String,
        permission: String? = null,
        executor: (CommandSender, Array<String>) -> Unit,
        tabCompleter: ((CommandSender, Array<String>) -> List<String>)? = null
    )
}
