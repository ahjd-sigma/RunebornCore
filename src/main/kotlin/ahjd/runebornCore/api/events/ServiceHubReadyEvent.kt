package com.runeborn.api.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ServiceHubReadyEvent : Event() {
    override fun getHandlers(): HandlerList = handlerList

    companion object {
        @JvmStatic
        val handlerList = HandlerList()
    }
}
