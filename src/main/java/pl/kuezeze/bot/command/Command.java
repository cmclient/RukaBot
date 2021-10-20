package pl.kuezeze.bot.command;

import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.kuezeze.bot.BotApplication;
import pl.kuezeze.bot.common.RukaEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Command {

    private final String name;
    private final String description;
    private final List<String> aliases;
    private final boolean onlyOwner;
    private final PermissionType permission;
    protected BotApplication bot;

    public Command(String name, String description, String[] aliases, boolean onlyOwner, PermissionType permission) {
        this.name = name;
        this.description = description;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
        this.onlyOwner = onlyOwner;
        this.permission = permission;
        (this.bot = BotApplication.getInstance()).getCommandManager().add(this);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isOnlyOwner() {
        return onlyOwner;
    }

    public PermissionType getPermission() {
        return permission;
    }

    protected BotApplication getBot() {
        return bot;
    }

    protected String getUsage(String args) {
        return "Usage: " + this.bot.getConfig().getPrefix() + this.name + " " + args;
    }

    public void run(MessageCreateEvent event, String[] args) {
        if (this.onlyOwner) {
            if (event.getServer().get().getOwner().get() != event.getMessageAuthor().asUser().get()) {
                event.getChannel().sendMessage(
                        new RukaEmbed()
                                .create(false)
                                .setTitle(":interrobang: This command can be used only by server owner. (" + this.permission.name() + ")")
                );
                return;
            }
        }
        if (this.permission != null &&
                !event.getServer().get().hasAnyPermission(event.getMessageAuthor().asUser().get(), PermissionType.ADMINISTRATOR, this.permission)) {
            event.getChannel().sendMessage(
                    new RukaEmbed()
                            .create(false)
                            .setTitle(":interrobang: You do not have sufficient privileges to use this command. (" + this.permission.name() + ")")
            );
            return;
        }
        this.execute(event, event.getMessageAuthor().asUser().get(), event.getChannel().asTextChannel().get(), args);
    }

    protected abstract void execute(MessageCreateEvent event, User user, TextChannel channel, String[] args);
}
