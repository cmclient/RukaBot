package pl.cmclient.bot.command.impl;

import org.javacord.api.entity.activity.Activity;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.common.RukaEmbed;

import java.util.stream.Collectors;

public class UserInfoCommand extends Command {

    public UserInfoCommand() {
        super("userinfo", "User info", CommandType.GENERAL, new String[0], false, null);
    }

    @Override
    protected void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String[] args) {
        User infoUser = event.getMessage().getMentionedUsers().isEmpty() ? user : event.getMessage().getMentionedUsers().get(0);
        if (infoUser == null) {
            EmbedBuilder embed = new RukaEmbed().create(true);
            embed.setDescription(this.getUsage("<user mention>"));
            channel.sendMessage(embed);
            return;
        }

        event.getChannel().sendMessage(new EmbedBuilder()
                .setTitle("User Info")
                .addField("Display Name", infoUser.getMentionTag(), true)
                .addField("Name + Discriminator", infoUser.getDiscriminatedName(), true)
                .addField("User Id", String.valueOf(infoUser.getId()), true)
                .addField("Online Status", infoUser.getStatus().getStatusString(), true)
                .addField("Connected Clients", infoUser.getCurrentClients().toString())
                .addField("Activity", infoUser.getActivities().isEmpty() ? "none" : infoUser.getActivities().stream().map(Activity::getName).collect(Collectors.joining("\n")), true)
                .setAuthor(infoUser));
    }
}
