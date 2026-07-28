package net.mysterria.titles;

import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import lombok.Getter;
import net.mysterria.titles.command.TitlesAdminCommand;
import net.mysterria.titles.command.TitlesCommand;
import net.mysterria.titles.command.argument.TitleArgument;
import net.mysterria.titles.command.exception.InvalidUsageHandler;
import net.mysterria.titles.command.exception.PermissionsHandler;
import net.mysterria.titles.config.ConfigManager;
import net.mysterria.titles.domain.buff.service.TitleBonusService;
import net.mysterria.titles.domain.buff.service.TitleBuffManager;
import net.mysterria.titles.domain.buff.types.*;
import net.mysterria.titles.domain.papi.TitlesExpansion;
import net.mysterria.titles.domain.shard.AnniversaryTokenService;
import net.mysterria.titles.domain.shard.CollectionerShardService;
import net.mysterria.titles.domain.storage.model.JsonPlayerDataStore;
import net.mysterria.titles.domain.storage.service.PlayerDataManager;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.SequenceTitleAutoGrantService;
import net.mysterria.titles.domain.title.service.TitleProgressService;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import net.mysterria.titles.domain.title.service.TitleTestModeService;
import net.mysterria.titles.integration.CircleOfImaginationHook;
import net.mysterria.titles.listener.CoiAvailabilityListener;
import net.mysterria.titles.listener.CoiTitleListener;
import net.mysterria.titles.listener.CollectionerShardListener;
import net.mysterria.titles.listener.PlayerLifecycleListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        buffManager.enableAll();

        Bukkit.getPluginManager().registerEvents(new PlayerLifecycleListener(this), this);

        collectionerShardService = new CollectionerShardService(this);
        Bukkit.getPluginManager().registerEvents(new CollectionerShardListener(this, collectionerShardService), this);
        anniversaryTokenService = new AnniversaryTokenService(this);
        titleProgressService = new TitleProgressService(this);
        titleTestModeService = new TitleTestModeService();

        sequenceTitleAutoGrantService = new SequenceTitleAutoGrantService(playerDataManager);
        Bukkit.getPluginManager().registerEvents(new CoiAvailabilityListener(this), this);
        registerCoiIntegration(); // covers the case COI is already enabled by this point

        buffManager.validateAgainstRegistry(titleRegistry);

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
        registerCoiIntegration(); // retries in case COI came up after us and PluginEnableEvent was missed somehow
        buffManager.validateAgainstRegistry(titleRegistry);
    }

    /**
     * Adds the Circle-of-Imagination-only buffs and the sequence/uniqueness auto-grant listener.
     * Safe to call multiple times (idempotent) and safe to call before COI is actually up (no-op
     * until it is) - called once inline in onEnable() for the common case, and again from
     * CoiAvailabilityListener whenever COI enables after us instead.
     */
    public void registerCoiIntegration() {
        if (coiIntegrationRegistered) return;
        if (!CircleOfImaginationHook.isPresent()) return;
        coiIntegrationRegistered = true;

        buffManager.registerAndEnable(new MagicDamageBoostBuff(this));
        buffManager.registerAndEnable(new MagicDefenseBoostBuff(this));
        buffManager.registerAndEnable(new MadnessReductionBuff(this));
        buffManager.validateAgainstRegistry(titleRegistry);

        Bukkit.getPluginManager().registerEvents(new CoiTitleListener(sequenceTitleAutoGrantService), this);
        startSequenceTitleRecheckTask();
    }

    /**
     * No dedicated COI event covers uniqueness-trait changes, so this is the safety net for that
     * plus anything else missed between SequenceChangeEvent firings (e.g. state changed while
     * the player was offline).
     */
    private void startSequenceTitleRecheckTask() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                sequenceTitleAutoGrantService.evaluate(player);
            }
        }, 100L, 200);
    }

}
