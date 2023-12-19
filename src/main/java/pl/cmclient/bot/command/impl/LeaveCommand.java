package pl.cmclient.bot.command.impl;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.managers.AudioManager;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.AudioPlayer;

public class LeaveCommand extends Command {

    public LeaveCommand() {
        super(Commands.slash("leave", "Leave voice channel")
                        .setGuildOnly(true),
                CommandType.MUSIC, false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        if (!audioManager.isConnected()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.ERROR)
                            .setTitle("I'm not connected to any channel.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        AudioPlayer audioPlayer = this.getBot().getMusicManager().get(event.getGuild());
        if (audioPlayer.getScheduler().getPlayingTrack() != null) {
            audioPlayer.getScheduler().stopTrack();
        }

        audioManager.closeAudioConnection();
    }
}
