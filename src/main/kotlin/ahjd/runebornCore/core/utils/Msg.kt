package com.runeborn.core.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.command.CommandSender

object Msg {
    private val mm = MiniMessage.miniMessage()
    private const val PREFIX_MM = "<#2e2e2e>[</#2e2e2e><bold><gradient:#8B0000:#FF4D4D>RBC</gradient></bold><#2e2e2e>]</#2e2e2e> "

    fun send(sender: CommandSender, messageMm: String) {
        sender.sendMessage(mm.deserialize(PREFIX_MM + messageMm))
    }

    fun sendRaw(sender: CommandSender, messageMm: String) {
        sender.sendMessage(mm.deserialize(messageMm))
    }

    fun sendLines(sender: CommandSender, linesMm: List<String>) {
        linesMm.forEach { send(sender, it) }
    }

    fun header(titleMm: String): String = "<gold><bold>$titleMm</bold></gold>"
    fun bullet(contentMm: String): String = "<yellow>•</yellow> <gray>$contentMm</gray>"
    fun footer(): String = "<gray>──────────────</gray>"
}
