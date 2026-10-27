package net.mysterria.titles;

import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import net.mysterria.titles.command.admin.TitlesAdminCommand;
import net.mysterria.titles.command.argument.TitleArgument;
import net.mysterria.titles.command.exception.InvalidUsageHandler;
import net.mysterria.titles.command.exception.PermissionsHandler;
import net.mysterria.titles.command.player.TitlesCommand;
import net.mysterria.titles.config.ConfigManager;
import net.mysterria.titles.domain.buff.listener.BuffRegistryValidationListener;
import net.mysterria.titles.domain.buff.service.TitleBonusService;
import net.mysterria.titles.domain.buff.service.TitleBuffManager;
import net.mysterria.titles.domain.buff.type.*;
import net.mysterria.titles.domain.papi.TitlesExpansion;
import net.mysterria.titles.domain.shard.listener.CollectionerShardListener;
import net.mysterria.titles.domain.shard.service.AnniversaryTokenService;
import net.mysterria.titles.domain.shard.service.CollectionerShardService;
import net.mysterria.titles.domain.storage.listener.PlayerLifecycleListener;
import net.mysterria.titles.domain.storage.service.JsonPlayerDataStore;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.listener.CoiAvailabilityListener;
import net.mysterria.titles.domain.title.listener.CoiTitleListener;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.SequenceTitleAutoGrantService;
import net.mysterria.titles.domain.title.service.TitleProgressService;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import net.mysterria.titles.domain.title.service.TitleTestModeService;
import net.mysterria.titles.integration.CircleOfImaginationHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.xenondevs.invui.InvUI;
import xyz.xenondevs.invui.window.Window;
import xyz.xenondevs.invui.window.WindowManager;

import java.util.List;

@Getter
public class MysterriaTitles extends JavaPlugin {

    private ConfigManager configManager;
    private TitleRegistry titleRegistry;
    private JsonPlayerDataStore playerDataStore;
    private PlayerDataManager playerDataManager;
    private TitleBonusService bonusService;

    private TitleBuffManager buffManager;
    private TitlesExpansion titlesExpansion;
    private SequenceTitleAutoGrantService sequenceTitleAutoGrantService;
    private CollectionerShardService collectionerShardService;
    private AnniversaryTokenService anniversaryTokenService;
    private TitleProgressService titleProgressService;
    private TitleTestModeService titleTestModeService;
    private boolean coiIntegrationRegistered = false;

    @Override
    public void onEnable() {
        InvUI.getInstance().setPlugin(this);
        WindowManager.getInstance();

        configManager = new ConfigManager(this);
        configManager.load();

        titleRegistry = new TitleRegistry(getLogger());
        titleRegistry.load(configManager.getTitlesConfig());

        playerDataStore = new JsonPlayerDataStore(getDataFolder().toPath().resolve("playerdata"), getLogger());
        playerDataManager = new PlayerDataManager(this, playerDataStore);

        bonusService = new TitleBonusService(playerDataManager, titleRegistry);

        buffManager = new TitleBuffManager(getLogger());

        buffManager.register(new ExpBoostBuff(this));
        buffManager.register(new SaturationBoostBuff(this));
        buffManager.register(new DamageBoostBuff(this));
        buffManager.register(new DefenseBoostBuff(this));
        buffManager.register(new PotionDurationBoostBuff(this));
        buffManager.register(new FallDamageReductionBuff(this));
        buffManager.register(new HungerDrainReductionBuff(this));
        buffManager.register(new MobLootBoostBuff(this));
        buffManager.register(new FishingLuckBoostBuff(this));
        buffManager.register(new KnockbackResistanceBoostBuff(this));
        buffManager.register(new HealingBoostBuff(this));
        buffManager.register(new WaterBreathingBoostBuff(this));
        buffManager.register(new DurabilityWearReductionBuff(this));
        buffManager.register(new StormLightningReductionBuff(this));
        buffManager.enableAll();

        Bukkit.getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);

        collectionerShardService = new CollectionerShardService(this);
        Bukkit.getPluginManager().registerEvents(new CollectionerShardListener(this, collectionerShardService), this);
        anniversaryTokenService = new AnniversaryTokenService(this);
        titleProgressService = new TitleProgressService(this);
        titleTestModeService = new TitleTestModeService();

        sequenceTitleAutoGrantService = new SequenceTitleAutoGrantService(playerDataManager);
        Bukkit.getPluginManager().registerEvents(new CoiAvailabilityListener(this), this);
        registerCoiIntegration();

        Bukkit.getPluginManager().registerEvents(new BuffRegistryValidationListener(this), this);

        LiteBukkitFactory.builder("titles", this)
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
        List.copyOf(WindowManager.getInstance().getWindows()).forEach(Window::close);
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
        collectionerShardService.load();
        anniversaryTokenService.load();
        registerCoiIntegration();
        buffManager.validateAgainstRegistry(titleRegistry);
    }

    public void registerCoiIntegration() {
        if (coiIntegrationRegistered) return;
        if (!CircleOfImaginationHook.isPresent()) return;
        coiIntegrationRegistered = true;

        // these take CoI event types; registering them without CoI throws NoClassDefFoundError
        buffManager.registerAndEnable(new MagicDamageBoostBuff(this));
        buffManager.registerAndEnable(new MagicDefenseBoostBuff(this));
        buffManager.registerAndEnable(new MadnessReductionBuff(this));
        getLogger().info("Circle of Imagination found, magic title bonuses enabled.");

        Bukkit.getPluginManager().registerEvents(new CoiTitleListener(sequenceTitleAutoGrantService), this);
        startSequenceTitleRecheckTask();
    }

    private void startSequenceTitleRecheckTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sequenceTitleAutoGrantService.evaluate(player);
            }
        }, 100L, 200);
    }

}
