package com.runeborn.core.services.internal

import com.runeborn.api.ClickAction
import com.runeborn.api.ClickActionMapper
import com.runeborn.api.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.ClickType as BukkitClickType

class DefaultClickActionMapper : ClickActionMapper {
    override fun mapClickType(event: InventoryClickEvent): ClickType = when (event.click) {
        BukkitClickType.SHIFT_LEFT -> ClickType.SHIFT_LEFT
        BukkitClickType.SHIFT_RIGHT -> ClickType.SHIFT_RIGHT
        BukkitClickType.MIDDLE -> ClickType.MIDDLE
        BukkitClickType.LEFT -> ClickType.LEFT
        BukkitClickType.RIGHT -> ClickType.RIGHT
        BukkitClickType.NUMBER_KEY -> ClickType.NUMBER_KEY
        BukkitClickType.DROP -> ClickType.DROP
        BukkitClickType.CONTROL_DROP -> ClickType.CTRL_DROP
        else -> ClickType.LEFT
    }

    override fun buildAction(event: InventoryClickEvent, type: ClickType): ClickAction = ClickAction(
        slot = event.slot,
        type = type,
        isLeftClick = event.click == BukkitClickType.LEFT || event.click == BukkitClickType.SHIFT_LEFT,
        isRightClick = event.click == BukkitClickType.RIGHT || event.click == BukkitClickType.SHIFT_RIGHT,
        isShiftClick = event.click == BukkitClickType.SHIFT_LEFT || event.click == BukkitClickType.SHIFT_RIGHT,
        isMiddleClick = event.click == BukkitClickType.MIDDLE,
        hotbarNumber = if (event.click == BukkitClickType.NUMBER_KEY && event.hotbarButton >= 0) event.hotbarButton + 1 else null
    )
}

