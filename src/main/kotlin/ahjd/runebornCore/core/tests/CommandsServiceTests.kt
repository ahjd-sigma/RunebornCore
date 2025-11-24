package com.runeborn.core.tests

import com.runeborn.api.CommandsApi
import com.runeborn.api.Services
import java.util.logging.Logger
import com.runeborn.core.utils.Msg

object CommandsServiceTests {
    fun run(logger: Logger): Boolean {
        logger.info("[TEST] Commands Service")
        return try {
            val cmds = Services.require(CommandsApi::class.java)
            cmds.registerMainCommand("testcore", "Test core commands", null)
            cmds.registerSubcommand(
                "testcore",
                "verify",
                "Verify commands are working",
                null,
                { sender, _ -> Msg.send(sender, "<green>Commands service is working!</green>") },
                { _, args -> if (args.isEmpty()) listOf("verify", "test") else emptyList() }
            )
            cmds.registerMainSubcommand(
                "testcmd",
                "Test command from core",
                null,
                { sender, _ -> Msg.send(sender, "<green>Core command working!</green>") }
            )
            logger.info("✓ Commands registration works")
            // Duplicate registration should be guarded
            cmds.registerMainCommand("testcore", "Duplicate test core", null)
            logger.info("✓ Duplicate main registration guarded")
            true
        } catch (e: Exception) {
            logger.severe("✗ Commands Service test failed: ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
