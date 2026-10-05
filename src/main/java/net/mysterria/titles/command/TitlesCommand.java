package net.mysterria.titles.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditOutcome;
import dev.ua.ikeepcalm.mysterria.audit.client.api.AuditRisk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.mysterria.titles.MysterriaTitles;
import net.mysterria.titles.audit.AuditContext;
import net.mysterria.titles.audit.AuditRow;
import net.mysterria.titles.gui.TitlesGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

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
        int previousTitles = plugin.getTitleRegistry().size();
        int previousShardRequired = plugin.getCollectionerShardService().requiredCount();
        plugin.reload();
        auditReload(sender, previousTitles, previousShardRequired);
        sender.sendMessage(Component.text("MysterriaTitles configuration reloaded.", NamedTextColor.GREEN));
    }

    private void auditReload(CommandSender sender, int previousTitles, int previousShardRequired) {
        Map<String, Object> metadata = AuditContext.metadata(sender);
        metadata.put("titles_count", plugin.getTitleRegistry().size());
        metadata.put("previous_titles_count", previousTitles);
        metadata.put("shard_required_count", plugin.getCollectionerShardService().requiredCount());
        metadata.put("previous_shard_required_count", previousShardRequired);
        plugin.getAuditEmitter().emit(AuditOutcome.COMMITTED, new AuditRow("titles.admin.reload", AuditRisk.NORMAL,
                UUID.randomUUID(), "titles.yml", AuditContext.actorId(sender), null, null, metadata));
    }
}
