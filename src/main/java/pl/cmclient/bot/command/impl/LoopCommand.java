package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.audio.TrackScheduler;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class LoopCommand extends Command {

    public LoopCommand() {
        super(Commands.slash("loop", "Enables/disables loop-mode")
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

        TrackScheduler scheduler = this.getBot().getMusicManager().get(event.getGuild()).getScheduler();
        scheduler.setLoop(!scheduler.isLoop());
    }
}
