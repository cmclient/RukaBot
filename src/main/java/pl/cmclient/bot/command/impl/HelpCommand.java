package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "List of all available commands", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        channel.sendMessage(new RukaEmbed().create(true)
                .setDescription("Bot prefix: **" + this.bot.getConfig().getPrefix() + "**\nAvailable commands:\n" + this.bot.getCommandManager().getCommandsList()));
    }
}
