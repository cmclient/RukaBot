package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "List of all available commands", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        channel.sendMessage(new CustomEmbed().create(true)
                .setAuthor(this.bot.getApi().getYourself())
                .setTitle("Bot prefix: **" + this.bot.getConfig().getPrefix() + "**\n**Available commands**:").setDescription(this.bot.getCommandManager().getCommandsList()));
    }
}
