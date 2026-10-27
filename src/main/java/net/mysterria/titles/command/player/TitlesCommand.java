package net.mysterria.titles.command.player;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.domain.title.model.PlayerTitleData;
import net.mysterria.titles.gui.TitlesGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "titles")
@Permission("mysterria.titles.use")
@RequiredArgsConstructor
public class TitlesCommand {

    private final MysterriaTitles plugin;

    @Execute
    public void open(@Context Player player) {
        PlayerTitleData data = plugin.getPlayerDataManager().getCached(player.getUniqueId());
        if (data == null) {
            player.sendMessage(Component.text("Your titles are still loading, try again in a moment.", NamedTextColor.RED));
            return;
        }
        new TitlesGui(plugin, player, data).open();
    }

    @Execute(name = "reload")
    @Permission("mysterria.titles.admin")
    public void reload(@Context CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("MysterriaTitles configuration reloaded.", NamedTextColor.GREEN));
    }
}
