package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.invite.Invite;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class RemoveUnusedInvitesCommand extends Command {

    public RemoveUnusedInvitesCommand() {
        super("removeunusedinvites", "Removes invites with less than 10 uses", CommandType.ADMINISTRATION, new String[0], false, PermissionType.MANAGE_CHANNELS);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        event.getServer().ifPresent(server -> server.getInvites()
                .thenAccept(richInvites -> richInvites
                        .stream()
                        .filter(richInvite -> richInvite.getUses() < 10)
                        .forEach(Invite::delete))
                .thenAccept(unused -> channel.sendMessage(new CustomEmbed().create(false)
                        .setTitle("Completed removing invites."))));
    }
}
