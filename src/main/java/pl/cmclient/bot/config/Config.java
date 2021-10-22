package pl.cmclient.bot.config;

import pl.cmclient.bot.BotApplication;

import java.io.*;
import java.util.Properties;

public class Config {

    private String token;
    private String prefix;

    public void load(BotApplication bot) {
        File file = new File("rukabot.cfg");
        Properties properties = new Properties();
        if (!file.exists()) {
            properties.setProperty("token", "defualt");
            properties.setProperty("prefix", "r!");
            try (OutputStream out = new FileOutputStream(file)) {
                properties.store(out, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            bot.getLogger().warn("Edit configuration (" + file.getAbsolutePath() + ")");
            System.exit(0);
            return;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            properties.load(reader);
            String envToken = System.getenv("DISCORD_TOKEN");
            this.token = envToken == null ? properties.getProperty("token") : envToken;
            this.prefix = properties.getProperty("prefix");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }
}
