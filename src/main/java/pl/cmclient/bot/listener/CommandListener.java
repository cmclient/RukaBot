package pl.cmclient.bot.listener;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.common.RukaEmbed;
import pl.cmclient.bot.object.ServerData;

import java.util.Arrays;
import java.util.Locale;

public class CommandListener implements MessageCreateListener {

    private final BotApplication bot;

    public CommandListener(BotApplication bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }

        String prefix = this.bot.getConfig().getPrefix();
        String msg = event.getMessage().getContent();

        event.getMessageAuthor().asUser().ifPresent(user -> event.getServer().ifPresent(server -> {
            ServerData serverData = this.bot.getServerDataManager().get(server.getId());
            String msgFormatted = msg.toLowerCase(Locale.ROOT);

            if ((msgFormatted.contains("discord.gg/") || msgFormatted.contains("discord.com/invite/"))
                    && !server.hasAnyPermission(user, PermissionType.MANAGE_MESSAGES, PermissionType.ADMINISTRATOR) && serverData.isInviteBans()) {
                event.getMessage().delete()
                        .thenAccept(unused -> user.sendMessage(new RukaEmbed().create(false).setTitle("You has been banned for sending invites!")))
                        .thenAccept(unused -> server.banUser(user, 7, "[" + this.bot.getConfig().getBotName() + "] Automatic ban for " + user.getMentionTag() + " (Sending server invites)"));
                return;
            }

            if (!server.hasAnyPermission(user, PermissionType.ADMINISTRATOR, PermissionType.MANAGE_MESSAGES)) {
                serverData.getBannedWords().stream().filter(msgFormatted::contains).findAny().ifPresent(s -> {
                    event.getMessage().delete()
                            .thenAccept(unused -> user.sendMessage(new RukaEmbed().create(false).setTitle("You can't send that message in this server!")))
                            .thenAccept(unused -> server.kickUser(user, "Sending not allowed words"))
                            .thenAccept(unused -> this.bot.getLogger().info("User " + user.getName() + " has been kicked from server "
                                    + server.getName() + " for sending not allowed words. (" + s + ") (" + msg + ")"));
                });
            }
        }));

        if (msg.startsWith(prefix)) {
            String[] split = msg.split(" ");
            String commandName = split[0].substring(this.bot.getConfig().getPrefix().length());
            this.bot.getCommandManager().get(commandName).ifPresent(command -> command.run(event, Arrays.copyOfRange(split, 1, split.length)));
        }
    }
}
