package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.AudioPlayer;

import java.util.concurrent.BlockingQueue;

public class ClearQueueCommand extends Command {

    public ClearQueueCommand() {
        super("clearqueue", "Clears song queue", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            AudioPlayer audioPlayer = this.bot.getMusicManager().get(server);
            BlockingQueue<AudioTrack> queue = audioPlayer.scheduler.getQueue();

            if (queue.isEmpty()) {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Song queue is empty."));
                return;
            }

            int tracks = audioPlayer.scheduler.clearQueue();

            channel.sendMessage(new CustomEmbed().create(true)
                    .setTitle("Removed " + tracks + " from song queue."));
        }, () -> channel.sendMessage(new CustomEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }
}
