package net.mysterria.titles.command.exception;

import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.permission.MissingPermissions;
import dev.rollczi.litecommands.permission.MissingPermissionsHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

public class PermissionsHandler implements MissingPermissionsHandler<CommandSender> {

    @Override
    public void handle(
            Invocation<CommandSender> invocation,
            MissingPermissions missingPermissions,
            ResultHandlerChain<CommandSender> chain
    ) {
        String permissions = missingPermissions.asJoinedText();
        CommandSender sender = invocation.sender();

        sender.sendMessage(
                Component.text("Missing permission! You must be granted: " + permissions, NamedTextColor.RED)
                        .decorate(TextDecoration.BOLD)
        );
    }
}
