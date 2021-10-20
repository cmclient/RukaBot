package pl.kuezeze.bot.manager;

import org.reflections.Reflections;
import pl.kuezeze.bot.BotApplication;
import pl.kuezeze.bot.command.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommandManager {

    private final List<Command> commands;

    public CommandManager() {
        this.commands = new ArrayList<>();
    }

    public void load(BotApplication bot) {
        Set<Class<? extends Command>> commands = new Reflections("pl.kuezeze.bot.command.impl").getSubTypesOf(Command.class);
        commands.forEach(command -> {
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

    public void add(Command command) {
        this.commands.add(command);
    }

    public Optional<Command> get(String name) {
        return this.commands.stream().filter(command -> command.getName().equalsIgnoreCase(name) || command.getAliases().contains(name.toLowerCase())).findAny();
    }
}
