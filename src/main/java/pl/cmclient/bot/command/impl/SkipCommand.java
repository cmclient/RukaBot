package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class SkipCommand extends Command {

    public SkipCommand() {
        super("skip", "Skip to next track", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getConnectedVoiceChannel(event.getApi().getYourself()).ifPresent(voiceChannel -> {
            if (!voiceChannel.isConnected(this.bot.getApi().getYourself())) {
                channel.sendMessage(new RukaEmbed().create(false)
                        .setTitle("I'm not connected to any channel!"));
            } else {
                server.getAudioConnection().ifPresent(connection -> this.bot.getServerMusicManager().skip(user, server, channel));
            }
        }));
    }
}
