package ahjd.runebornCore.core

import com.runeborn.api.*
import com.runeborn.api.ServiceHub
import com.runeborn.api.events.ServiceHubReadyEvent
import com.runeborn.core.commands.CommandRegistry
import com.runeborn.core.commands.subs.MetricsSubCommand
import com.runeborn.core.commands.subs.TestSubCommand
import com.runeborn.core.commands.subs.TickersSubCommand
import com.runeborn.core.config.ConfigManager
import com.runeborn.core.config.ConfigService
import com.runeborn.core.services.*
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin

class RunebornCore : JavaPlugin() {
    private lateinit var configManager: ConfigManager

    override fun onEnable() {
        if (!dataFolder.exists()) dataFolder.mkdirs()
        configManager = ConfigManager(this)
        server.servicesManager.register(ConfigService::class.java, configManager, this, ServicePriority.Normal)
        server.servicesManager.register(ConfigApi::class.java, configManager, this, ServicePriority.Normal)

        val commandsSvc = CommandsService()
        val guiSvc = GuiService(this)
        val debuggerSvc = DebuggerService(this)
        val testingSvc = TestingService(this)
        val tickerSvc = TickerService(this)
        val metricsSvc = MetricsService()
        val databaseSvc = DatabaseService(this)
        server.servicesManager.register(CommandsApi::class.java, commandsSvc, this, ServicePriority.Normal)
        server.servicesManager.register(GuiApi::class.java, guiSvc, this, ServicePriority.Normal)
        server.servicesManager.register(DebuggerApi::class.java, debuggerSvc, this, ServicePriority.Normal)
        server.servicesManager.register(TestingApi::class.java, testingSvc, this, ServicePriority.Normal)
        server.servicesManager.register(TickerApi::class.java, tickerSvc, this, ServicePriority.Normal)
        server.servicesManager.register(MetricsApi::class.java, metricsSvc, this, ServicePriority.Normal)
        server.servicesManager.register(DatabaseApi::class.java, databaseSvc, this, ServicePriority.Normal)
        ServiceHub.initialize()
        server.pluginManager.callEvent(ServiceHubReadyEvent())

        CommandRegistry.init(this)
        val runebornMain = CommandRegistry.createMain("runeborn", "RunebornCore main command", null)

        guiSvc.register()
        databaseSvc.initialize()
        
        runebornMain.addSubCommand(TestSubCommand())
        runebornMain.addSubCommand(MetricsSubCommand())
        runebornMain.addSubCommand(TickersSubCommand())
        
    }

    override fun onDisable() {
        server.servicesManager.unregister(ConfigService::class.java)
        if (ServiceHub.isReady()) {
            ServiceHub.database.shutdown()
            ServiceHub.ticker.cancelAll()
        }
        
        
        try { CommandRegistry.unregisterAll() } catch (_: Exception) {}
    }

    
}
