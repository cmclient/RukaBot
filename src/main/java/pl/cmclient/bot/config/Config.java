package pl.cmclient.bot.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.cmclient.bot.BotApplication;

import java.io.*;
import java.util.Properties;

@RequiredArgsConstructor
@Getter
public class Config {

    private final BotApplication bot;
    private String botName;
    private String databaseName;
    private String token;
    private String youtubeApiKey;
    private String spotifyClientID;
    private String spotifyClientSecret;
    private String youtubeOAuthToken;
    private String ytDlpPath;
    private String ytDlpCookiesPath;
    private String ytDlpJsRuntimes;

    public void load() {
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
            this.spotifyClientID = properties.getProperty("spotifyClientID");
            this.spotifyClientSecret = properties.getProperty("spotifyClientSecret");
            this.youtubeOAuthToken = properties.getProperty("youtubeOAuthToken", "");
            this.ytDlpPath = stripQuotes(properties.getProperty("ytDlpPath", ""));
            this.ytDlpCookiesPath = stripQuotes(properties.getProperty("ytDlpCookiesPath", ""));
            this.ytDlpJsRuntimes = properties.getProperty("ytDlpJsRuntimes", "");
        } catch (IOException ex) {
            bot.getLogger().error("Failed to load configuration!", ex);
        }
        if (this.token.equals("default")) {
            bot.getLogger().warn("Edit configuration ({})", file.getAbsolutePath());
            System.exit(0);
        }
        if (this.youtubeApiKey.equals("default")) {
            bot.getLogger().warn("YouTube API key is not defined! Searching will be unavailable.");
        }
    }

    private static String stripQuotes(String value) {
        if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private void saveDefault(Properties properties, File file) {
        properties.setProperty("botName", "RukaBot");
        properties.setProperty("databaseName", "rukabot.db");
        properties.setProperty("token", "default");
        properties.setProperty("youtubeApiKey", "default");
        properties.setProperty("spotifyClientID", "default");
        properties.setProperty("spotifyClientSecret", "default");
        properties.setProperty("youtubeOAuthToken", "");
        properties.setProperty("ytDlpPath", "");
        properties.setProperty("ytDlpCookiesPath", "");
        properties.setProperty("ytDlpJsRuntimes", "");
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException ex) {
            bot.getLogger().error("Failed to save default configuration!", ex);
        }
    }
}
