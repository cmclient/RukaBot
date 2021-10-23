package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "Purge the chat", CommandType.MODERATION, new String[]{"purge"}, false, PermissionType.MANAGE_MESSAGES);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0 || !this.isNumber(args[0])) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<amount>")));
            return;
        }
        int amount = Integer.parseInt(args[0]);
        channel.getMessages(amount)
                .thenAcceptAsync(messages -> channel.deleteMessages(messages)
                        .thenAcceptAsync(unused -> channel.sendMessage(new RukaEmbed().create(true)
                                .setTitle(amount + " messages has been purged."))));
    }

    private boolean isNumber(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
