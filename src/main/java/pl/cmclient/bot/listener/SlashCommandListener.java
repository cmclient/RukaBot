package pl.cmclient.bot.listener;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.cmclient.bot.BotApplication;

@RequiredArgsConstructor
public class SlashCommandListener extends ListenerAdapter {

    private final BotApplication bot;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        this.bot.getCommandManager().get(event.getName()).ifPresent(command -> command.run(event));
    }
}
