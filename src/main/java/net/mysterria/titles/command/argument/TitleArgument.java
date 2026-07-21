package net.mysterria.titles.command.argument;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import net.mysterria.titles.domain.title.model.Title;
import net.mysterria.titles.domain.title.service.TitleRegistry;
import org.bukkit.command.CommandSender;

public class TitleArgument extends ArgumentResolver<CommandSender, Title> {

    private final TitleRegistry registry;

    public TitleArgument(TitleRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected ParseResult<Title> parse(Invocation<CommandSender> invocation, Argument<Title> argument, String value) {
        return registry.get(value)
                .map(ParseResult::success)
                .orElseGet(() -> ParseResult.failure("Unknown title '" + value + "'."));
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Title> argument, SuggestionContext context) {
        return registry.all().stream()
                .map(Title::id)
                .collect(SuggestionResult.collector());
    }
}
