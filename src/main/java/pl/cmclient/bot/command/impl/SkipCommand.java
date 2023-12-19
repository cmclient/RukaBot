package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class SkipCommand extends Command {

    public SkipCommand() {
        super(Commands.slash("skip", "Skip to next track")
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }


    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("I'm not connected to any channel.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        this.getBot().getMusicManager().skip(event);
    }
}
