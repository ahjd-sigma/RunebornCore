package com.runeborn.core.commands.subs

import com.runeborn.api.Services
import com.runeborn.api.TestingApi
import com.runeborn.core.commands.SubCommand
import org.bukkit.command.CommandSender

class TestSubCommand : SubCommand {
    override val name: String = "test"
    override val description: String = "Run service tests"
    override fun execute(sender: CommandSender, args: Array<String>) {
        val svc = Services.get(TestingApi::class.java)
        if (svc == null) {
            com.runeborn.core.utils.Msg.send(sender, "<red>✖ Testing service not available</red>")
            return
        }
        com.runeborn.core.utils.Msg.send(sender, "<green>Running service tests…</green>")
        val ok = svc.runAllTests()
        if (ok) com.runeborn.core.utils.Msg.send(sender, "<green>✔ All tests passed!</green>") else com.runeborn.core.utils.Msg.send(sender, "<red>✖ Some tests failed.</red> <gray>Check console for details.</gray>")
    }
}
