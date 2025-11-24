# Config API

## 1) Overview & Features

- Manage per-module YAML configs located at `plugins/RunebornCore/<Module>/<file>.yml`.
- Support both settings mode (typed getters/setters) and object mode (section maps).

## 2) How to Use

### Resolve the API

```kotlin
import com.runeborn.api.ServiceHub
val cfg = ServiceHub.config
```

### Create/Ensure Config with Defaults

```kotlin
// Creates file from packaged defaults if available, else minimal YAML
cfg.ensureConfig("RbItems", "items.yml")
```

### Settings Mode (Typed Getters/Setters)

```kotlin
val enableCrits = cfg.getOrDefaultBoolean("RbRPG", "rpg.yml", "damage.enableCriticals", true)
val rarityColor = cfg.getOrDefaultString("RbItems", "items.yml", "rarities.common.color", "GRAY")

cfg.set("RbItems", "items.yml", "rarities.common.color", "DARK_GRAY")
cfg.reload("RbItems", "items.yml")
```

### Object Mode (Sections as Maps)

```kotlin
val epic = cfg.getSectionMap("RbItems", "items.yml", "rarities.epic")
val updated = epic + mapOf("color" to "DARK_PURPLE")
cfg.setSectionFromMap("RbItems", "items.yml", "rarities.epic", updated)

// Ensure a section exists (creates it if missing)
cfg.ensureSection("RbItems", "items.yml", "rarities.legendary")
```

### List Available Config Files

```kotlin
val files = cfg.listFiles("RbItems") // e.g., [items.yml]
```

## 3) Full Examples

### Typical Extension onEnable Flow

```kotlin
override fun onEnable() {
    val cfg = ServiceHub.config
    cfg.ensureConfig("RbItems", "items.yml")
    val color = cfg.getOrDefaultString("RbItems", "items.yml", "rarities.common.color", "GRAY")
    server.consoleSender.sendMessage("Common rarity color: $color")
}
```
## 4) Advanced Notes & Reference

- `ensureWithDefaults(module, file)` loads packaged defaults from `src/main/resources/defaults/<Module>/<file>` when available.
- `reload(module, file)` refreshes in-memory cache from disk.
- All configs are stored under the plugin’s data folder for a single-folder deployment.

### Fallback Resolution (softdepend)
```kotlin
import com.runeborn.api.Services
val cfg = Services.require(com.runeborn.api.ConfigApi::class.java)
```
