package net.mysterria.titles.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.gui.TitlesGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Command(name = "titles")
@Permission("mysterria.titles.use")
public class TitlesCommand {

    private final MysterriaTitles plugin;

    public TitlesCommand(MysterriaTitles plugin) {
        this.plugin = plugin;
    }

    @Execute
    public void open(@Context Player player) {
        new TitlesGui(plugin, player).open();
    }

    @Execute(name = "reload")
    @Permission("mysterria.titles.admin")
    public void reload(@Context CommandSender sender) {
        plugin.reload();
        sender.sendMessage(Component.text("MysterriaTitles configuration reloaded.", NamedTextColor.GREEN));
    }
}
