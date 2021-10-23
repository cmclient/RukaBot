package pl.cmclient.bot.listener;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.message.MessageEditEvent;
import pl.cmclient.bot.BotApplication;

import java.util.Locale;

public class MessageEditListener implements org.javacord.api.listener.message.MessageEditListener {

    private final BotApplication bot;

    public MessageEditListener(BotApplication bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        event.getMessageAuthor().ifPresent(messageAuthor -> {
            if (messageAuthor.isBotUser()) {
                return;
            }

            messageAuthor.asUser().ifPresent(user -> event.getServer().ifPresent(server -> event.getMessage().ifPresent(message -> {
                String msg = message.getContent();
                if ((msg.toLowerCase(Locale.ROOT).contains("discord.gg/") || msg.toLowerCase(Locale.ROOT).contains("discord.com/invite/"))
                        && !server.hasAnyPermission(user, PermissionType.MANAGE_MESSAGES, PermissionType.ADMINISTRATOR)
                        && this.bot.getServerDataManager().get(server.getId()).isInviteBans()) {
                    server.banUser(user, 7, "[" + this.bot.getConfig().getBotName() + "] Automatic ban for " + user.getMentionTag() + " (Sending server invites)");
                }
            })));
        });
    }
}
