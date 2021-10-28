package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;
import pl.cmclient.bot.helper.StringHelper;
import pl.cmclient.bot.object.ServerData;

import java.util.Locale;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Configure bot for you server", CommandType.ADMINISTRATION, new String[0], false, PermissionType.MANAGE_SERVER);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<key> [value]") + "\nConfiguration keys: `inviteBans`, `bannedWords`"));
            return;
        }

        event.getServer().ifPresent(server -> {
            ServerData serverData = this.bot.getServerDataManager().get(server.getId());

            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "invitebans":
                    if (args.length == 1) {
                        channel.sendMessage(new RukaEmbed()
                                .create(true)
                                .setDescription("Automatic bans for sending invites are **" + (serverData.isInviteBans() ? "enabled" : "disabled") + "**"));
                    } else {
                        boolean value = Boolean.parseBoolean(args[1]);
                        serverData.setInviteBans(value);
                        channel.sendMessage(new RukaEmbed()
                                .create(true)
                                .setDescription("Automatic bans for sending has been **" + (value ? "enabled" : "disabled") + "**"));
                    }
                    break;
                case "bannedwords":
                    if (args.length == 1) {
                        channel.sendMessage(new RukaEmbed()
                                .create(true)
                                .setDescription("Currently banned words:\n" + StringHelper.join(serverData.getBannedWords(), ", ")));
                    } else {
                        String value = args[1];
                        if (serverData.getBannedWords().contains(value)) {
                            serverData.removeBannedWord(value);
                            channel.sendMessage(new RukaEmbed()
                                    .create(true)
                                    .setDescription("Removed banned word: **" + value + "**"));
                        } else {
                            serverData.addBannedWord(value);
                            channel.sendMessage(new RukaEmbed()
                                    .create(true)
                                    .setDescription("Added banned word: **" + value + "**"));
                        }
                    }
                    break;
                default:
                    channel.sendMessage(new RukaEmbed()
                            .create(false)
                            .setDescription(this.getUsage("<key> [value]") + "\nConfiguration keys: `inviteBans`, `bannedWords`"));
                    break;
            }
        });
    }
}
