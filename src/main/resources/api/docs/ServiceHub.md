# ServiceHub — Centralized Access to Core Services

## Overview

- Single access point for all RunebornCore services
- Typed properties cached after core initializes: `config`, `commands`, `gui`, `debugger`, `testing`, `ticker`, `metrics`, `database`
- Initialization happens in core `onEnable` and emits `ServiceHubReadyEvent`
- Provides readiness guard: `isReady()`; optional accessors `tryXxx()` for softdepend

## Plugin Metadata

- Plugin name: `RunebornCore`
- Main class: `com.runeborn.core.RunebornCore`
- Paper descriptor: `paper-plugin.yml` with `api-version: 1.21`

## Recommended Load Order

- Preferred: declare `depend: [RunebornCore]` in your plugin descriptor to ensure core is enabled before your `onEnable`
- Alternative: use `softdepend: [RunebornCore]` and guard with `ServiceHub.isReady()` or listen for `ServiceHubReadyEvent`

## Full Guide: Connecting to ServiceHub

### 1) Descriptor (`plugin.yml` or `paper-plugin.yml`)

```
name: RbHologram
version: 1.0.0
main: ahjd.rbHologram.RbHologram
depend: [RunebornCore]
api-version: 1.21
```

### 2) Access in `onEnable`

```kotlin
override fun onEnable() {
    if (!com.runeborn.api.ServiceHub.isReady()) return

    val cfg = com.runeborn.api.ServiceHub.config
    val cmds = com.runeborn.api.ServiceHub.commands
    val db = com.runeborn.api.ServiceHub.database
    val ticks = com.runeborn.api.ServiceHub.ticker

    cmds.registerMainCommand("rbholo", "Hologram tools")
    cmds.registerSubcommand(
        "rbholo", "test", "Run tests", null,
        { sender, _ -> sender.sendMessage("ok") }, null
    )
}
```

### 3) Optional: Listen for `ServiceHubReadyEvent` (softdepend)

```kotlin
class MyListener : org.bukkit.event.Listener {
    @org.bukkit.event.EventHandler
    fun onReady(e: com.runeborn.api.events.ServiceHubReadyEvent) {
        val cmds = com.runeborn.api.ServiceHub.commands
        // proceed with registration
    }
}

override fun onEnable() {
    server.pluginManager.registerEvents(MyListener(), this)
}
```

### 4) Fallback Access (older cores or softdepend)

```kotlin
val cmds = com.runeborn.api.Services.require(com.runeborn.api.CommandsApi::class.java)
```

## Best Practices

- Do not shade/relocate `com.runeborn.api`; type identity must match across plugins
- Prefer `depend` for deterministic load order; use event or `isReady()` guard only when soft depending
- Keep command registration lightweight; offload heavy work to async and hop back for Bukkit APIs

## API Surface Reference

- Readiness: `ServiceHub.isReady()`
- Initialization: performed by `RunebornCore` during `onEnable`
- Typed access: `config`, `commands`, `gui`, `debugger`, `testing`, `ticker`, `metrics`, `database`
- Optional accessors: `tryConfig()`, `tryCommands()`, `tryGui()`, `tryDebugger()`, `tryTesting()`, `tryTicker()`, `tryMetrics()`, `tryDatabase()`
