package pl.kuezeze.bot.command.impl;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.kuezeze.bot.command.Command;
import pl.kuezeze.bot.common.RukaEmbed;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "List of all available commands", new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, TextChannel channel, String[] args) {
        EmbedBuilder embed = new RukaEmbed().create(true);
        embed.setDescription("Bot prefix: **" + this.bot.getConfig().getPrefix() + "**\nAvailable commands:");
        this.bot.getCommandManager().getCommands().forEach(command -> {
            embed.addField(command.getName(), command.getDescription());
        });
        channel.sendMessage(embed);
    }
}
