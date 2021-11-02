package pl.cmclient.bot.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pl.cmclient.bot.BotApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

public class YoutubeApiManager {

    private final BotApplication bot;

    public YoutubeApiManager(BotApplication bot) {
        this.bot = bot;
    }

    public Optional<String> search(String query) {
        if (query.contains("://")) {
            return Optional.of(query);
        }
        String apiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=%s&key=%s";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new URL(String.format(apiUrl, query.replace(" ", "+"), this.bot.getConfig().getYoutubeApiKey())).openStream()))) {
            JsonObject object = JsonParser.parseReader(br).getAsJsonObject();
            if (object.has("items")) {
                JsonObject video = object.get("items").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsJsonObject();
                return video.has("videoId") ? Optional.of(("https://www.youtube.com/watch?v=" + video.get("videoId").getAsString())) : Optional.empty();
            } else {
                return Optional.empty();
            }
        } catch (IOException ex) {
            return Optional.empty();
        }
    }
}
