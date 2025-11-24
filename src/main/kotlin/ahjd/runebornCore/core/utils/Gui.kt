package com.runeborn.core.utils

import com.runeborn.api.GuiApi
import com.runeborn.api.GuiButton
import com.runeborn.api.ClickAction
import com.runeborn.api.ClickType
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object Gui {
    
    fun create(size: Int, title: String): GuiBuilder {
        return GuiBuilder(size, title)
    }
}

class GuiBuilder(private val size: Int, private val title: String) {
    private val items = mutableMapOf<Int, GuiItem>()
    
    fun slot(slot: Int, item: ItemStack, sound: Sound? = null, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        items[slot] = GuiItem(item, action, sound, null, null)
        return this
    }
    
    fun slot(slot: Int, material: Material, sound: Sound? = null, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemStack(material), sound, action)
    }
    
    fun slot(slot: Int, item: ItemStack, meta: ItemMetaData, sound: Sound? = null, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemMeta.addMeta(item, meta), sound, action)
    }
    
    fun slot(slot: Int, material: Material, meta: ItemMetaData, sound: Sound? = null, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemMeta.createWithMeta(material, meta), sound, action)
    }

    fun slot(slot: Int, item: ItemStack, clickSounds: Map<ClickType, Sound>, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        items[slot] = GuiItem(item, action, null, clickSounds, null)
        return this
    }

    fun slot(slot: Int, material: Material, clickSounds: Map<ClickType, Sound>, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemStack(material), clickSounds, action)
    }

    fun slot(slot: Int, item: ItemStack, meta: ItemMetaData, clickSounds: Map<ClickType, Sound>, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemMeta.addMeta(item, meta), clickSounds, action)
    }

    fun slot(slot: Int, material: Material, meta: ItemMetaData, clickSounds: Map<ClickType, Sound>, action: ((ClickAction) -> Unit)? = null): GuiBuilder {
        return slot(slot, ItemMeta.createWithMeta(material, meta), clickSounds, action)
    }

    fun slotAdvanced(
        slot: Int,
        item: ItemStack,
        meta: ItemMetaData? = null,
        clickSounds: Map<ClickType, Sound>? = null,
        clickHandlers: Map<ClickType, (ClickAction) -> Unit>? = null,
        sound: Sound? = null,
        action: ((ClickAction) -> Unit)? = null
    ): GuiBuilder {
        val built = if (meta != null) ItemMeta.addMeta(item, meta) else item
        items[slot] = GuiItem(built, action, sound, clickSounds, clickHandlers)
        return this
    }

    fun slotAdvanced(
        slot: Int,
        material: Material,
        meta: ItemMetaData? = null,
        clickSounds: Map<ClickType, Sound>? = null,
        clickHandlers: Map<ClickType, (ClickAction) -> Unit>? = null,
        sound: Sound? = null,
        action: ((ClickAction) -> Unit)? = null
    ): GuiBuilder {
        val item = ItemStack(material)
        return slotAdvanced(slot, item, meta, clickSounds, clickHandlers, sound, action)
    }

    fun slotClick(
        slot: Int,
        material: Material,
        meta: ItemMetaData? = null,
        type: ClickType,
        sound: Sound? = null,
        handler: (ClickAction) -> Unit
    ): GuiBuilder {
        val item = if (meta != null) ItemMeta.createWithMeta(material, meta) else ItemStack(material)
        items[slot] = GuiItem(item, null, sound, null, mapOf(type to handler))
        return this
    }

    fun slotClick(
        slot: Int,
        item: ItemStack,
        type: ClickType,
        sound: Sound? = null,
        handler: (ClickAction) -> Unit
    ): GuiBuilder {
        items[slot] = GuiItem(item, null, sound, null, mapOf(type to handler))
        return this
    }
    
    fun open(
        guiApi: GuiApi,
        player: Player,
        listenEmptySlots: Boolean = false,
        onEmptySlot: ((Player, Int, ClickAction) -> Unit)? = null,
        listenPlayerInventory: Boolean = false,
        onPlayerInventoryClick: ((Player, Int, ClickAction) -> Unit)? = null,
        globalDelayMs: Long? = null,
        perSlotDelayMs: Map<Int, Long>? = null,
        showCooldownMessage: Boolean = false,
        cooldownMessage: String? = null,
        cooldownSound: Sound? = null,
        unassignedClickSound: Sound? = null,
        showUnassignedMessage: Boolean = false,
        unassignedMessage: String? = null
    ) {
        val buttons = items.map { (slot, guiItem) ->
            GuiButton(
                slot = slot,
                item = guiItem.item,
                onClick = { p, slotNum, clickAction ->
                    val s = guiItem.clickSounds?.get(clickAction.type) ?: guiItem.sound
                    if (s != null) p.playSound(p.location, s, 1.0f, 1.0f)
                    val handler = guiItem.clickHandlers?.get(clickAction.type) ?: guiItem.action
                    handler?.invoke(clickAction)
                },
                supportedTypes = guiItem.clickHandlers?.keys?.toSet()
            )
        }
        val opts = com.runeborn.api.GuiOptions(
            listenEmptySlots = listenEmptySlots,
            listenPlayerInventory = listenPlayerInventory,
            showCooldownMessage = showCooldownMessage,
            cooldownMessage = cooldownMessage,
            cooldownSound = cooldownSound,
            unassignedClickSound = unassignedClickSound,
            showUnassignedMessage = showUnassignedMessage,
            unassignedMessage = unassignedMessage,
            globalDelayMs = globalDelayMs,
            perSlotDelayMs = perSlotDelayMs,
            allowTopInventoryItemMoves = false,
            persistentSession = false,
            onEmptySlot = onEmptySlot,
            onPlayerInventoryClick = onPlayerInventoryClick
        )
        guiApi.openMenuWithOptions(player, title, size, buttons, opts)
    }
}

data class GuiItem(
    val item: ItemStack,
    val action: ((ClickAction) -> Unit)?,
    val sound: Sound?,
    val clickSounds: Map<ClickType, Sound>?,
    val clickHandlers: Map<ClickType, (ClickAction) -> Unit>?
)

// ClickAction and ClickType are defined in com.runeborn.api

object ItemMeta {
    
    fun addMeta(item: ItemStack, meta: ItemMetaData): ItemStack {
        val newItem = item.clone()
        val itemMeta = newItem.itemMeta ?: return newItem
        
        meta.name?.let { itemMeta.setDisplayName(it) }
        meta.lore?.let { itemMeta.lore = it }
        
        if (meta.hideFlags) {
            itemMeta.addItemFlags(*ItemFlag.entries.toTypedArray())
        }
        
        if (meta.enchantGlint) {
            itemMeta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true)
        }
        
        newItem.itemMeta = itemMeta
        return newItem
    }
    
    fun createWithMeta(material: Material, meta: ItemMetaData): ItemStack {
        return addMeta(ItemStack(material), meta)
    }
}

data class ItemMetaData(
    val name: String? = null,
    val lore: List<String>? = null,
    val hideFlags: Boolean = true,
    val enchantGlint: Boolean = false
)
