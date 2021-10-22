package pl.cmclient.bot.manager;

import org.reflections.Reflections;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.command.Command;
import pl.cmclient.bot.command.CommandType;
import pl.cmclient.bot.helper.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandManager {

    private final List<Command> commands;

    public CommandManager() {
        this.commands = new ArrayList<>();
    }

    public void load(BotApplication bot) {
        String packageName = bot.getClass().getName().replace(bot.getClass().getSimpleName(), "command.impl");
        new Reflections(packageName).getSubTypesOf(Command.class).forEach(command -> {
            try {
                command.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        bot.getLogger().info("Loaded " + commands.size() + " commands.");
    }

    public List<Command> getCommands() {
        return commands;
    }

    public String getCommandsList() {
        StringBuilder builder = new StringBuilder("\n");
        for (CommandType type : CommandType.values()) {
            builder.append(type.getName()).append(":\n");
            List<String> list = this.commands.stream().filter(command -> command.getCommandType() == type).map(command -> "`" + command.getName() + "`").collect(Collectors.toList());
            if (list.isEmpty()) {
                list.add("`No commands`");
            }
            builder.append(StringHelper.join(list, ", ")).append("\n\n");
        }
        return builder.toString();
    }

    public void add(Command command) {
        this.commands.add(command);
    }

    public Optional<Command> get(String name) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(name) || command.getAliases().contains(name.toLowerCase())).findAny();
    }
}
