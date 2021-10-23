package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

public class AvatarCommand extends Command {

    public AvatarCommand() {
        super("avatar", "Sends your avatar", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        User avatarUser = event.getMessage().getMentionedUsers().isEmpty() ? user : event.getMessage().getMentionedUsers().get(0);
        if (avatarUser == null) {
            EmbedBuilder embed = new RukaEmbed().create(true);
            embed.setDescription(this.getUsage("<user mention>"));
            channel.sendMessage(embed);
            return;
        }
        channel.sendMessage(new RukaEmbed().create(true)
                .setDescription(avatarUser.getMentionTag() + "'s avatar")
                .setImage(avatarUser.getAvatar().getUrl().toString() + "?size=2048"));
    }
}
