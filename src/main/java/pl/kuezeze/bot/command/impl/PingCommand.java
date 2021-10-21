package pl.kuezeze.bot.command.impl;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.kuezeze.bot.command.Command;
import pl.kuezeze.bot.common.RukaEmbed;

import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Response time from Discord Gateway", new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, TextChannel channel, String[] args) {
        long ms = TimeUnit.MILLISECONDS.convert(this.bot.getApi().getLatestGatewayLatency().getNano(), TimeUnit.NANOSECONDS);
        EmbedBuilder embed = new RukaEmbed().create(true).setTitle(":watch: Response time: " + ms + "ms");
        channel.sendMessage(embed);
    }
}
