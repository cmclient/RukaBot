package pl.cmclient.bot.command;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.common.RukaEmbed;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Command {

    private final String name;
    private final String description;
    private final CommandType commandType;
    private final List<String> aliases;
    private final boolean onlyOwner;
    private final PermissionType permission;
    protected BotApplication bot;

    public Command(String name, String description, CommandType commandType, String[] aliases, boolean onlyOwner, PermissionType permission) {
        this.name = name;
        this.description = description;
        this.commandType = commandType;
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

    public CommandType getCommandType() {
        return commandType;
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

    public void run(MessageCreateEvent event, String... args) {
        event.getServer().ifPresent(server -> event.getMessageAuthor().asUser().ifPresent(user -> {
            if (this.onlyOwner && user.getId() != server.getOwnerId()) {
                event.getChannel().sendMessage(new RukaEmbed()
                        .create(false)
                        .setTitle(":interrobang: This command can be used only by server owner"));
                return;
            }

            if (this.permission != null && !event.getServer().get().hasAnyPermission(user, PermissionType.ADMINISTRATOR, this.permission)) {
                event.getChannel().sendMessage(new RukaEmbed()
                        .create(false)
                        .setTitle(":interrobang: You do not have sufficient privileges to use this command. (" + this.permission.name() + ")"));
                return;
            }

            event.getChannel().asServerTextChannel().ifPresent(channel -> this.execute(event, user, channel, args));
        }));
    }

    protected abstract void execute(MessageCreateEvent event, User user, ServerTextChannel channel, String... args);
}
