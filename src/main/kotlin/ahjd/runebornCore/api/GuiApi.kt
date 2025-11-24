package com.runeborn.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.event.inventory.InventoryClickEvent

interface GuiOpenApi {
    fun openMenu(player: Player, title: String, size: Int, buttons: List<GuiButton>)
    fun close(player: Player)
    fun isGuiOpen(player: Player): Boolean
    fun updateSlotItem(player: Player, slot: Int, item: ItemStack): Boolean
    fun updateSlotHandler(player: Player, slot: Int, onClick: (Player, Int, ClickAction) -> Unit, supportedTypes: Set<ClickType>? = null): Boolean
    fun setButtons(player: Player, buttons: List<GuiButton>): Boolean
    fun openMenuWithOptions(player: Player, title: String, size: Int, buttons: List<GuiButton>, options: GuiOptions)
    fun updateGlobalDelay(player: Player, delayMs: Long?)
    fun updatePerSlotDelay(player: Player, perDelays: Map<Int, Long>?)
}

interface GuiSharedApi {
    fun openSharedMenu(sessionId: String, title: String, size: Int, buttons: List<GuiButton>)
    fun openSharedMenuWithOptions(sessionId: String, title: String, size: Int, buttons: List<GuiButton>, options: GuiOptions)
    fun addViewer(sessionId: String, player: Player): Boolean
    fun removeViewer(sessionId: String, player: Player): Boolean
    fun isSharedMenuOpen(sessionId: String): Boolean
    fun updateSharedSlotItem(sessionId: String, slot: Int, item: ItemStack): Boolean
    fun updateSharedSlotHandler(sessionId: String, slot: Int, onClick: (Player, Int, ClickAction) -> Unit, supportedTypes: Set<ClickType>? = null): Boolean
    fun setSharedButtons(sessionId: String, buttons: List<GuiButton>): Boolean
    fun getSharedMetadata(sessionId: String): SharedSessionMeta?
    fun listSharedSessions(): List<SharedSessionMeta>
    fun setSharedMetadata(sessionId: String, name: String, owner: java.util.UUID?): Boolean
    fun deleteSharedMenu(sessionId: String): Boolean
    fun deleteSharedMenuIfEmpty(sessionId: String): Boolean
}

interface GuiIdApi {
    fun makePublicIdFromTitle(title: String, normalizer: IdNormalizer?): String
    fun makeTradeId(playerA: String, playerB: String): String
}

interface GuiLiveUpdateApi : GuiOpenApi

interface GuiApi : GuiOpenApi, GuiSharedApi, GuiIdApi

data class GuiButton(
    val slot: Int,
    val item: ItemStack,
    val onClick: (Player, Int, ClickAction) -> Unit,
    val supportedTypes: Set<ClickType>? = null
)

data class GuiOptions(
    val listenEmptySlots: Boolean = false,
    val listenPlayerInventory: Boolean = false,
    val showCooldownMessage: Boolean = false,
    val cooldownMessage: String? = null,
    val cooldownSound: org.bukkit.Sound? = null,
    val unassignedClickSound: org.bukkit.Sound? = null,
    val showUnassignedMessage: Boolean = false,
    val unassignedMessage: String? = null,
    val globalDelayMs: Long? = null,
    val perSlotDelayMs: Map<Int, Long>? = null,
    val requiredPermission: String? = null,
    val maxViewers: Int? = null,
    val operatorsBypassLimit: Boolean = true,
    val operatorsBypassPermission: Boolean = true,
    val allowTopInventoryItemMoves: Boolean = false,
    val persistentSession: Boolean = false,
    val onEmptySlot: ((Player, Int, ClickAction) -> Unit)? = null,
    val onPlayerInventoryClick: ((Player, Int, ClickAction) -> Unit)? = null
)

data class ClickAction(
    val slot: Int,
    val type: ClickType,
    val isLeftClick: Boolean,
    val isRightClick: Boolean,
    val isShiftClick: Boolean,
    val isMiddleClick: Boolean,
    val hotbarNumber: Int? = null
)

enum class ClickType {
    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, MIDDLE, NUMBER_KEY, DROP, CTRL_DROP
}

data class SharedSessionMeta(
    val id: String,
    val name: String,
    val createdAt: Long,
    val owner: java.util.UUID?
)

fun interface IdNormalizer {
    fun normalize(title: String): String
}

interface ClickActionMapper {
    fun mapClickType(event: InventoryClickEvent): ClickType
    fun buildAction(event: InventoryClickEvent, type: ClickType): ClickAction
}

interface DelayPolicy {
    fun canClick(controller: com.runeborn.core.utils.ClickDelayController?, slot: Int, isHandledClick: Boolean): Pair<Boolean, Long>
}

interface AccessPolicy {
    fun canView(player: Player, options: GuiOptions?, nonOperatorViewerCount: Int): Boolean
}
