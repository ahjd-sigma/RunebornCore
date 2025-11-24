package com.runeborn.core.services

import com.runeborn.api.ClickAction
import com.runeborn.api.ClickType
import com.runeborn.api.GuiApi
import com.runeborn.api.GuiButton
import com.runeborn.core.services.internal.DefaultAccessPolicy
import com.runeborn.core.services.internal.DefaultClickActionMapper
import com.runeborn.core.services.internal.DefaultDelayPolicy
import com.runeborn.core.utils.ClickDelayController
import org.bukkit.Bukkit
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.plugin.java.JavaPlugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GuiService(private val plugin: JavaPlugin) : GuiApi, Listener {
    private val clickMapper = DefaultClickActionMapper()
    private val delayPolicy = DefaultDelayPolicy()
    private val accessPolicy = DefaultAccessPolicy()
    private data class HandlerEntry(
        val fn: (Player, Int, ClickAction) -> Unit,
        val supportedTypes: Set<ClickType>?
    )
    private val handlers = mutableMapOf<UUID, MutableMap<Int, HandlerEntry>>()
    private val options = mutableMapOf<UUID, com.runeborn.api.GuiOptions>()
    private val delayControllers = ConcurrentHashMap<UUID, ClickDelayController>()
    private val inventories = mutableMapOf<UUID, Inventory>()
    private val sharedHandlers = mutableMapOf<String, MutableMap<Int, HandlerEntry>>()
    private val sharedOptions = mutableMapOf<String, com.runeborn.api.GuiOptions>()
    private val viewerNonOpCount = mutableMapOf<String, Int>()
    private val sharedDelayControllers = mutableMapOf<String, ClickDelayController>()
    private val sharedInventories = mutableMapOf<String, Inventory>()
    private val inventoryToSession = mutableMapOf<Inventory, String>()
    private val viewers = mutableMapOf<String, MutableSet<UUID>>()
    private val sharedMetadata = mutableMapOf<String, com.runeborn.api.SharedSessionMeta>()
    private class Holder : InventoryHolder { override fun getInventory(): Inventory { throw IllegalStateException() } }
    override fun openMenu(player: Player, title: String, size: Int, buttons: List<GuiButton>) {
        val inv = Bukkit.createInventory(Holder(), size, Component.text(title))
        val map = mutableMapOf<Int, HandlerEntry>()
        buttons.forEach {
            inv.setItem(it.slot, it.item)
            map[it.slot] = HandlerEntry(it.onClick, it.supportedTypes)
        }
        handlers[player.uniqueId] = map
        inventories[player.uniqueId] = inv
        player.openInventory(inv)
    }
    override fun close(player: Player) {
        handlers.remove(player.uniqueId)
        options.remove(player.uniqueId)
        delayControllers.remove(player.uniqueId)
        inventories.remove(player.uniqueId)
        player.closeInventory()
    }
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val p = e.whoClicked as? Player ?: return
        val clickType = clickMapper.mapClickType(e)
        val action = clickMapper.buildAction(e, clickType)

        val isTopInv = e.clickedInventory?.holder is Holder
        val sessionId = if (isTopInv) inventoryToSession[e.clickedInventory!!] else null
        val (map, opts, controller) = if (sessionId != null) {
            Triple(sharedHandlers[sessionId] ?: mutableMapOf(), sharedOptions[sessionId], sharedDelayControllers[sessionId])
        } else {
            Triple(handlers[p.uniqueId] ?: mutableMapOf(), options[p.uniqueId], delayControllers[p.uniqueId])
        }
        val entry = map[e.slot]
        val isHandledClick = when {
            entry == null -> false
            entry.supportedTypes == null -> true
            else -> entry.supportedTypes.contains(clickType)
        }
        if (isTopInv && controller != null && isHandledClick) {
            val (allowed, remainingMs) = delayPolicy.canClick(controller, e.slot, true)
            if (!allowed) {
                e.isCancelled = true
                val msg = opts?.cooldownMessage ?: "§cPlease wait ${remainingMs}ms"
                if (opts?.showCooldownMessage == true) p.sendMessage(msg)
                opts?.cooldownSound?.let { p.playSound(p.location, it, 1.0f, 1.0f) }
                return
            }
        }

        if (isTopInv) {
            if (entry != null && isHandledClick) {
                e.isCancelled = true
                entry.fn(p, e.slot, action)
                return
            }
            // Unassigned click on a configured slot or any empty slot
            if (opts != null) {
                opts.unassignedClickSound?.let { p.playSound(p.location, it, 1.0f, 1.0f) }
                if (opts.showUnassignedMessage && opts.unassignedMessage != null) {
                    p.sendMessage(opts.unassignedMessage)
                }
            }
            if (opts?.listenEmptySlots == true && opts.onEmptySlot != null) {
                e.isCancelled = true
                opts.onEmptySlot.invoke(p, e.slot, action)
                return
            }
            if (opts?.allowTopInventoryItemMoves == true) {
                e.isCancelled = false
                return
            }
            e.isCancelled = true
            return
        } else {
            if (opts?.listenPlayerInventory == true && opts.onPlayerInventoryClick != null) {
                e.isCancelled = true
                opts.onPlayerInventoryClick.invoke(p, e.slot, action)
                return
            }
            // allow normal player inventory interactions
        }
    }
    fun register() { plugin.server.pluginManager.registerEvents(this, plugin) }
    @EventHandler
    fun onInvClose(e: InventoryCloseEvent) {
        val p = e.player as? Player ?: return
        if (e.inventory.holder is Holder) {
            handlers.remove(p.uniqueId)
            options.remove(p.uniqueId)
            delayControllers.remove(p.uniqueId)
            inventories.remove(p.uniqueId)
            val sid = inventoryToSession.remove(e.inventory)
            if (sid != null) {
                viewers[sid]?.remove(p.uniqueId)
                val persistent = sharedOptions[sid]?.persistentSession == true
                if (!persistent && viewers[sid]?.isEmpty() == true) {
                    sharedHandlers.remove(sid)
                    sharedOptions.remove(sid)
                    sharedDelayControllers.remove(sid)
                    sharedInventories.remove(sid)
                    viewers.remove(sid)
                }
            }
        }
    }
    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val id = e.player.uniqueId
        handlers.remove(id)
        options.remove(id)
        delayControllers.remove(id)
        inventories.remove(id)
        viewers.values.forEach { it.remove(id) }
    }

    override fun openMenuWithOptions(player: Player, title: String, size: Int, buttons: List<GuiButton>, options: com.runeborn.api.GuiOptions) {
        this.options[player.uniqueId] = options
        val gd = options.globalDelayMs
        val ps = options.perSlotDelayMs?.toMutableMap() ?: mutableMapOf()
        if (gd != null || ps.isNotEmpty()) {
            delayControllers[player.uniqueId] = ClickDelayController(gd, ps)
        }
        openMenu(player, title, size, buttons)
    }

    

    override fun isGuiOpen(player: Player): Boolean {
        return inventories.containsKey(player.uniqueId)
    }
    override fun updateSlotItem(player: Player, slot: Int, item: org.bukkit.inventory.ItemStack): Boolean {
        val inv = inventories[player.uniqueId] ?: return false
        inv.setItem(slot, item)
        return true
    }
    override fun updateSlotHandler(player: Player, slot: Int, onClick: (Player, Int, ClickAction) -> Unit, supportedTypes: Set<ClickType>?): Boolean {
        val map = handlers[player.uniqueId] ?: return false
        map[slot] = HandlerEntry(onClick, supportedTypes)
        return true
    }
    override fun setButtons(player: Player, buttons: List<GuiButton>): Boolean {
        val inv = inventories[player.uniqueId] ?: return false
        val map = handlers[player.uniqueId] ?: return false
        buttons.forEach {
            inv.setItem(it.slot, it.item)
            map[it.slot] = HandlerEntry(it.onClick, it.supportedTypes)
        }
        return true
    }

    override fun updateGlobalDelay(player: Player, delayMs: Long?) {
        val existing = options[player.uniqueId] ?: return
        val updated = existing.copy(globalDelayMs = delayMs)
        options[player.uniqueId] = updated
        delayControllers[player.uniqueId] =
            ClickDelayController(updated.globalDelayMs, updated.perSlotDelayMs?.toMutableMap() ?: mutableMapOf())
    }
    override fun updatePerSlotDelay(player: Player, perDelays: Map<Int, Long>?) {
        val existing = options[player.uniqueId] ?: return
        val updated = existing.copy(perSlotDelayMs = perDelays)
        options[player.uniqueId] = updated
        delayControllers[player.uniqueId] =
            ClickDelayController(updated.globalDelayMs, updated.perSlotDelayMs?.toMutableMap() ?: mutableMapOf())
    }
    override fun openSharedMenu(sessionId: String, title: String, size: Int, buttons: List<GuiButton>) {
        val inv = Bukkit.createInventory(Holder(), size, Component.text(title))
        val map = mutableMapOf<Int, HandlerEntry>()
        buttons.forEach {
            inv.setItem(it.slot, it.item)
            map[it.slot] = HandlerEntry(it.onClick, it.supportedTypes)
        }
        sharedHandlers[sessionId] = map
        sharedInventories[sessionId] = inv
        inventoryToSession[inv] = sessionId
        viewers[sessionId] = mutableSetOf()
        sharedMetadata[sessionId] = com.runeborn.api.SharedSessionMeta(sessionId, title, System.currentTimeMillis(), null)
    }
    override fun openSharedMenuWithOptions(sessionId: String, title: String, size: Int, buttons: List<GuiButton>, options: com.runeborn.api.GuiOptions) {
        sharedOptions[sessionId] = options
        val gd = options.globalDelayMs
        val ps = options.perSlotDelayMs?.toMutableMap() ?: mutableMapOf()
        if (gd != null || ps.isNotEmpty()) {
            sharedDelayControllers[sessionId] = ClickDelayController(gd, ps)
        }
        openSharedMenu(sessionId, title, size, buttons)
    }
    override fun addViewer(sessionId: String, player: Player): Boolean {
        val inv = sharedInventories[sessionId] ?: return false
        val opts = sharedOptions[sessionId]
        // Permission check (no operator bypass unless permission grants it)
        if (opts?.requiredPermission != null) {
            val hasPerm = player.hasPermission(opts.requiredPermission) || (opts.operatorsBypassPermission && player.isOp)
            if (!hasPerm) {
                return false
            }
        }
        // Viewer limit: operators may bypass if configured, and do not count towards the limit
        val existing = viewers[sessionId]
        val currentViewers = if (existing != null) existing else {
            val s = HashSet<UUID>()
            viewers[sessionId] = s
            viewerNonOpCount[sessionId] = 0
            s
        }
        val nonOpCount = viewerNonOpCount[sessionId] ?: 0
        if (!accessPolicy.canView(player, opts, nonOpCount)) return false
        currentViewers.add(player.uniqueId)
        val countsAs = !(player.isOp && (opts?.operatorsBypassLimit == true))
        if (countsAs) viewerNonOpCount[sessionId] = (viewerNonOpCount[sessionId] ?: 0) + 1
        player.openInventory(inv)
        return true
    }
    override fun removeViewer(sessionId: String, player: Player): Boolean {
        val v = viewers[sessionId]
        val removed = v?.remove(player.uniqueId) ?: false
        if (removed) {
            val opts = sharedOptions[sessionId]
            val countsAs = !(player.isOp && (opts?.operatorsBypassLimit == true))
            if (countsAs) viewerNonOpCount[sessionId] = (viewerNonOpCount[sessionId] ?: 1) - 1
        }
        val persistent = sharedOptions[sessionId]?.persistentSession == true
        if (!persistent && (viewers[sessionId]?.isEmpty() == true)) {
            sharedHandlers.remove(sessionId)
            sharedOptions.remove(sessionId)
            sharedDelayControllers.remove(sessionId)
            sharedInventories.remove(sessionId)
            viewers.remove(sessionId)
            viewerNonOpCount.remove(sessionId)
        }
        return true
    }
    override fun isSharedMenuOpen(sessionId: String): Boolean {
        return sharedInventories.containsKey(sessionId)
    }
    override fun updateSharedSlotItem(sessionId: String, slot: Int, item: org.bukkit.inventory.ItemStack): Boolean {
        val inv = sharedInventories[sessionId] ?: return false
        inv.setItem(slot, item)
        return true
    }
    override fun updateSharedSlotHandler(sessionId: String, slot: Int, onClick: (Player, Int, ClickAction) -> Unit, supportedTypes: Set<ClickType>?): Boolean {
        val map = sharedHandlers[sessionId] ?: return false
        map[slot] = HandlerEntry(onClick, supportedTypes)
        return true
    }
    override fun setSharedButtons(sessionId: String, buttons: List<GuiButton>): Boolean {
        val inv = sharedInventories[sessionId] ?: return false
        val map = sharedHandlers[sessionId] ?: return false
        buttons.forEach {
            inv.setItem(it.slot, it.item)
            map[it.slot] = HandlerEntry(it.onClick, it.supportedTypes)
        }
        return true
    }

    override fun getSharedMetadata(sessionId: String): com.runeborn.api.SharedSessionMeta? {
        return sharedMetadata[sessionId]
    }
    override fun listSharedSessions(): List<com.runeborn.api.SharedSessionMeta> {
        return sharedMetadata.values.toList()
    }
    override fun setSharedMetadata(sessionId: String, name: String, owner: UUID?): Boolean {
        val meta = sharedMetadata[sessionId] ?: return false
        sharedMetadata[sessionId] = meta.copy(name = name, owner = owner)
        // also update inventory title if desired? keep as metadata only to avoid re-open flicker
        return true
    }
    override fun deleteSharedMenu(sessionId: String): Boolean {
        val inv = sharedInventories.remove(sessionId) ?: return false
        val v = viewers.remove(sessionId) ?: mutableSetOf()
        v.forEach { uid ->
            val pl = plugin.server.getPlayer(uid)
            if (pl != null && pl.openInventory.topInventory == inv) pl.closeInventory()
        }
        sharedHandlers.remove(sessionId)
        sharedOptions.remove(sessionId)
        sharedDelayControllers.remove(sessionId)
        sharedMetadata.remove(sessionId)
        inventoryToSession.remove(inv)
        return true
    }
    override fun deleteSharedMenuIfEmpty(sessionId: String): Boolean {
        val v = viewers[sessionId]
        if (v == null || v.isEmpty()) return deleteSharedMenu(sessionId)
        return false
    }
    override fun makePublicIdFromTitle(title: String, normalizer: com.runeborn.api.IdNormalizer?): String {
        val base = (normalizer?.normalize(title) ?: defaultNormalize(title))
        if (!sharedInventories.containsKey(base)) return base
        var i = 2
        while (sharedInventories.containsKey("$base-$i")) i++
        return "$base-$i"
    }
    override fun makeTradeId(playerA: String, playerB: String): String {
        val a = playerA.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val b = playerB.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val (p1, p2) = if (a <= b) a to b else b to a
        return "$p1-trade-$p2"
    }

    private fun defaultNormalize(title: String): String {
        // strip color codes and non-alnum, collapse spaces to dashes
        val noColors = title.replace("§[0-9a-fk-or]".toRegex(RegexOption.IGNORE_CASE), "")
        val clean = noColors.lowercase().replace("[^a-z0-9]+".toRegex(), "-").trim('-')
        return clean
    }
}
