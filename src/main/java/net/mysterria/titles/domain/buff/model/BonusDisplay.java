package net.mysterria.titles.domain.buff.model;

import java.util.Locale;
import java.util.Map;

public record BonusDisplay(String label, Kind kind) {

    public enum Kind {
        BOOST,
        REDUCTION
    }

    private static final Map<String, BonusDisplay> REGISTRY = Map.ofEntries(
            Map.entry("EXP_BOOST", new BonusDisplay("More Experience", Kind.BOOST)),
            Map.entry("SATURATION_BOOST", new BonusDisplay("More Saturation from Food", Kind.BOOST)),
            Map.entry("DAMAGE_BOOST", new BonusDisplay("More Damage Dealt", Kind.BOOST)),
            Map.entry("DEFENSE_BOOST", new BonusDisplay("Less Damage Taken", Kind.REDUCTION)),
            Map.entry("POTION_DURATION_BOOST", new BonusDisplay("Longer Potion Effects", Kind.BOOST)),
            Map.entry("FALL_DAMAGE_REDUCTION", new BonusDisplay("Less Fall Damage", Kind.REDUCTION)),
            Map.entry("HUNGER_DRAIN_REDUCTION", new BonusDisplay("Slower Hunger Drain", Kind.REDUCTION)),
            Map.entry("MOB_LOOT_BOOST", new BonusDisplay("More Mob Drops", Kind.BOOST)),
            Map.entry("FISHING_LUCK_BOOST", new BonusDisplay("Better Fishing Catches", Kind.BOOST)),
            Map.entry("KNOCKBACK_RESISTANCE_BOOST", new BonusDisplay("Less Knockback Taken", Kind.REDUCTION)),
            Map.entry("HEALING_BOOST", new BonusDisplay("More Healing Received", Kind.BOOST)),
            Map.entry("MAGIC_DAMAGE_BOOST", new BonusDisplay("More Magic Damage", Kind.BOOST)),
            Map.entry("MAGIC_DEFENSE_BOOST", new BonusDisplay("Less Magic Damage Taken", Kind.REDUCTION)),
            Map.entry("MADNESS_REDUCTION", new BonusDisplay("Less Madness Gained", Kind.REDUCTION))
    );

    public static BonusDisplay of(String buffId) {
        BonusDisplay known = REGISTRY.get(buffId);
        return known != null ? known : new BonusDisplay(humanize(buffId), Kind.BOOST);
    }

    /**
     * Reduction buffs are applied as damage/amount ÷ (1 + value), so the percentage
     * actually felt by the player is (1 - 1/(1+value)), not the raw configured value.
     */
    public int percent(double value) {
        return switch (kind) {
            case BOOST -> (int) Math.round(value * 100);
            case REDUCTION -> (int) Math.round((1 - 1 / (1 + value)) * 100);
        };
    }

    public String sign() {
        return kind == Kind.REDUCTION ? "-" : "+";
    }

    private static String humanize(String id) {
        String[] parts = id.toLowerCase(Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
