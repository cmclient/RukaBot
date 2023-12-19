package pl.cmclient.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.common.CustomEmbed;

@RequiredArgsConstructor @Getter
public abstract class Command {

    protected final SlashCommandData data;
    private final CommandType commandType;
    private final boolean onlyOwner;
    private BotApplication bot;

    public abstract void execute(SlashCommandInteractionEvent event);

    public void run(SlashCommandInteractionEvent event) {
        if (this.onlyOwner && event.getGuild() != null && event.getInteraction().getMember().getIdLong() != event.getGuild().getOwnerIdLong()) {
            event.getChannel().sendMessageEmbeds(new CustomEmbed()
                    .create(CustomEmbed.Type.ERROR)
                    .setTitle(":interrobang: This command can be used only by server owner").build()).queue();
            return;
        }

        this.execute(event);
    }

    public void register(BotApplication bot) {
        (this.bot = bot).getCommandManager().getCommands().put(this.data.getName(), this);
    }
}
