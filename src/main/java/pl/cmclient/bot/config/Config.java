package pl.cmclient.bot.config;

import pl.cmclient.bot.BotApplication;

import java.io.*;
import java.util.Properties;

public class Config {

    private String botName;
    private String sqliteDatabaseName;
    private String token;
    private String prefix;
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
            this.sqliteDatabaseName = properties.getProperty("sqliteDatabaseName");
            this.token = envToken == null ? properties.getProperty("token") : envToken;
            this.prefix = properties.getProperty("prefix");
            this.youtubeApiKey = properties.getProperty("youtubeApiKey");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (this.token.equals("default")) {
            bot.getLogger().warn("Edit configuration (" + file.getAbsolutePath() + ")");
            System.exit(0);
        }
        if (this.youtubeApiKey.equals("default")) {
            bot.getLogger().warn("YouTube api key not defined! Searching music by keywords will not be available.");
        }
    }

    public String getBotName() {
        return botName;
    }

    public String getSqliteDatabaseName() {
        return sqliteDatabaseName;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getYoutubeApiKey() {
        return youtubeApiKey;
    }

    private void saveDefault(Properties properties, File file) {
        properties.setProperty("botName", "RukaBot");
        properties.setProperty("sqliteDatabaseName", "rukabot.db");
        properties.setProperty("token", "default");
        properties.setProperty("prefix", "r!");
        properties.setProperty("youtubeApiKey", "default");
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
