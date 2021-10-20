package pl.kuezeze.bot.listener;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pl.kuezeze.bot.BotApplication;

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

        if (msg.startsWith(prefix)) {
            String[] split = msg.split(" ");
            String[] args = Arrays.copyOfRange(split, 1, split.length);
            String commandName = split[0].substring(this.bot.getConfig().getPrefix().length());
            this.bot.getCommandManager().get(commandName).ifPresent(command -> command.run(event, args));
        }
    }
}
