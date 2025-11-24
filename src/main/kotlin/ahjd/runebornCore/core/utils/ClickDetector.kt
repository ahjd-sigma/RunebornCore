package com.runeborn.core.utils

import com.runeborn.api.ClickAction
import com.runeborn.api.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.ClickType as BukkitClickType

object ClickDetector {
    
    fun getClickAction(event: InventoryClickEvent): ClickAction {
        val slot = event.slot
        val clickType = when {
            event.isShiftClick && event.click == BukkitClickType.LEFT -> ClickType.SHIFT_LEFT
            event.isShiftClick && event.click == BukkitClickType.RIGHT -> ClickType.SHIFT_RIGHT
            event.click == BukkitClickType.MIDDLE -> ClickType.MIDDLE
            event.click == BukkitClickType.LEFT -> ClickType.LEFT
            event.click == BukkitClickType.RIGHT -> ClickType.RIGHT
            else -> ClickType.LEFT
        }
        
        return ClickAction(
            slot = slot,
            type = clickType,
            isLeftClick = event.click == BukkitClickType.LEFT || (event.isShiftClick && event.click == BukkitClickType.LEFT),
            isRightClick = event.click == BukkitClickType.RIGHT || (event.isShiftClick && event.click == BukkitClickType.RIGHT),
            isShiftClick = event.isShiftClick,
            isMiddleClick = event.click == BukkitClickType.MIDDLE
        )
    }
}