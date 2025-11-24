package com.runeborn.core.utils

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class ClickDelayController(
    globalDelayMs: Long? = null,
    perSlotDelayMs: MutableMap<Int, Long> = mutableMapOf(),
    private val nowProvider: () -> Long = System::currentTimeMillis
) {
    @Volatile private var globalDelay: Long? = validate(globalDelayMs)
    private val perSlotDelays = perSlotDelayMs.mapValues { validate(it.value) }.filterValues { it != null }.mapValues { it.value!! }.toMutableMap()

    private val lastGlobalClick = AtomicLong(0L)
    private val lastSlotClicks = ConcurrentHashMap<Int, AtomicLong>()

    fun setGlobalDelay(ms: Long?) { globalDelay = validate(ms) }
    fun setSlotDelay(slot: Int, ms: Long?) {
        val v = validate(ms)
        if (v == null) perSlotDelays.remove(slot) else perSlotDelays[slot] = v
    }

    fun canClick(slot: Int): Pair<Boolean, Long> {
        val delay = perSlotDelays[slot] ?: globalDelay
        if (delay == null) return true to 0L
        val now = nowProvider()
        return if (perSlotDelays.containsKey(slot)) {
            val last = lastSlotClicks.computeIfAbsent(slot) { AtomicLong(0L) }
            val elapsed = now - last.get()
            if (elapsed >= delay) {
                last.set(now)
                true to 0L
            } else false to (delay - elapsed)
        } else {
            val elapsed = now - lastGlobalClick.get()
            if (elapsed >= delay) {
                lastGlobalClick.set(now)
                true to 0L
            } else false to (delay - elapsed)
        }
    }

    private fun validate(ms: Long?): Long? {
        if (ms == null) return null
        if (ms <= 0) return null
        return ms
    }
}