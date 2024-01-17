package pl.cmclient.bot;

import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.cmclient.bot.config.Config;
import pl.cmclient.bot.database.Database;
import pl.cmclient.bot.listener.MessageListener;
import pl.cmclient.bot.listener.SlashCommandListener;
import pl.cmclient.bot.manager.CommandManager;
import pl.cmclient.bot.manager.MusicManager;
import pl.cmclient.bot.manager.ServerDataManager;
import pl.cmclient.bot.manager.YoutubeApiManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class BotApplication {

    private @Getter
    static BotApplication instance;

    private final Logger logger;
    private Config config;
    private Database database;
    private CommandManager commandManager;
    private ServerDataManager serverDataManager;
    private MusicManager musicManager;
    private YoutubeApiManager youtubeApiManager;
    private JDA jda;

    public BotApplication() {
        instance = this;
        this.logger = LoggerFactory.getLogger(this.getClass());
        try {
            this.start();
        } catch (InterruptedException ex) {
            this.logger.error("Failed to start bot!", ex);
        }
    }

    private void start() throws InterruptedException {
        this.logger.info(this.getAsciiArtLogo());
        this.logger.info("Loading configuration...");
        (this.config = new Config()).load(this);
        this.logger.info("Token: " + this.censor(this.config.getToken()));
        this.logger.info("Loading database...");
        if ((this.database = new Database()).connect(this)) {
            this.logger.info("Writing tables...");
            this.database.update("CREATE TABLE IF NOT EXISTS `servers` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `serverId` int NOT NULL, `inviteBans` int NOT NULL, `bannedWords` text NOT NULL);");
            this.logger.info("Loading servers data...");
            (this.serverDataManager = new ServerDataManager()).load(this);
        } else {
            this.logger.warn("Unable to connect to the database.");
        }
        this.logger.info("Loading audio player...");
        this.musicManager = new MusicManager();
        this.youtubeApiManager = new YoutubeApiManager(this);
        this.logger.info("Loading JDA...");
        this.jda = JDABuilder.create(this.config.getToken(), List.of(GatewayIntent.values()))
                .addEventListeners(new MessageListener(this), new SlashCommandListener(this))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.listening("/help"))
                .build();

        this.jda.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);

        this.logger.info("Loading commands...");
        (this.commandManager = new CommandManager()).load(this, this.jda);
        this.logger.info("Waiting for bot to be ready..");
        this.jda.awaitReady();
        this.logger.info("Bot {} has been loaded successfully.", this.config.getBotName());
        this.logger.info("Invite: {}", this.jda.getInviteUrl(Permission.ADMINISTRATOR));
        this.addShutdownHook();
    }

    private String getAsciiArtLogo() {
        return "\n" +
                "\u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2591\u2588\u2588\u2557\u2591\u2591\u2591\u2588\u2588\u2557\u2588\u2588\u2557\u2591\u2591\u2588\u2588\u2557\u2591\u2588\u2588\u2588\u2588\u2588\u2557\u2591\u2588\u2588\u2588\u2588\u2588\u2588\u2557\u2591\u2591\u2588\u2588\u2588\u2588\u2588\u2557\u2591\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2557\n" +
                "\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u2588\u2588\u2551\u2591\u2591\u2591\u2588\u2588\u2551\u2588\u2588\u2551\u2591\u2588\u2588\u2554\u255D\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u255A\u2550\u2550\u2588\u2588\u2554\u2550\u2550\u255D\n" +
                "\u2588\u2588\u2588\u2588\u2588\u2588\u2554\u255D\u2588\u2588\u2551\u2591\u2591\u2591\u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2550\u255D\u2591\u2588\u2588\u2588\u2588\u2588\u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2588\u2566\u255D\u2588\u2588\u2551\u2591\u2591\u2588\u2588\u2551\u2591\u2591\u2591\u2588\u2588\u2551\u2591\u2591\u2591\n" +
                "\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u2588\u2588\u2551\u2591\u2591\u2591\u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2588\u2588\u2557\u2591\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2551\u2588\u2588\u2554\u2550\u2550\u2588\u2588\u2557\u2588\u2588\u2551\u2591\u2591\u2588\u2588\u2551\u2591\u2591\u2591\u2588\u2588\u2551\u2591\u2591\u2591\n" +
                "\u2588\u2588\u2551\u2591\u2591\u2588\u2588\u2551\u255A\u2588\u2588\u2588\u2588\u2588\u2588\u2554\u255D\u2588\u2588\u2551\u2591\u255A\u2588\u2588\u2557\u2588\u2588\u2551\u2591\u2591\u2588\u2588\u2551\u2588\u2588\u2588\u2588\u2588\u2588\u2566\u255D\u255A\u2588\u2588\u2588\u2588\u2588\u2554\u255D\u2591\u2591\u2591\u2588\u2588\u2551\u2591\u2591\u2591\n" +
                "\u255A\u2550\u255D\u2591\u2591\u255A\u2550\u255D\u2591\u255A\u2550\u2550\u2550\u2550\u2550\u255D\u2591\u255A\u2550\u255D\u2591\u2591\u255A\u2550\u255D\u255A\u2550\u255D\u2591\u2591\u255A\u2550\u255D\u255A\u2550\u2550\u2550\u2550\u2550\u255D\u2591\u2591\u255A\u2550\u2550\u2550\u2550\u255D\u2591\u2591\u2591\u2591\u255A\u2550\u255D\u2591\u2591\u2591";
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
}
