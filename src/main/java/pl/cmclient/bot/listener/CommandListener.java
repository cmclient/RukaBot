package pl.cmclient.bot.listener;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pl.cmclient.bot.BotApplication;

import java.util.Arrays;

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
            if ((msg.toLowerCase().contains("discord.gg/") || msg.toLowerCase().contains("discord.com/invite/"))
                    && !server.hasAnyPermission(user, PermissionType.MANAGE_MESSAGES, PermissionType.ADMINISTRATOR)
                    && this.bot.getServerDataManager().getOrCreate(server.getId()).isInviteBans()) {
                server.banUser(user, 7, "[RukaBot] Automatic ban for " + user.getMentionTag() + " (Sending server invites)");
            }
        }));

        if (msg.startsWith(prefix)) {
            String[] split = msg.split(" ");
            String commandName = split[0].substring(this.bot.getConfig().getPrefix().length());
            this.bot.getCommandManager().get(commandName).ifPresent(command -> command.run(event, Arrays.copyOfRange(split, 1, split.length)));
        }
    }
}
