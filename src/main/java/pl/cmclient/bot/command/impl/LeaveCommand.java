package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class LeaveCommand extends Command {

    public LeaveCommand() {
        super("leave", "Bot leaves channel", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getConnectedVoiceChannel(event.getApi().getYourself()).ifPresentOrElse(voiceChannel -> {
            server.getAudioConnection().ifPresentOrElse(connection -> {
                this.bot.getServerMusicManager().get(server).player.stopTrack();
                connection.close();
            }, () -> event.getChannel().sendMessage(new RukaEmbed().create(false)
                    .setTitle("The bot doesn't seem to be in any voice channel.")));
        }, () -> event.getChannel().sendMessage(new RukaEmbed().create(false)
                .setTitle("The bot doesn't seem to be in any voice channel."))));
    }
}
