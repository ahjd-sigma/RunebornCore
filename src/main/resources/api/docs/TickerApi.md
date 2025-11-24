# Ticker API

## 1) Overview & Features

- Create and manage repeating tick tasks with lifecycle control
- Async or sync scheduling, one-shot delayed tasks
- Preferred resolution via `ServiceHub.ticker` (cached after core enable)
- Fallback via `Services.require(TickerApi::class.java)`

## 2) How to Use

### Resolve the API
```kotlin
import com.runeborn.api.ServiceHub
val ticks = ServiceHub.ticker
```

### Typical Usage
```kotlin
val id = ticks.createTicker(name = "regen", periodTicks = 20L, async = false) { n ->
    player.heal(1.0)
}

// Pause and resume
ticks.pauseTicker(id)
ticks.resumeTicker(id)

// Stop
ticks.stopTicker(id)

// One-shot delayed task
ticks.runOnce(delayTicks = 40L, async = true) { player.sendMessage("Ready") }
```

### Inspect
```kotlin
val infos = ticks.listTickers()
val info = ticks.getTickerInfo(id)
val running = ticks.listRunningTickers()
val paused = ticks.listPausedTickers()
val summary = ticks.getSummary()
```

## 3) Advanced Notes & Reference

- `createTicker(name, periodTicks, async, startImmediately, runnable)`
- `startTicker(id)` / `pauseTicker(id)` / `resumeTicker(id)` / `stopTicker(id)`
- `listTickers()` returns `TickerInfo`
- `listRunningTickers()` / `listPausedTickers()`
- `getSummary()` returns counts and total executions
- `runOnce(delayTicks, async, runnable)` for delayed one-shot
- `cancelAll()` to stop everything

### Fallback Resolution (softdepend)
```kotlin
import com.runeborn.api.Services
val ticks = Services.require(com.runeborn.api.TickerApi::class.java)
```

## 4) Sync vs Async

- Sync (main thread)
  - Use for world and Bukkit API interactions: entities, blocks, inventories, GUIs, sounds, events
  - Keep work lightweight; split heavy jobs across ticks to avoid lag
  - Create with `async = false`

- Async (background thread)
  - Use for CPU-heavy work, file/DB I/O, networking; do not call Bukkit APIs directly
  - Hand off results back to sync using `Bukkit.getScheduler().runTask(plugin) { ... }`
  - Create with `async = true`

### Example: Async compute, Sync apply
```kotlin
val id = ticks.createTicker(name = "compute", periodTicks = 20L, async = true) { n ->
    val result = heavyCalc()
    org.bukkit.Bukkit.getScheduler().runTask(plugin) {
        applyResultToWorld(result)
    }
}
```

### Why not make everything async?

- Most Bukkit/Paper APIs are not thread-safe and must be called on the main thread.
- World state changes (entities, blocks, inventories) must be tick-aligned; off-thread calls can corrupt state or crash.
- Packet sending, events, sounds, scoreboard/HUD updates also require main-thread execution.
- Async loops drift from TPS and ignore server throttling; using the scheduler keeps behavior consistent with server load.

### When to hop back to sync inside async

- Hop back to sync for any code that touches Bukkit or world state.
- Pure compute or I/O can remain async; only the “apply” phase needs to run sync.
- Encapsulating a method that uses Bukkit inside an async ticker is still unsafe; wrap that call with `Bukkit.getScheduler().runTask(plugin) { ... }`.

### Safe pattern

```kotlin
// Async compute
val id = ticks.createTicker("ai", 40L, async = true) { _ ->
    val path = computePathForMob()
    // Sync apply (world interaction)
    org.bukkit.Bukkit.getScheduler().runTask(plugin) {
        mob.follow(path)
    }
}
```

### Unsafe pattern (avoid)

```kotlin
// Async ticker directly calling Bukkit APIs – NOT SAFE
val id = ticks.createTicker("bad", 20L, async = true) { _ ->
    player.world.spawnEntity(player.location, org.bukkit.entity.EntityType.ZOMBIE) // off-thread
}
```
