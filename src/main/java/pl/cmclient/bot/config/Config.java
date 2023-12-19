package pl.cmclient.bot.config;

import lombok.Getter;
import pl.cmclient.bot.BotApplication;

import java.io.*;
import java.util.Properties;

@Getter
public class Config {

    private String botName;
    private String databaseName;
    private String token;
    private String youtubeApiKey;

    public void load(BotApplication bot) {
        File file = new File("rukabot.cfg");
        Properties properties = new Properties();
        if (!file.exists()) {
            this.saveDefault(properties, file);
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            properties.load(reader);
            String envToken = System.getenv("DISCORD_TOKEN");
            this.botName = properties.getProperty("botName");
            this.databaseName = properties.getProperty("databaseName");
            this.token = envToken == null ? properties.getProperty("token") : envToken;
            this.youtubeApiKey = properties.getProperty("youtubeApiKey");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (this.token.equals("default")) {
            bot.getLogger().warn("Edit configuration (" + file.getAbsolutePath() + ")");
            System.exit(0);
        }
        if (this.youtubeApiKey.equals("default")) {
            bot.getLogger().warn("YouTube API key is not defined! Searching will be unavailable.");
        }
    }

    private void saveDefault(Properties properties, File file) {
        properties.setProperty("botName", "RukaBot");
        properties.setProperty("databaseName", "rukabot.db");
        properties.setProperty("token", "default");
        properties.setProperty("youtubeApiKey", "default");
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
