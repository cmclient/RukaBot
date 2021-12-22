package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

import java.time.Duration;

public class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        super("nowplaying", "Sends currently played song", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            AudioTrack track = this.bot.getMusicManager().getPlayingTrack(server);

            if (track == null) {
                channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Currently i'm not playing any song."));
                return;
            }

            channel.sendMessage(new CustomEmbed().create(true)
                    .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                    .setTitle("<:watch:901557828127449099> " + this.formatDuration(this.bot.getMusicManager().getPosition(server)))
                    .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
        }, () -> channel.sendMessage(new CustomEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }

    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long absSeconds = Math.abs(seconds);
        String positive = String.format(
                "%d:%02d:%02d",
                absSeconds / 3600,
                (absSeconds % 3600) / 60,
                absSeconds % 60);
        return seconds < 0 ? "-" + positive : positive;
    }
}
