package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

import java.time.Duration;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class SeekCommand extends Command {

    public SeekCommand() {
        super("seek", "Seek", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0 || args[0].length() != "00:00:00".length()) {
            channel.sendMessage(new RukaEmbed().create(false)
                    .setTitle(this.getUsage("<HH:MM:SS>\nExample: 00:01:14")));
            return;
        }

        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            AudioTrack track = this.bot.getMusicManager().getPlayingTrack(server);

            if (track == null) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("Currently i'm not playing any song."));
                return;
            }

            if (!track.isSeekable()) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("This track is not seekable!"));
                return;
            }

            this.bot.getMusicManager().setPosition(server, this.formatDuration(args[0]).toMillis(), TimeUnit.MILLISECONDS);

            channel.sendMessage(new RukaEmbed().create(true)
                    .setAuthor(track.getInfo().title, track.getInfo().uri, "https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg")
                    .setTitle("<:watch:901557828127449099> " + args[0])
                    .setThumbnail("https://img.youtube.com/vi/" + track.getInfo().identifier + "/maxresdefault.jpg"));
        }, () -> event.getChannel().sendMessage(new RukaEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }

    private Duration formatDuration(String duration) {
        return Duration.between(LocalTime.MIN, LocalTime.parse(duration));
    }
}
