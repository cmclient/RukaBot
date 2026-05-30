package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.ServerData;

public class TwoFourSevenCommand extends Command {

    public TwoFourSevenCommand() {
        super(Commands.slash("247", "Toggle 24/7 mode (bot stays in voice channel even when alone)")
                        .setContexts(InteractionContextType.GUILD),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        ServerData serverData = this.getBot().getServerDataManager().get(event.getGuild().getIdLong());
        boolean newState = !serverData.isTwoFourSeven();
        serverData.setTwoFourSeven(newState);

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setTitle("24/7 mode is now " + (newState ? "**enabled** 🟢" : "**disabled** 🔴"))
                        .setDescription(newState
                                ? "Bot will stay in the voice channel even when everyone leaves."
                                : "Bot will leave the voice channel when the last user leaves.")
                        .build())
                .queue();
    }
}

