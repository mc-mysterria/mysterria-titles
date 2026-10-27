package net.mysterria.titles.domain.buff.model;

import lombok.Data;
import net.mysterria.titles.domain.buff.model.source.BonusKind;

import java.util.Locale;
import java.util.Map;

@Data
public class BonusDisplay {

    private static final Map<String, BonusDisplay> REGISTRY = Map.ofEntries(
            Map.entry("EXP_BOOST", new BonusDisplay("More Experience", BonusKind.BOOST)),
            Map.entry("SATURATION_BOOST", new BonusDisplay("More Saturation from Food", BonusKind.BOOST)),
            Map.entry("DAMAGE_BOOST", new BonusDisplay("More Damage Dealt", BonusKind.BOOST)),
            Map.entry("DEFENSE_BOOST", new BonusDisplay("Less Damage Taken", BonusKind.REDUCTION)),
            Map.entry("POTION_DURATION_BOOST", new BonusDisplay("Longer Potion Effects", BonusKind.BOOST)),
            Map.entry("FALL_DAMAGE_REDUCTION", new BonusDisplay("Less Fall Damage", BonusKind.REDUCTION)),
            Map.entry("HUNGER_DRAIN_REDUCTION", new BonusDisplay("Slower Hunger Drain", BonusKind.REDUCTION)),
            Map.entry("MOB_LOOT_BOOST", new BonusDisplay("More Mob Drops", BonusKind.BOOST)),
            Map.entry("FISHING_LUCK_BOOST", new BonusDisplay("Better Fishing Catches", BonusKind.BOOST)),
            Map.entry("KNOCKBACK_RESISTANCE_BOOST", new BonusDisplay("Less Knockback Taken", BonusKind.REDUCTION)),
            Map.entry("HEALING_BOOST", new BonusDisplay("More Healing Received", BonusKind.BOOST)),
            Map.entry("MAGIC_DAMAGE_BOOST", new BonusDisplay("More Magic Damage", BonusKind.BOOST)),
            Map.entry("MAGIC_DEFENSE_BOOST", new BonusDisplay("Less Magic Damage Taken", BonusKind.REDUCTION)),
            Map.entry("MADNESS_REDUCTION", new BonusDisplay("Less Madness Gained", BonusKind.REDUCTION)),
            Map.entry("WATER_BREATHING_BOOST", new BonusDisplay("Longer Breath Underwater", BonusKind.BOOST)),
            Map.entry("DURABILITY_WEAR_REDUCTION", new BonusDisplay("Slower Tool and Armor Wear", BonusKind.REDUCTION))
    );

    private final String label;
    private final BonusKind kind;

    public static BonusDisplay of(String buffId) {
        BonusDisplay known = REGISTRY.get(buffId);
        return known != null ? known : new BonusDisplay(humanize(buffId), BonusKind.BOOST);
    }

    public String describe(double value) {
        return kind.getSign() + kind.percent(value) + "% " + label;
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
