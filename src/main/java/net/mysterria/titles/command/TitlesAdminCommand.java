package net.mysterria.titles.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditOutcome;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.audit.AuditContext;
import net.mysterria.titles.audit.AuditRow;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

@Command(name = "titles admin")
@Permission("mysterria.titles.admin")
public class TitlesAdminCommand {

    private static final int MINT_HIGH_RISK_AMOUNT = 64;

    private final MysterriaTitles plugin;

    public TitlesAdminCommand(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "grant")
    public void grant(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        UUID correlationId = UUID.randomUUID();
        if (data.unlock(title.id())) {
            sender.sendMessage(Component.text("Granted '" + title.id() + "' to " + target.getName() + ".", NamedTextColor.GREEN));
            emitObserved(row("titles.title.staff_grant", correlationId, sender, target, title.id(), null,
                    grantMetadata(sender, title, "grant", false)));
        } else {
            sender.sendMessage(Component.text(target.getName() + " already has '" + title.id() + "'.", NamedTextColor.YELLOW));
            plugin.getAuditEmitter().emit(AuditOutcome.DENIED, row("titles.title.staff_grant", correlationId, sender,
                    target, title.id(), "already_unlocked", grantMetadata(sender, title, "grant", true)));
        }
    }

    @Execute(name = "revoke")
    public void revoke(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        boolean wasActive = title.id().equals(data.getActiveTitle().orElse(null));
        Map<String, Object> metadata = titleMetadata(sender, title);
        UUID correlationId = UUID.randomUUID();
        if (data.revoke(title.id())) {
            sender.sendMessage(Component.text("Revoked '" + title.id() + "' from " + target.getName() + ".", NamedTextColor.GREEN));
            UnlimitedNameTagsHook.refresh(target.getUniqueId());
            metadata.put("cleared_active", wasActive);
            emitObserved(row("titles.title.staff_revoke", correlationId, sender, target, title.id(), null, metadata));
        } else {
            sender.sendMessage(Component.text(target.getName() + " doesn't have '" + title.id() + "'.", NamedTextColor.YELLOW));
            metadata.put("cleared_active", false);
            plugin.getAuditEmitter().emit(AuditOutcome.DENIED, row("titles.title.staff_revoke", correlationId, sender,
                    target, title.id(), "not_unlocked", metadata));
        }
    }

    @Execute(name = "set")
    public void set(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        String previousActive = data.getActiveTitle().orElse(null);
        boolean unlockedNow = false;
        if (!data.hasUnlocked(title.id())) {
            unlockedNow = data.unlock(title.id());
        }
        boolean activeSet = data.setActiveTitle(title.id());
        if (activeSet) {
            sender.sendMessage(Component.text("Set " + target.getName() + "'s active title to '" + title.id() + "'.", NamedTextColor.GREEN));
            UnlimitedNameTagsHook.refresh(target.getUniqueId());
        } else {
            sender.sendMessage(Component.text("Could not set active title.", NamedTextColor.RED));
        }
        auditSet(sender, target, title, previousActive, unlockedNow, activeSet);
    }

    private void auditSet(CommandSender sender, Player target, Title title, String previousActive,
                          boolean unlockedNow, boolean activeSet) {
        if (!unlockedNow && !activeSet) return;
        UUID correlationId = UUID.randomUUID();
        if (unlockedNow) {
            emitObserved(row("titles.title.staff_grant", correlationId,
                    sender, target, title.id(), null, grantMetadata(sender, title, "set", false)));
        }
        if (activeSet) {
            Map<String, Object> metadata = titleMetadata(sender, title);
            metadata.put("previous_active", previousActive == null ? "" : previousActive);
            emitObserved(row("titles.admin.set_active", correlationId,
                    sender, target, title.id(), null, metadata));
        }
    }

    @Execute(name = "shard give")
    public void giveShard(@Context CommandSender sender, @Arg Player target, @OptionalArg Integer amount) {
        int give = amount != null ? amount : 1;
        ItemStack shard = plugin.getCollectionerShardService().create(give);
        mint(sender, target, shard, give, "titles.item.mint_shard", "collectioner_shard");

        sender.sendMessage(Component.text("Gave " + give + " Collectioner Shard(s) to " + target.getName() + ".", NamedTextColor.GREEN));
    }

    @Execute(name = "token give")
    public void giveToken(@Context CommandSender sender, @Arg Player target, @OptionalArg Integer amount) {
        int give = amount != null ? amount : 1;
        ItemStack token = plugin.getAnniversaryTokenService().create(give);
        mint(sender, target, token, give, "titles.item.mint_token", "anniversary_token");

        sender.sendMessage(Component.text("Gave " + give + " Anniversary Token(s) to " + target.getName() + ".", NamedTextColor.GREEN));
    }

    @Execute(name = "progress give")
    public void giveProgress(@Context CommandSender sender, @Arg Player target, @Arg Title title, @OptionalArg Integer amount) {
        int give = amount != null ? amount : 1;
        int required = title.progressRequired();
        if (required <= 0) {
            sender.sendMessage(Component.text("'" + title.id() + "' has no progress requirement configured.", NamedTextColor.RED));
            return;
        }

        PlayerTitleData data = plugin.getPlayerDataManager().getCached(target.getUniqueId());
        int previous = data != null ? data.getProgress(title.id()) : 0;
        boolean wasUnlocked = data != null && data.hasUnlocked(title.id());
        int total = plugin.getTitleProgressService().addProgress(target, title.id(), give);
        if (data != null) {
            auditProgressAdd(sender, target, title, give, previous, total, !wasUnlocked && data.hasUnlocked(title.id()));
        }
        if (total >= required) {
            sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' reached " + total + "/" + required + " - title unlocked.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' is now " + total + "/" + required + ".", NamedTextColor.GREEN));
        }
    }

    private void auditProgressAdd(CommandSender sender, Player target, Title title, int delta, int previous,
                                  int total, boolean unlockedNow) {
        boolean flagged = delta <= 0 || delta > 1;
        Map<String, Object> metadata = titleMetadata(sender, title);
        metadata.put("delta", delta);
        metadata.put("old_value", previous);
        metadata.put("new_total", total);
        metadata.put("required", title.progressRequired());
        metadata.put("unlocked_now", unlockedNow);
        metadata.put("flagged_delta", flagged);
        emitObserved(new AuditRow("titles.progress.add", flagged ? AuditRisk.HIGH : AuditRisk.NORMAL,
                UUID.randomUUID(), title.id(), AuditContext.actorId(sender), target.getUniqueId(), null, metadata));
    }

    @Execute(name = "progress set")
    public void setProgress(@Context CommandSender sender, @Arg Player target, @Arg Title title, @Arg Integer amount) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(target.getUniqueId());
        int previous = data != null ? data.getProgress(title.id()) : 0;
        plugin.getTitleProgressService().setProgress(target, title.id(), amount);
        if (data != null) {
            Map<String, Object> metadata = titleMetadata(sender, title);
            metadata.put("old_value", previous);
            metadata.put("new_value", amount);
            metadata.put("required", title.progressRequired());
            emitObserved(row("titles.progress.set", UUID.randomUUID(), sender, target, title.id(), null, metadata));
        }
        sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' set to " + amount + "/" + title.progressRequired() + ".", NamedTextColor.GREEN));
    }

    @Execute(name = "test")
    public void toggleTestMode(@Context Player sender) {
        boolean enabled = plugin.getTitleTestModeService().toggle(sender.getUniqueId());
        Map<String, Object> metadata = AuditContext.metadata(sender);
        metadata.put("enabled", enabled);
        plugin.getAuditEmitter().emit(AuditOutcome.COMMITTED, new AuditRow("titles.admin.test_mode_toggle",
                AuditRisk.NORMAL, UUID.randomUUID(), sender.getUniqueId().toString(), sender.getUniqueId(),
                sender.getUniqueId(), null, metadata));
        if (enabled) {
            sender.sendMessage(Component.text("Test mode enabled - every title shows as unlocked in /titles for this session only.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Test mode disabled.", NamedTextColor.YELLOW));
        }
    }

    @Execute(name = "list")
    public void list(@Context CommandSender sender, @Arg Player target) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        sender.sendMessage(Component.text(target.getName() + "'s active title: " + data.getActiveTitle().orElse("none"), NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Unlocked: " + String.join(", ", data.getUnlockedTitles()), NamedTextColor.GRAY));
    }

    /** No PDC is written: the items are stackable. The lot_id exists only in the audit row. */
    private void mint(CommandSender sender, Player target, ItemStack lot, int amount, String eventType, String itemKind) {
        UUID lotId = UUID.randomUUID();
        String mintedBy = AuditContext.actorRef(sender);

        int overflowCount = 0;
        int droppedCount = 0;
        for (ItemStack leftover : target.getInventory().addItem(lot).values()) {
            int leftoverAmount = leftover.getAmount();
            overflowCount += leftoverAmount;
            Item dropped = target.getWorld().dropItemNaturally(target.getLocation(), leftover);
            if (dropped != null && dropped.isValid() && !dropped.isDead()) {
                droppedCount += leftoverAmount;
            }
        }

        int lost = overflowCount - droppedCount;
        Map<String, Object> metadata = AuditContext.metadata(sender);
        metadata.put("item", itemKind);
        metadata.put("lot_id", lotId.toString());
        metadata.put("actor", mintedBy);
        metadata.put("recipient", target.getUniqueId().toString());
        metadata.put("material", lot.getType().name());
        metadata.put("amount", amount);
        metadata.put("delivered_inventory", amount - overflowCount);
        metadata.put("overflow_dropped_count", droppedCount);
        metadata.put("overflow_lost_count", lost);
        AuditContext.putLocation(metadata, target.getLocation());

        boolean highRisk = amount > MINT_HIGH_RISK_AMOUNT || amount <= 0 || lost > 0;
        plugin.getAuditEmitter().emit(AuditOutcome.COMMITTED, new AuditRow(eventType,
                highRisk ? AuditRisk.HIGH : AuditRisk.NORMAL, lotId, lotId.toString(),
                AuditContext.actorId(sender), target.getUniqueId(), null, metadata));
    }

    /** The change is saved by the autosave, as before, so the row records it as observed, not committed. */
    private void emitObserved(AuditRow row) {
        plugin.getAuditEmitter().emit(AuditOutcome.OBSERVED, row);
    }

    private static AuditRow row(String eventType, UUID correlationId, CommandSender sender, Player target,
                                String titleId, String reason, Map<String, Object> metadata) {
        return new AuditRow(eventType, AuditRisk.NORMAL, correlationId, titleId,
                AuditContext.actorId(sender), target.getUniqueId(), reason, metadata);
    }

    private static Map<String, Object> titleMetadata(CommandSender sender, Title title) {
        Map<String, Object> metadata = AuditContext.metadata(sender);
        metadata.put("title_id", title.id());
        metadata.put("unlock_method", title.unlockMethod().name());
        return metadata;
    }

    private static Map<String, Object> grantMetadata(CommandSender sender, Title title, String via,
                                                     boolean wasAlreadyUnlocked) {
        Map<String, Object> metadata = titleMetadata(sender, title);
        metadata.put("via", via);
        metadata.put("was_already_unlocked", wasAlreadyUnlocked);
        return metadata;
    }

    private PlayerTitleData requireData(CommandSender sender, Player target) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(Component.text(target.getName() + " has no loaded title data.", NamedTextColor.RED));
        }
        return data;
    }
}
