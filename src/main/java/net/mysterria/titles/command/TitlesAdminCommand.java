package net.mysterria.titles.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.domain.title.model.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(name = "titles admin")
@Permission("mysterria.titles.admin")
public class TitlesAdminCommand {

    private final MysterriaTitles plugin;

    public TitlesAdminCommand(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @Execute(name = "grant")
    public void grant(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        if (data.unlock(title.id())) {
            sender.sendMessage(Component.text("Granted '" + title.id() + "' to " + target.getName() + ".", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(target.getName() + " already has '" + title.id() + "'.", NamedTextColor.YELLOW));
        }
    }

    @Execute(name = "revoke")
    public void revoke(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        if (data.revoke(title.id())) {
            sender.sendMessage(Component.text("Revoked '" + title.id() + "' from " + target.getName() + ".", NamedTextColor.GREEN));
            UnlimitedNameTagsHook.refresh(target.getUniqueId());
        } else {
            sender.sendMessage(Component.text(target.getName() + " doesn't have '" + title.id() + "'.", NamedTextColor.YELLOW));
        }
    }

    @Execute(name = "set")
    public void set(@Context CommandSender sender, @Arg Player target, @Arg Title title) {
        PlayerTitleData data = requireData(sender, target);
        if (data == null) return;

        if (!data.hasUnlocked(title.id())) {
            data.unlock(title.id());
        }
        if (data.setActiveTitle(title.id())) {
            sender.sendMessage(Component.text("Set " + target.getName() + "'s active title to '" + title.id() + "'.", NamedTextColor.GREEN));
            UnlimitedNameTagsHook.refresh(target.getUniqueId());
        } else {
            sender.sendMessage(Component.text("Could not set active title.", NamedTextColor.RED));
        }
    }

    @Execute(name = "shard give")
    public void giveShard(@Context CommandSender sender, @Arg Player target, @OptionalArg Integer amount) {
        int give = amount != null ? amount : 1;
        ItemStack shard = plugin.getCollectionerShardService().create(give);

        var overflow = target.getInventory().addItem(shard);
        overflow.values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));

        sender.sendMessage(Component.text("Gave " + give + " Collectioner Shard(s) to " + target.getName() + ".", NamedTextColor.GREEN));
    }

    @Execute(name = "token give")
    public void giveToken(@Context CommandSender sender, @Arg Player target, @OptionalArg Integer amount) {
        int give = amount != null ? amount : 1;
        ItemStack token = plugin.getAnniversaryTokenService().create(give);

        var overflow = target.getInventory().addItem(token);
        overflow.values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));

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

        int total = plugin.getTitleProgressService().addProgress(target, title.id(), give);
        if (total >= required) {
            sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' reached " + total + "/" + required + " - title unlocked.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' is now " + total + "/" + required + ".", NamedTextColor.GREEN));
        }
    }

    @Execute(name = "progress set")
    public void setProgress(@Context CommandSender sender, @Arg Player target, @Arg Title title, @Arg Integer amount) {
        plugin.getTitleProgressService().setProgress(target, title.id(), amount);
        sender.sendMessage(Component.text(target.getName() + "'s progress on '" + title.id() + "' set to " + amount + "/" + title.progressRequired() + ".", NamedTextColor.GREEN));
    }

    @Execute(name = "test")
    public void toggleTestMode(@Context Player sender) {
        boolean enabled = plugin.getTitleTestModeService().toggle(sender.getUniqueId());
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

    private PlayerTitleData requireData(CommandSender sender, Player target) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(target.getUniqueId());
        if (data == null) {
            sender.sendMessage(Component.text(target.getName() + " has no loaded title data.", NamedTextColor.RED));
        }
        return data;
    }
}
