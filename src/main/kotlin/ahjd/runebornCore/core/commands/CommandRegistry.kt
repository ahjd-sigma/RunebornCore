package com.runeborn.core.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

object CommandRegistry {
    private lateinit var plugin: JavaPlugin
    private val mains = mutableMapOf<String, BaseMainCommand>()
    private val commands = mutableMapOf<String, Command>()
    fun init(p: JavaPlugin) { plugin = p }
    fun registerExisting(name: String, main: BaseMainCommand) { mains[name.lowercase()] = main }
    fun createMain(name: String, description: String, permission: String?): BaseMainCommand {
        val key = name.lowercase()
        if (mains.containsKey(key)) {
            try { plugin.logger.warning("Main command '$name' already exists; skipping duplicate registration") } catch (_: Exception) {}
            return mains[key]!!
        }
        val main = object : BaseMainCommand() {
            override fun sendDefaultHeader(sender: CommandSender) {
                com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header(description))
            }
        }
        val cmd = object : Command(name) {
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
                return main.onCommand(sender, this, commandLabel, args)
            }
            override fun tabComplete(sender: CommandSender, alias: String, args: Array<String>): List<String> {
                return main.onTabComplete(sender, this, alias, args) ?: emptyList()
            }
        }
        if (permission != null) cmd.permission = permission
        CommandMapHelper.get().register(name, cmd)
        mains[name.lowercase()] = main
        commands[name.lowercase()] = cmd
        main.addSubCommand(object : SubCommand {
            override val name: String = "help"
            override val description: String = "Show subcommands"
            override fun execute(sender: CommandSender, args: Array<String>) {
                com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.header(description))
                val subs = main.listSubCommands(sender)
                subs.forEach { s ->
                    com.runeborn.core.utils.Msg.sendRaw(sender, com.runeborn.core.utils.Msg.bullet("<aqua>${s.name}</aqua> <gray>–</gray> <white>${s.description}</white>"))
                }
                com.runeborn.core.utils.Msg.send(sender, com.runeborn.core.utils.Msg.footer())
            }
        })
        return main
    }
    fun addSub(mainName: String, sub: SubCommand) { mains[mainName.lowercase()]?.addSubCommand(sub) }
    fun unregisterAll() {
        val map = CommandMapHelper.get()
        commands.values.forEach { c ->
            try { c.unregister(map) } catch (_: Exception) {}
        }
        commands.clear()
        mains.clear()
    }
}
