package pl.cmclient.bot.listener;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.message.MessageEditEvent;
import pl.cmclient.bot.BotApplication;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MessageEditListener implements org.javacord.api.listener.message.MessageEditListener {

    private final BotApplication bot;

    public MessageEditListener(BotApplication bot) {
        this.bot = bot;
    }

    @Override
    public void onMessageEdit(MessageEditEvent event) {
        event.getMessageAuthor().asUser().ifPresent(user -> {
            if (user.isBot()) {
                return;
            }

            event.getServer().ifPresent(server -> {
                String msg = event.getMessage().getContent();
                if ((msg.toLowerCase().contains("discord.gg/") || msg.toLowerCase(Locale.ROOT).contains("discord.com/invite/"))
                        && !server.hasAnyPermission(user, PermissionType.MANAGE_MESSAGES, PermissionType.ADMINISTRATOR)
                        && this.bot.getServerDataManager().get(server.getId()).isInviteBans()) {
                    server.banUser(user, 7, TimeUnit.DAYS, "[" + this.bot.getConfig().getBotName() + "] Automatic ban for " + user.getMentionTag() + " (Sending server invites)");
                }
            });
        });
    }
}
