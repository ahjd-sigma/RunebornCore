package com.runeborn.core.services.internal

import com.runeborn.api.AccessPolicy
import com.runeborn.api.GuiOptions
import org.bukkit.entity.Player

class DefaultAccessPolicy : AccessPolicy {
    override fun canView(player: Player, options: GuiOptions?, nonOperatorViewerCount: Int): Boolean {
        val opts = options ?: return true
        // Permission requirement
        opts.requiredPermission?.let {
            val hasPerm = player.hasPermission(it) || (opts.operatorsBypassPermission && player.isOp)
            if (!hasPerm) return false
        }
        // Viewer limit
        val limit = opts.maxViewers
        val canBypass = opts.operatorsBypassLimit && player.isOp
        if (limit != null && limit > 0 && !canBypass) {
            if (nonOperatorViewerCount >= limit) return false
        }
        return true
    }
}

