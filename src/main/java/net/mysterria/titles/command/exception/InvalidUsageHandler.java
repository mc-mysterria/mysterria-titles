package net.mysterria.titles.command.exception;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.schematic.Schematic;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class InvalidUsageHandler implements dev.rollczi.litecommands.invalidusage.InvalidUsageHandler<CommandSender> {

    @Override
    public void handle(
            Invocation<CommandSender> invocation,
            InvalidUsage<CommandSender> result,
            ResultHandlerChain<CommandSender> chain
    ) {
        CommandSender sender = invocation.sender();
        Schematic schematic = result.getSchematic();

        if (schematic.isOnlyFirst()) {
            sender.sendMessage(
                    Component.text("Invalid usage of command! ", NamedTextColor.RED)
                            .append(Component.text("(" + schematic.first() + ")", NamedTextColor.GRAY))
            );
            return;
        }

        sender.sendMessage(
                Component.text("Invalid usage of command:", NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
        );

        for (String scheme : schematic.all()) {
            sender.sendMessage(
                    Component.text("  • ", NamedTextColor.DARK_GRAY)
                            .append(Component.text(scheme, NamedTextColor.GRAY))
            );
        }
    }
}
