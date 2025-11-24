package com.runeborn.core.commands.tabcompletion

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

fun interface TabCompleter {
    fun getCompletions(sender: CommandSender, args: Array<String>): List<String>
}

object TabCompleters {
    val ONLINE_PLAYERS = TabCompleter { _, args ->
        Bukkit.getOnlinePlayers().map { it.name }.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
    }
    val BOOLEAN = TabCompleter { _, args ->
        listOf("true", "false").filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
    }
    fun fixed(vararg options: String) = TabCompleter { _, args ->
        options.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }.toList()
    }
    fun fromList(options: List<String>) = TabCompleter { _, args ->
        options.filter { it.startsWith(args.lastOrNull() ?: "", ignoreCase = true) }
    }
}

data class TabCompleteConfig(
    val indexCompletions: Map<Int, TabCompleter> = emptyMap(),
    val pathCompletions: Map<List<String>, TabCompleter> = emptyMap()
) {
    companion object { val EMPTY = TabCompleteConfig() }
    fun getCompletions(sender: CommandSender, args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()
        val argIndex = (args.size - 1).coerceAtLeast(0)
        val path = args.dropLast(1)
        pathCompletions[path]?.let { return it.getCompletions(sender, args) }
        val wildcard = pathCompletions.entries.firstOrNull { (key, _) ->
            key.size == path.size && key.zip(path).all { (k, a) -> k == "*" || k == a }
        }?.value
        if (wildcard != null) return wildcard.getCompletions(sender, args)
        return indexCompletions[argIndex]?.getCompletions(sender, args) ?: emptyList()
    }
}

class TabCompleteBuilder {
    private val indexCompletions = mutableMapOf<Int, TabCompleter>()
    private val pathCompletions = mutableMapOf<List<String>, TabCompleter>()
    fun atIndex(index: Int, completer: TabCompleter) = apply { indexCompletions[index] = completer }
    fun atPath(vararg path: String, completer: TabCompleter) = apply { pathCompletions[path.toList()] = completer }
    fun build() = TabCompleteConfig(indexCompletions, pathCompletions)
}