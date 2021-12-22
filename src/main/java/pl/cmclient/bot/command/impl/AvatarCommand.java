package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.CustomEmbed;

public class AvatarCommand extends Command {

    public AvatarCommand() {
        super("avatar", "Sends your avatar", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        User avatarUser = event.getMessage().getMentionedUsers().isEmpty() ? user : event.getMessage().getMentionedUsers().get(0);
        if (avatarUser == null) {
            channel.sendMessage(new CustomEmbed().create(false)
                    .setDescription(this.getUsage("<user mention>")));
            return;
        }
        channel.sendMessage(new CustomEmbed().create(true)
                .setDescription(avatarUser.getMentionTag() + "'s avatar")
                .setImage(avatarUser.getAvatar().getUrl().toString() + "?size=2048"));
    }
}
