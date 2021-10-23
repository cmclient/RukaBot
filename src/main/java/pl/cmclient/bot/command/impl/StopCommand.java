package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Stop playing music", CommandType.MUSIC, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getAudioConnection().ifPresentOrElse(connection -> {
            this.bot.getServerMusicManager().stop(user, server, channel);
        }, () -> event.getChannel().sendMessage(new RukaEmbed().create(false)
                .setTitle("I'm not connected to any channel!"))));
    }
}
