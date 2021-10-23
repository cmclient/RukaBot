package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;
import pl.cmclient.bot.object.ServerData;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "Configure bot for you server", CommandType.ADMINISTRATION, new String[0], false, PermissionType.MANAGE_SERVER);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        if (args.length == 0) {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<key> [value]") + "\n\nConfiguration keys: inviteBans"));
            return;
        }

        if (args[0].equalsIgnoreCase("inviteBans")) {
            event.getServer().ifPresent(server -> {
                ServerData serverData = this.bot.getServerDataManager().get(server.getId());
                if (args.length == 1) {
                    channel.sendMessage(new RukaEmbed()
                            .create(true)
                            .setDescription("`" + args[0] + "` value: " + serverData.isInviteBans()));
                } else {
                    boolean value = Boolean.parseBoolean(args[1]);
                    serverData.setInviteBans(value);
                    channel.sendMessage(new RukaEmbed()
                            .create(true)
                            .setDescription("Changed `" + args[0] + "` to: " + value));
                }
            });
        } else {
            channel.sendMessage(new RukaEmbed()
                    .create(false)
                    .setDescription(this.getUsage("<key> <value>") + "\nConfiguration keys: inviteBans"));
        }
    }
}
