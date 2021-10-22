package pl.cmclient.bot.manager;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.cmclient.bot.BotApplication;

import java.io.IOException;
import java.util.Optional;

public class YoutubeApiManager {

    private final BotApplication bot;

    public YoutubeApiManager(BotApplication bot) {
        this.bot = bot;
    }

    public Optional<String> search(String keyword) {
        if (keyword.contains("://")) {
            return Optional.of(keyword);
        }
        try {
            keyword = keyword.replace(" ", "+");
            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=1&q=" + keyword + "&key=" + this.bot.getConfig().getYoutubeApiKey();
            Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(5000).get();
            JsonObject object = new Gson().fromJson(doc.text(), JsonElement.class).getAsJsonObject();
            JsonArray jsonArray = object.get("items").getAsJsonArray();
            if (jsonArray.size() == 0) {
                return Optional.empty();
            }
            JsonObject items = jsonArray.get(0).getAsJsonObject().get("id").getAsJsonObject();
            return items.has("videoId") ? Optional.of(("https://www.youtube.com/watch?v=" + items.get("videoId").getAsString())) : Optional.empty();
        } catch (IOException ex) {
            return Optional.empty();
        }
    }
}
