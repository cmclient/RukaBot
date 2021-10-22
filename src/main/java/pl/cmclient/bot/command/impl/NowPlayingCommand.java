package pl.cmclient.bot.command.impl;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class NowPlayingCommand extends Command {

    public NowPlayingCommand() {
        super("nowplaying", "Sends currently played song", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getConnectedVoiceChannel(event.getApi().getYourself()).ifPresent(voiceChannel -> {
            if (!voiceChannel.isConnected(this.bot.getApi().getYourself())) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("I'm not connected to any channel!"));
            } else {
                server.getAudioConnection().ifPresent(connection -> {
                    AudioTrack track = this.bot.getServerMusicManager().getPlayingTrack(server);

                    if (track == null) {
                        channel.sendMessage(new RukaEmbed().create(false)
                                .setTitle("Currently i'm not playing any song."));
                        return;
                    }

                    channel.sendMessage(new RukaEmbed().create(true)
                            .setTitle("Currently playing: **" + track.getInfo().title + "**"));
                });
            }
        }));
    }
}
