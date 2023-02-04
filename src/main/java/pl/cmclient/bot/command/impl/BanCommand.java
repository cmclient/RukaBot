package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.helper.StringHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BanCommand extends Command {

    public BanCommand() {
        super("ban", "Bans user from server", CommandType.MODERATION, new String[0], false, PermissionType.BAN_MEMBERS);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new CustomEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<user mention> [reason]")));
            return;
        }
        List<User> mentions = event.getMessage().getMentionedUsers();
        if (mentions.isEmpty()) {
            channel.sendMessage(new CustomEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<user mention> [reason]")));
            return;
        }
        event.getServer().ifPresent(server -> {
            User other = mentions.get(0);
            if (!server.canBanUser(this.bot.getApi().getYourself(), other)) {
                channel.sendMessage(new CustomEmbed()
                        .create(false)
                        .setDescription(":warning: I do not have permission to ban this user."));
                return;
            }
            String reason = args.length == 1 ? "No reason" : StringHelper.join(args, " ", 1, args.length);
            CompletableFuture<Void> future = server.banUser(other, 0, TimeUnit.DAYS, reason);
            future.whenComplete((unused, throwable) -> {
                if (throwable != null) {
                    channel.sendMessage(new CustomEmbed()
                            .create(false)
                            .setDescription("I cant kick him.\nError: " + throwable.getMessage()));
                    future.completeExceptionally(throwable);
                    return;
                }
                channel.sendMessage(new CustomEmbed()
                        .create(true)
                        .setDescription(":white_check_mark: User " + other.getMentionTag() + " has been banned from this server")
                        .addField("Reason", reason));
                future.complete(unused);
            });
        });
    }
}
