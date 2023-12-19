package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.AudioPlayer;

import java.util.concurrent.BlockingQueue;

public class ClearQueueCommand extends Command {

    public ClearQueueCommand() {
        super(Commands.slash("clearqueue", "Clears song queue")
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

        AudioPlayer audioPlayer = this.getBot().getMusicManager().get(event.getGuild());
        BlockingQueue<AudioTrack> queue = audioPlayer.getScheduler().getQueue();

        if (queue.isEmpty()) {
            event.replyEmbeds(new CustomEmbed()
                            .create(CustomEmbed.Type.WARNING)
                            .setTitle("Song queue is empty.")
                            .build())
                    .setEphemeral(true)
                    .queue();
            return;
        }

        int removedTracks = audioPlayer.getScheduler().clearQueue();

        event.replyEmbeds(new CustomEmbed()
                        .create(CustomEmbed.Type.SUCCESS)
                        .setTitle("Removed " + removedTracks + " songs from queue.")
                        .build())
                //.setEphemeral(true)
                .queue();
    }
}
