package net.mysterria.titles.domain.buff.model;

import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.buff.service.TitleBonusService;

import java.util.UUID;

public abstract class TitleBuff {

    protected final MysterriaTitles plugin;
    protected final TitleBonusService bonusService;

    protected TitleBuff(MysterriaTitles plugin) {
        this.plugin = plugin;
        this.bonusService = plugin.getBonusService();
    }

    public abstract String id();

    public abstract void register();

    public abstract void unregister();

    protected double multiplierFor(UUID uuid) {
        return bonusService.getMultiplier(uuid, id());
    }
}
