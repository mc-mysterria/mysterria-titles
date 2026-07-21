package net.mysterria.titles;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.mysterria.titles.buff.TitleBuffManager;
import net.mysterria.titles.buff.impl.ExpBoostBuff;
import net.mysterria.titles.command.TitlesAdminCommand;
import net.mysterria.titles.command.TitlesCommand;
import net.mysterria.titles.command.argument.TitleArgument;
import net.mysterria.titles.command.exception.InvalidUsageHandler;
import net.mysterria.titles.command.exception.PermissionsHandler;
import net.mysterria.titles.config.ConfigManager;
import net.mysterria.titles.listener.PlayerLifecycleListener;
import net.mysterria.titles.model.Title;
import net.mysterria.titles.placeholder.TitlesExpansion;
import net.mysterria.titles.registry.TitleRegistry;
import net.mysterria.titles.service.TitleBonusService;
import net.mysterria.titles.storage.JsonPlayerDataStore;
import net.mysterria.titles.storage.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MysterriaTitles extends JavaPlugin {

    private ConfigManager configManager;
    private TitleRegistry titleRegistry;
    private JsonPlayerDataStore playerDataStore;
    private PlayerDataManager playerDataManager;
    private TitleBonusService bonusService;
    private TitleBuffManager buffManager;
    private TitlesExpansion titlesExpansion;
    private LiteCommands<CommandSender> liteCommands;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        configManager.load();

        titleRegistry = new TitleRegistry(getLogger());
        titleRegistry.load(configManager.getTitlesConfig());

        playerDataStore = new JsonPlayerDataStore(getDataFolder().toPath().resolve("playerdata"), getLogger());
        playerDataManager = new PlayerDataManager(this, playerDataStore);

        bonusService = new TitleBonusService(playerDataManager, titleRegistry);

        buffManager = new TitleBuffManager(getLogger());
        buffManager.register(new ExpBoostBuff(this));
        buffManager.validateAgainstRegistry(titleRegistry);
        buffManager.enableAll();

        Bukkit.getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);

        this.liteCommands = LiteBukkitFactory.builder("titles", this)
                .argument(Title.class, new TitleArgument(titleRegistry))
                .missingPermission(new PermissionsHandler())
                .invalidUsage(new InvalidUsageHandler())
                .commands(
                        new TitlesCommand(this),
                        new TitlesAdminCommand(this)
                )
                .build();

        titlesExpansion = new TitlesExpansion(this);
        titlesExpansion.register();

        playerDataManager.startAutosaveTask(configManager.getSettings().getAutosaveIntervalSeconds() * 20L);

        for (Player player : Bukkit.getOnlinePlayers()) {
            playerDataManager.load(player.getUniqueId());
        }
    }

    @Override
    public void onDisable() {
        if (buffManager != null) {
            buffManager.disableAll();
        }
        if (playerDataManager != null) {
            playerDataManager.stopAutosaveTask();
            playerDataManager.flushAll().join();
        }
        if (playerDataStore != null) {
            playerDataStore.shutdown();
        }
        if (titlesExpansion != null) {
            titlesExpansion.unregister();
        }
    }

    public void reload() {
        configManager.load();
        titleRegistry.load(configManager.getTitlesConfig());
        buffManager.validateAgainstRegistry(titleRegistry);
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TitleRegistry getTitleRegistry() {
        return titleRegistry;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public TitleBonusService getBonusService() {
        return bonusService;
    }

    public TitleBuffManager getBuffManager() {
        return buffManager;
    }
}
