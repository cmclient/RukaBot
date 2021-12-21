package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

import java.awt.*;

public class UnbanAllCommand extends Command {

    public UnbanAllCommand() {
        super("unbanall", "Unbans all banned users from the server", CommandType.ADMINISTRATION, new String[0], false, PermissionType.BAN_MEMBERS);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> {
            server.getBans().thenAccept(bans -> {
                if (args.length == 0) {
                    channel.sendMessage(new RukaEmbed()
                            .create()
                            .setColor(Color.yellow)
                            .setDescription("Are you sure to unban **" + bans.size()
                                    + "** users? If yes execute command: **" + this.bot.getConfig().getPrefix() + "unbanall yes**"));
                    return;
                }
                channel.sendMessage(new RukaEmbed()
                        .create(true)
                        .setTitle("Started unbanning all people. Estimated time: **" + bans.size() * 2 + " seconds**."));
                bans.forEach(ban -> server.unbanUser(ban.getUser()));
            });
        });
    }
}
