package pl.kuezeze.bot;

import org.apache.log4j.BasicConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import pl.kuezeze.bot.config.Config;
import pl.kuezeze.bot.listener.CommandListener;
import pl.kuezeze.bot.manager.CommandManager;

public class BotApplication {

    private static BotApplication instance;

    private final Logger logger;
    private Config config;
    private CommandManager commandManager;
    private DiscordApi api;

    public BotApplication() {
        instance = this;
        this.logger = LogManager.getLogger(this.getClass());
        this.config = new Config();
        this.start();
    }

    private void start() {
        this.logger.info("Loading configuration...");
        (this.config = new Config()).load(this);
        BasicConfigurator.configure();
        this.logger.info("Token: " + this.censor(this.config.getToken()));
        this.logger.info("Loading commands...");
        (this.commandManager = new CommandManager()).load(this);
        this.logger.info("Loading Discord API...");
        this.api = new DiscordApiBuilder().setToken(this.config.getToken()).login().join();
        this.logger.info("Loading listeners...");
        this.api.addListener(new CommandListener(this));
        this.logger.info("Setting activity...");
        this.api.updateActivity(ActivityType.WATCHING, this.getConfig().getPrefix() + "help");
        this.logger.info("RukaBot | v1.0-SNAPSHOT has been loaded successfully.");
        this.logger.info("Invite: " + this.api.createBotInvite(Permissions.fromBitmask(PermissionType.ADMINISTRATOR.getValue())));
    }

    private String censor(String s) {
        return s.substring(0, s.length() / 2) + s.substring(s.length() / 2).replaceAll(".", "*");
    }

    public static BotApplication getInstance() {
        return instance;
    }

    public Logger getLogger() {
        return logger;
    }

    public Config getConfig() {
        return config;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public DiscordApi getApi() {
        return api;
    }
}
