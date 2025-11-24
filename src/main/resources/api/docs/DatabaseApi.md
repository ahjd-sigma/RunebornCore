# Database API

## 1) Overview & Features

- Provides a lightweight JSON-over-SQLite repository layer.
- Typed repositories per table; auto table creation and caching.
- Preferred resolution via `ServiceHub.database` (cached after core enable)
- Fallback via `Services.require(DatabaseApi::class.java)`

## 2) How to Use

### Resolve the API

```kotlin
import com.runeborn.api.ServiceHub
val db = ServiceHub.database
```

### Typical Extension onEnable Flow

```kotlin
data class PlayerStats(val level: Int = 1, val xp: Long = 0)

override fun onEnable() {
    val db = ServiceHub.database
    val repo = db.getRepository<PlayerStats>("player_stats")

    // Create / Update
    repo.save(player.uniqueId.toString(), PlayerStats(level = 5, xp = 1200))

    // Read
    val stats = repo.get(player.uniqueId.toString())

    // Modify and persist
    stats?.let { s ->
        val next = s.copy(level = s.level + 1)
        repo.save(player.uniqueId.toString(), next)
    }

    // Delete (optional)
    // repo.delete(player.uniqueId.toString())
}
```

### Repository Operations
```kotlin
// Checks
repo.exists(player.uniqueId.toString())
repo.getAllIds()
val all = repo.getAll()

// Maintenance
repo.reload()   // clear cache and reload from DB
repo.saveAll()  // flush cache to DB
repo.saveAllTransactional() // batch in a transaction

// Paging (non-cached)
val page = repo.getPage(offset = 0, limit = 100)
```

## 3) Full Examples

### Basic Guild Data
```kotlin
data class GuildData(
    var guildName: String = "Unknown",
    var leader: String = "",
    var members: MutableList<String> = mutableListOf(),
    var bankBalance: Double = 0.0
)

val guildRepo = db.getRepository<GuildData>("guilds")

fun joinGuild(guildId: String, playerUuid: String) {
    val guild = guildRepo.get(guildId) ?: return
    if (playerUuid !in guild.members) {
        guild.members.add(playerUuid)
        guildRepo.save(guildId, guild)
    }
}
```

## 4) Advanced Notes & Reference

- Data file: `plugins/RunebornCore/database.db`
- Schema evolution:
  - Missing fields use data class defaults
  - Extra fields are ignored on read and cleaned on rewrite
- Repository API:
  - `get(id: String): T?` — read one
  - `save(id: String, obj: T)` — create or update
  - `delete(id: String)` — remove one
  - `getAll(): Map<String, T>` — read all cached
  - `exists(id: String): Boolean` — presence check
  - `getAllIds(): Set<String>` — list IDs
  - `reload()` — clear cache and reload from DB
  - `saveAll()` — flush cache to DB
  - `saveAllTransactional()` — batch flush in transaction
  - `getPage(offset, limit)` — page from DB without caching

### Lazy Repositories
- Resolve lazily: `ServiceHub.database.getRepositoryLazy<GuildData>("guilds")`

### Fallback Resolution (softdepend)
```kotlin
import com.runeborn.api.Services
val db = Services.require(com.runeborn.api.DatabaseApi::class.java)
```
- Lazy repos skip initial full-table load; use `getPage` to read slices.
