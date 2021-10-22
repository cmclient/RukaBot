package pl.kuezeze.bot.command.impl;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.kuezeze.bot.command.Command;
import pl.kuezeze.bot.command.CommandType;
import pl.kuezeze.bot.common.RukaEmbed;
import pl.kuezeze.bot.helper.StringHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class KickCommand extends Command {

    public KickCommand() {
        super("kick", "Kick user from server", CommandType.MODERATION, new String[0], false, PermissionType.KICK_MEMBERS);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, TextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<user mention> [reason]")));
            return;
        }
        List<User> mentions = event.getMessage().getMentionedUsers();
        if (mentions.isEmpty()) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<user mention> [reason]")));
            return;
        }
        event.getServer().ifPresent(server -> {
            User other = mentions.get(0);
            if (server.canKickUser(this.bot.getApi().getYourself(), other)) {
                channel.sendMessage(new RukaEmbed()
                        .create(false)
                        .setDescription(":warning: I do not have permission to ban this user."));
                return;
            }
            String reason = args.length == 1 ? "No reason" : StringHelper.join(args, "", 2, args.length);
            CompletableFuture<Void> future = server.kickUser(other, reason);
            future.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    channel.sendMessage(new RukaEmbed()
                            .create(false)
                            .setDescription("I cant kick him.\nError: " + throwable.getMessage()));
                    future.completeExceptionally(throwable);
                    return;
                }
                channel.sendMessage(new RukaEmbed()
                        .create(true)
                        .setDescription(":white_check_mark: User " + other.getMentionTag() + " has been kicked from this server")
                        .addField("Reason", reason));
                future.complete(unused);
            });
        });
    }
}
