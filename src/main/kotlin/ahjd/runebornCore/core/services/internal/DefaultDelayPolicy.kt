package com.runeborn.core.services.internal

import com.runeborn.api.DelayPolicy
import com.runeborn.core.utils.ClickDelayController

class DefaultDelayPolicy : DelayPolicy {
    override fun canClick(controller: ClickDelayController?, slot: Int, isHandledClick: Boolean): Pair<Boolean, Long> {
        if (controller == null || !isHandledClick) return true to 0L
        return controller.canClick(slot)
    }
}

