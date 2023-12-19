package pl.cmclient.bot.listener;

import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.common.CustomEmbed;
import pl.cmclient.bot.object.ServerData;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class MessageListener extends ListenerAdapter {

    private final BotApplication bot;
    private final List<String> bannedWords = List.of("discord.gg/", "discord.com/invite/");

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return;
        }

        User user = event.getAuthor();
        if (user.isBot()) {
            return;
        }

        Member member = event.getMember();
        String msg = event.getMessage().getContentRaw();

        this.checkForDisallowedWords(event.getGuild(), user, member, msg);
    }

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        if (event.getChannelType() == ChannelType.PRIVATE) {
            return;
        }

        User user = event.getAuthor();
        if (user.isBot()) {
            return;
        }

        Member member = event.getMember();
        String msg = event.getMessage().getContentRaw();

        this.checkForDisallowedWords(event.getGuild(), user, member, msg);
    }

    private boolean checkForDisallowedWords(Guild guild, User user, Member member, String msg) {
        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            return false;
        }

        ServerData serverData = this.bot.getServerDataManager().get(guild.getIdLong());

        if (serverData.isInviteBans() && this.bannedWords.stream().anyMatch(s -> msg.toLowerCase().contains(s))) {
            Consumer banConsumer = (o) -> member.ban(24, TimeUnit.HOURS).queue();

            user.openPrivateChannel()
                    .queue(privateChannel -> privateChannel.sendMessageEmbeds(new CustomEmbed()
                                    .create(CustomEmbed.Type.ERROR)
                                    .setTitle("You has been banned for sending server invites.")
                                    .setFooter(guild.getName(), guild.getIconUrl())
                                    .build())
                            .queue(banConsumer, banConsumer), banConsumer);

            this.bot.getLogger().info("User {} banned from server {} for sending server invites.", user.getName(), guild.getName());
            return true;
        }

        if (!serverData.getBannedWords().isEmpty()) {
            serverData.getBannedWords().stream()
                    .filter(string -> !string.isEmpty())
                    .filter(msg::contains).findAny().ifPresent(s -> {
                        Consumer banConsumer = (o) -> member.ban(24, TimeUnit.HOURS).queue();

                        user.openPrivateChannel()
                                .queue(privateChannel -> privateChannel.sendMessageEmbeds(new CustomEmbed()
                                                .create(CustomEmbed.Type.ERROR)
                                                .setTitle("You has been banned for sending blocked words.")
                                                .setFooter(guild.getName(), guild.getIconUrl())
                                                .build())
                                        .queue(banConsumer, banConsumer), banConsumer);

                        this.bot.getLogger().info("User {} banned from server {} for sending disallowed words.", user.getName(), guild.getName());
                    });
        }

        return false;
    }
}
