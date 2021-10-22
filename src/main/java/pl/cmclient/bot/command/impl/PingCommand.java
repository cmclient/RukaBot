package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Response time from Discord Gateway", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        channel.sendMessage(new RukaEmbed().create(true).setTitle(":watch: Response time: " +
                TimeUnit.MILLISECONDS.convert(this.bot.getApi().getLatestGatewayLatency().getNano(), TimeUnit.NANOSECONDS)+ "ms"));
    }
}
