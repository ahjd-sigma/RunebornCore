# Commands API

## 1) Overview & Features

- Register a root command with description and permission
- Attach subcommands with execution and tab completion
- Runtime registration (no plugin.yml entries required)
- Preferred resolution via `ServiceHub.commands` (cached after core enable)
- Fallback via `Services.require(CommandsApi::class.java)`

## 2) How to Use

### Resolve the API
```kotlin
import com.runeborn.api.ServiceHub
val cmds = ServiceHub.commands
```

### Register a Main Command
```kotlin
cmds.registerMainCommand(
    name = "rbrpg",
    description = "RPG command",
    permission = "rbrpg.use"
)
```

### Add Subcommands under your Main
```kotlin
cmds.registerSubcommand(
    mainName = "rbrpg",
    name = "class",
    description = "Class operations",
    permission = "rbrpg.class",
    executor = { sender, args -> sender.sendMessage("Class: ${args.joinToString(" ")}") },
    tabCompleter = { _, args ->
        when (args.size) {
            1 -> listOf("select", "respec").filter { it.startsWith(args.last(), true) }
            2 -> listOf("Mage", "Warrior", "Archer").filter { it.startsWith(args.last(), true) }
            else -> emptyList()
        }
    }
)
```

### Attach Quick Subcommands to /runeborn
```kotlin
cmds.registerMainSubcommand(
    name = "feature",
    description = "Feature tools",
    permission = "runeborn.feature.use",
    executor = { sender, _ -> sender.sendMessage("feature executed") },
    tabCompleter = { _, args -> listOf("start", "stop").filter { it.startsWith(args.lastOrNull() ?: "", true) } }
)
```

### Register a Simple Dynamic Command (standalone)
```kotlin
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

cmds.registerDynamicCommand(
    namespace = "rbrpg",
    name = "debug",
    executor = object : CommandExecutor, TabCompleter {
        override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
            sender.sendMessage("Debug executed")
            return true
        }
        override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
            return listOf("info", "reset").filter { it.startsWith(args.lastOrNull() ?: "", true) }
        }
    }
)
```

## 3) Full Examples

### GUI Test Dispatcher
```kotlin
cmds.registerMainSubcommand(
    name = "guitest",
    description = "Open GUI examples",
    permission = "runeborn.guitest.use",
    executor = { sender, args ->
        val p = sender as? org.bukkit.entity.Player ?: run {
            sender.sendMessage("§cOnly players can use this command")
            return@registerMainSubcommand
        }
        when (args.firstOrNull()?.lowercase()) {
            "shop" -> GuiExamples.exampleShop(guiSvc, p)
            "confirm" -> GuiExamples.exampleConfirmationDialog(guiSvc, p)
            "chest" -> GuiExamples.exampleChest(guiSvc, p)
            "advanced" -> GuiExamples.exampleAdvanced(guiSvc, p)
            "cooldown" -> GuiExamples.exampleCooldown(guiSvc, p)
            "hooks" -> GuiExamples.exampleHooks(guiSvc, p)
            "public" -> {
                val id = guiSvc.makePublicIdFromTitle("Public Container", null)
                if (!guiSvc.isSharedMenuOpen(id)) {
                    guiSvc.openSharedMenuWithOptions(id, "§bPublic Container", 54, listOf(), com.runeborn.api.GuiOptions())
                }
                guiSvc.addViewer(id, p)
            }
            else -> p.sendMessage("§eUsage: /runeborn guitest <shop|confirm|chest|advanced|cooldown|hooks|public>")
        }
    },
    tabCompleter = { _, args ->
        val opts = listOf("shop", "confirm", "chest", "advanced", "cooldown", "hooks", "public", "help")
        val key = args.firstOrNull() ?: ""
        opts.filter { it.startsWith(key, ignoreCase = true) }
    }
)
```

## 4) Advanced Notes & Reference

- Use module namespaces (e.g., `rbrpg`, `rbitems`) to avoid clashes
- Always check permissions inside executors
- Avoid long-running tasks; use async or schedulers
- Provide helpful error messages and tab completions

### Fallback Resolution (softdepend)
```kotlin
import com.runeborn.api.Services
val cmds = Services.require(com.runeborn.api.CommandsApi::class.java)
```
