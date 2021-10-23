package pl.cmclient.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import pl.cmclient.bot.config.Config;
import pl.cmclient.bot.database.Database;
import pl.cmclient.bot.listener.CommandListener;
import pl.cmclient.bot.listener.MessageEditListener;
import pl.cmclient.bot.manager.CommandManager;
import pl.cmclient.bot.manager.ServerDataManager;
import pl.cmclient.bot.manager.MusicManager;
import pl.cmclient.bot.manager.YoutubeApiManager;

import java.util.concurrent.TimeUnit;

public class BotApplication {

    private static BotApplication instance;

    private final Logger logger;
    private Config config;
    private Database database;
    private CommandManager commandManager;
    private ServerDataManager serverDataManager;
    private MusicManager musicManager;
    private YoutubeApiManager youtubeApiManager;
    private DiscordApi api;

    public BotApplication() {
        instance = this;
        this.logger = LogManager.getLogger(this.getClass());
        this.config = new Config();
        this.start();
    }

    private void start() {
        this.logger.info(this.getAsciiArtLogo());
        this.logger.info("Loading configuration...");
        (this.config = new Config()).load(this);
        this.logger.info("Loading database...");
        if ((this.database = new Database()).connect(this)) {
            this.logger.info("Writing tables...");
            this.database.update("CREATE TABLE IF NOT EXISTS `servers` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `serverId` int NOT NULL, `inviteBans` int NOT NULL);");
            this.logger.info("Loading servers data...");
            (this.serverDataManager = new ServerDataManager()).load(this);
        } else {
            this.logger.warn("Unable to connect to the database.");
        }
        this.logger.info("Loading audio player...");
        this.musicManager = new MusicManager();
        this.youtubeApiManager = new YoutubeApiManager(this);
        this.logger.info("Token: " + this.censor(this.config.getToken()));
        this.logger.info("Loading commands...");
        (this.commandManager = new CommandManager()).load(this);
        this.logger.info("Loading Discord API...");
        this.api = new DiscordApiBuilder().setToken(this.config.getToken()).login().join();
        this.logger.info("Loading listeners...");
        this.api.addListener(new CommandListener(this));
        this.api.addListener(new MessageEditListener(this));
        this.logger.info("Setting activity...");
        this.api.updateActivity(ActivityType.WATCHING, this.getConfig().getPrefix() + "help");
        this.logger.info("Adding shutdown hook...");
        this.addShutdownHook();
        this.logger.info(this.config.getBotName() + " | v1.0-SNAPSHOT has been loaded successfully.");
        this.logger.info("Invite: " + this.api.createBotInvite(Permissions.fromBitmask(PermissionType.ADMINISTRATOR.getValue())));
    }

    private String getAsciiArtLogo() {
        return "\n" +
                "██████╗░██╗░░░██╗██╗░░██╗░█████╗░██████╗░░█████╗░████████╗\n" +
                "██╔══██╗██║░░░██║██║░██╔╝██╔══██╗██╔══██╗██╔══██╗╚══██╔══╝\n" +
                "██████╔╝██║░░░██║█████═╝░███████║██████╦╝██║░░██║░░░██║░░░\n" +
                "██╔══██╗██║░░░██║██╔═██╗░██╔══██║██╔══██╗██║░░██║░░░██║░░░\n" +
                "██║░░██║╚██████╔╝██║░╚██╗██║░░██║██████╦╝╚█████╔╝░░░██║░░░\n" +
                "╚═╝░░╚═╝░╚═════╝░╚═╝░░╚═╝╚═╝░░╚═╝╚═════╝░░╚════╝░░░░╚═╝░░░";
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.logger.info("Waiting for database tasks for complete...");
            try {
                this.database.executor.shutdown();
                if (this.database.executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    this.logger.info("Database tasks completed successfully...");
                } else {
                    this.logger.warn("Can't complete some database tasks!");
                }
                this.database.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.logger.info("Goodbye!");
        }, "Shutdown Hook"));
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

    public Database getDatabase() {
        return database;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public ServerDataManager getServerDataManager() {
        return serverDataManager;
    }

    public MusicManager getMusicManager() {
        return musicManager;
    }

    public YoutubeApiManager getYoutubeApiManager() {
        return youtubeApiManager;
    }

    public DiscordApi getApi() {
        return api;
    }
}
