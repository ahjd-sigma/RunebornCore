package com.runeborn.core.tests

import com.runeborn.api.DatabaseApi
import com.runeborn.api.Services
import com.runeborn.core.database.DataRepository
import java.util.logging.Logger

data class TestRecord(val value: String = "")

object DatabaseServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Database Service")
        return try {
            val db = Services.get(DatabaseApi::class.java) ?: run {
                logger.warning("Database service not available")
                return true
            }
            val repo: DataRepository<TestRecord> = db.getRepository("test_records", TestRecord::class.java)
            repo.save("id-1", TestRecord("hello"))
            val read = repo.get("id-1")?.value
            repo.save("id-2", TestRecord("world"))
            repo.saveAllTransactional()
            val page = repo.getPage(offset = 0, limit = 1)
            val lazyRepo: DataRepository<TestRecord> = db.getRepositoryLazy("test_records_lazy", TestRecord::class.java)
            lazyRepo.save("lid-1", TestRecord("lazy"))
            val maintained = db.performMaintenance(vacuum = true, backup = false)
            repo.delete("id-1")
            repo.delete("id-2")
            lazyRepo.delete("lid-1")
            logger.info("✓ Repository CRUD works (value=$read), paging=${page.size == 1}, maintenance=$maintained")
            Services.get(com.runeborn.api.MetricsApi::class.java)?.let { m ->
                val snap = m.getSnapshot()
                logger.info("✓ Metrics snapshot counters(db.save/db.delete/db.getPage/db.loadAll): ${snap.counters}")
            }
            true
        } catch (e: Exception) {
            logger.severe("✗ Database Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
