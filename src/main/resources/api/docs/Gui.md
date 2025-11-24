# GUI API

## 1) Overview & Features

Gui is a lightweight, intuitive GUI creation system with:
- Builder pattern for private GUIs (size, slots, metadata)
- Click actions: LEFT/RIGHT/SHIFT_LEFT/SHIFT_RIGHT/MIDDLE/NUMBER_KEY/DROP/CTRL_DROP
- Per‑click sounds and optional generic click sound
- Delay system: global and per‑slot with optional messages/sounds
- Unassigned click handling (optional sound/message)
- Hooks for empty slots and player inventory clicks
- Shared/public sessions: multi‑viewer GUIs with IDs, metadata, persistence
- Access policies: `requiredPermission`, `maxViewers`, operator bypass controls

## Quick Start

```kotlin
import com.runeborn.core.utils.Gui
import com.runeborn.core.utils.ItemMetaData
import com.runeborn.api.ServiceHub

val guiApi = ServiceHub.gui
```
// Create a simple GUI
Gui.create(27, "§6My Shop")
    .slot(10, Material.DIAMOND) { action ->
        player.sendMessage("You clicked slot ${action.slot}")
    }
    .open(guiApi, player)
```

## 2) How to Use

### Private GUIs

#### Create a builder
`Gui.create(size: Int, title: String): GuiBuilder`

Creates a new GUI builder with the specified size and title.

**Parameters:**
- `size`: Inventory size (must be multiple of 9, max 54)
- `title`: GUI title with color codes support

**Returns:** GuiBuilder instance for chaining

#### Assign slots

#### Basic Slot Assignment
```kotlin
.slot(slot: Int, item: ItemStack, action: ((ClickAction) -> Unit)? = null, sound: Sound? = null)
.slot(slot: Int, material: Material, action: ((ClickAction) -> Unit)? = null, sound: Sound? = null)
```

#### With Metadata
```kotlin
.slot(slot: Int, item: ItemStack, meta: ItemMetaData, action: ((ClickAction) -> Unit)? = null, sound: Sound? = null)
.slot(slot: Int, material: Material, meta: ItemMetaData, action: ((ClickAction) -> Unit)? = null, sound: Sound? = null)
```

**Parameters:**
- `slot`: Slot number (0-based)
- `item`/`material`: Item to place in slot
- `meta`: Optional metadata for name, lore, flags
- `action`: Click handler (optional)
- `sound`: Optional sound to play when clicked (optional)

#### Open GUI

Opens the GUI for the specified player.

Optional parameters for UX and anti‑spam:

```
open(
  guiApi,
  player,
  listenEmptySlots = false,
  onEmptySlot = null,
  listenPlayerInventory = false,
  onPlayerInventoryClick = null,
  globalDelayMs = null,
  perSlotDelayMs = null,
  showCooldownMessage = false,
  cooldownMessage = null,
  cooldownSound = null,
  unassignedClickSound = null,
  showUnassignedMessage = false,
  unassignedMessage = null
)
```

Open behavior:
- Delay applies only to assigned click types for a slot
- Unassigned clicks can optionally play `unassignedClickSound` and show `unassignedMessage`
- Cooldown blocks handler and plays `cooldownSound` when provided

### Click Actions

#### Data

```kotlin
data class ClickAction(
    val slot: Int,              // The clicked slot
    val type: ClickType,        // Type of click
    val isLeftClick: Boolean,   // true if left click
    val isRightClick: Boolean,  // true if right click
    val isShiftClick: Boolean,  // true if shift held
    val isMiddleClick: Boolean // true if middle click
)
```

#### Types

```kotlin
enum class ClickType {
    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT, MIDDLE, NUMBER_KEY, DROP, CTRL_DROP
}
```

### Item Metadata

#### Data

```kotlin
data class ItemMetaData(
    val name: String? = null,           // Display name with color codes
    val lore: List<String>? = null,     // Lore lines with color codes
    val hideFlags: Boolean = true,      // Hide vanilla item flags
    val enchantGlint: Boolean = false   // Add enchantment glint
)
```

#### Helpers

```kotlin
// Add metadata to existing item
ItemMeta.addMeta(item: ItemStack, meta: ItemMetaData): ItemStack

// Create new item with metadata
ItemMeta.createWithMeta(material: Material, meta: ItemMetaData): ItemStack
```

## 3) Full Examples

### Simple Chest GUI
```kotlin
Gui.create(27, "§eStorage")
    .slot(0, Material.CHEST) { action ->
        player.sendMessage("Opened chest at slot ${action.slot}")
    }
    .slot(1, Material.DIAMOND) { action ->
        player.sendMessage("Found diamond!")
    }
    .open(guiApi, player)
```

### Shop with Metadata and Sounds
```kotlin
Gui.create(27, "§6Item Shop")
    .slot(10, Material.DIAMOND_SWORD, ItemMetaData(
        name = "§bDiamond Sword",
        lore = listOf("§7Cost: 100 coins", "§7Click to buy!"),
        hideFlags = true
    ), Sound.BLOCK_ANVIL_USE) { action ->
        when (action.type) {
            ClickType.LEFT -> buyItem(player, "diamond_sword", 100)
            ClickType.SHIFT_LEFT -> buyStack(player, "diamond_sword", 1000)
            else -> player.sendMessage("§cUse left click to buy")
        }
    }
    .open(guiApi, player)
```

### Confirmation Dialog with Sounds
```kotlin
Gui.create(27, "§cDelete Item?")
    .slot(11, Material.GREEN_WOOL, ItemMetaData(
        name = "§aConfirm",
        lore = listOf("§7Click to delete permanently"),
        hideFlags = true
    ), Sound.BLOCK_NOTE_BLOCK_PLING) { action ->
        deleteItem(player)
        player.sendMessage("§aItem deleted!")
    }
    .slot(15, Material.RED_WOOL, ItemMetaData(
        name = "§cCancel",
        lore = listOf("§7Keep the item"),
        hideFlags = true
    ), Sound.BLOCK_NOTE_BLOCK_BASS) { action ->
        player.sendMessage("§cDeletion cancelled!")
    }
    .open(guiApi, player)
```

### Click Type Tester with Sounds
```kotlin
Gui.create(9, "§6Click Test")
    .slot(4, Material.STICK, ItemMetaData(
        name = "§eClick Me!",
        lore = listOf(
            "§7Left: Message A",
            "§7Right: Message B", 
            "§7Shift+Left: Message C",
            "§7Shift+Right: Message D"
        ),
        hideFlags = true
    ), Sound.UI_BUTTON_CLICK) { action ->
        when (action.type) {
            ClickType.LEFT -> player.sendMessage("§aLeft click!")
            ClickType.RIGHT -> player.sendMessage("§bRight click!")
            ClickType.SHIFT_LEFT -> player.sendMessage("§6Shift left!")
            ClickType.SHIFT_RIGHT -> player.sendMessage("§dShift right!")
            else -> player.sendMessage("§7Other click: ${action.type}")
        }
    }
    .open(guiApi, player)
```

### Sound Effects Demo
```kotlin
Gui.create(27, "§dSound Gallery")
    .slot(10, Material.NOTE_BLOCK, ItemMetaData(
        name = "§aChime Sound",
        lore = listOf("§7Click to play a pleasant chime")
    ), Sound.BLOCK_NOTE_BLOCK_CHIME) { action ->
        player.sendMessage("§aPlayed chime sound!")
    }
    .slot(12, Material.JUKEBOX, ItemMetaData(
        name = "§6Xylophone",
        lore = listOf("§7Click to play xylophone sound")
    ), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE) { action ->
        player.sendMessage("§6Played xylophone sound!")
    }
    .slot(14, Material.BELL, ItemMetaData(
        name = "§cBell Sound",
        lore = listOf("§7Click to play bell sound")
    ), Sound.BLOCK_BELL_USE) { action ->
        player.sendMessage("§cPlayed bell sound!")
    }
    .slot(16, Material.DRAGON_HEAD, ItemMetaData(
        name = "§5Epic Sound",
        lore = listOf("§7Click to play epic totem sound"),
        enchantGlint = true
    ), Sound.ITEM_TOTEM_USE) { action ->
        player.sendMessage("§5Played totem sound!")
    }
    .open(guiApi, player)
```

### Command Integration

The Gui system is automatically available when RunebornCore is enabled. Use the `/runeborn guitest` command to test examples:

- `/runeborn guitest shop` - Simple shop GUI with sounds
- `/runeborn guitest confirm` - Confirmation dialog built via builder (confirm helper removed)
- `/runeborn guitest chest` - Simple chest GUI with sounds
- `/runeborn guitest clicks` - Click type tester with sounds
- `/runeborn guitest sounds` - Sound effects demo
- `/runeborn guitest help` - Shows available tests
 - `/runeborn guitest advanced` - Advanced click types and sounds
 - `/runeborn guitest cooldown` - Cooldown demo
 - `/runeborn guitest hooks` - Empty slot and inventory hooks
 - `/runeborn guitest public` - Public shared container

### Sound System

#### Available Sound Parameters

All slot methods accept an optional `Sound` parameter as the last argument:

```kotlin
.slot(slot, material, action, sound)
.slot(slot, material, meta, action, sound)
.slot(slot, item, action, sound)
.slot(slot, item, meta, action, sound)
```

#### Popular Minecraft Sounds

```kotlin
// UI/Interaction Sounds
Sound.UI_BUTTON_CLICK              // Classic button click
Sound.BLOCK_NOTE_BLOCK_PLING       // Pleasant bell sound
Sound.BLOCK_NOTE_BLOCK_BASS        // Deep bass sound
Sound.BLOCK_NOTE_BLOCK_CHIME       // Soft chime
Sound.BLOCK_NOTE_BLOCK_XYLOPHONE   // Xylophone sound

// Item Sounds
Sound.ITEM_TOTEM_USE               // Epic totem sound
Sound.ENTITY_VILLAGER_YES          // Positive villager sound
Sound.ENTITY_VILLAGER_NO           // Negative villager sound
Sound.ENTITY_EXPERIENCE_ORB_PICKUP // XP pickup sound

// Block Sounds
Sound.BLOCK_ANVIL_USE              // Anvil clink
Sound.BLOCK_CHEST_OPEN             // Chest open
Sound.BLOCK_BELL_USE               // Bell ring
Sound.BLOCK_ENCHANTMENT_TABLE_USE  // Enchantment sound
```

#### Best Practices

1. **Use subtle sounds** - Avoid loud or jarring sounds
2. **Match sound to action** - Positive sounds for success, negative for failures
3. **Don't overuse** - Not every slot needs a sound
4. **Consider context** - Shop purchases might use coin sounds, confirmations use pleasant tones
5. **Test volume** - Sounds play at 1.0f volume and 1.0f pitch by default

### General Best Practices

1. **Use metadata for clarity** - Add names and lore to make GUI items self-explanatory
2. **Handle different click types** - Support various interactions (left, right, shift combinations)
3. **Provide feedback** - Always give players feedback when they click items
4. **Use sounds strategically** - Enhance UX with appropriate sound effects
5. **Keep it simple** - Gui is designed for straightforward GUIs
6. **Use consistent slot patterns** - Follow common Minecraft GUI conventions

### Error Handling

- Invalid slot numbers are ignored
- Null actions create non-clickable items
- Invalid sizes default to 27
- Missing GUI service shows error to player
- Invalid sounds are ignored silently

### Performance Notes

- Items are cloned to prevent modification
- Metadata is applied efficiently
- Click detection is optimized
- No persistent state between GUI opens
- Sounds play asynchronously without blocking

## Access Control (Planned)

### Implemented

- Options
  - `requiredPermission: String?` — raw permission required to view
  - `maxViewers: Int?` — limit for non‑operator viewers
  - `operatorsBypassLimit: Boolean` — operators can view beyond the limit without consuming slots
  - `operatorsBypassPermission: Boolean` — operators bypass permission requirement
- Enforcement
  - `addViewer(sessionId, player)` denies when permission absent or viewer limit reached
  - Operators bypass maxViewers (do not count toward the limit) but must still have permission when required
- Notes
  - Limits are based on non‑operator viewer count and do not drift due to operator access
  - Future: owner/whitelist/blacklist lists, audit logs, denied feedback
## 4) Advanced Notes & Reference

- `openConfirm(...)` in `GuiApi` has been removed. Build confirm/cancel UIs with the builder (`slotClick`/`slotAdvanced`) and present via `openMenu`/`openMenuWithOptions`.

### Shared Sessions Quick‑Start

- Create ID: `makePublicIdFromTitle("Leaderboard")`, or `makeTradeId("Alice","Bob")`
- Open session: `openSharedMenuWithOptions(id, title, size, buttons, options)`
- Add viewer: `addViewer(id, player)`
- Live update: `updateSharedSlotItem(id, slot, item)`, `setSharedButtons(id, buttons)`
- Metadata: `setSharedMetadata(id, name, ownerUuid)`; list sessions: `listSharedSessions()`
- Persistence: set `persistentSession=true` in options to keep session alive without viewers
- Item moves: set `allowTopInventoryItemMoves=true` to allow players to place/take items

### Access Policies (Implemented)

- Options
  - `requiredPermission: String?`
  - `maxViewers: Int?` (non‑ops only)
  - `operatorsBypassLimit: Boolean`
  - `operatorsBypassPermission: Boolean`
- Enforcement
  - Operators bypass limit and (optionally) permission; non‑ops counted against `maxViewers`
