package net.mysterria.titles.command;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.integration.UnlimitedNameTagsHook;
import net.mysterria.titles.model.PlayerTitleData;
import net.mysterria.titles.model.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
