package pl.cmclient.bot.manager;

import lombok.Getter;
import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.object.ServerData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ServerDataManager {

    private final List<ServerData> servers;

    public ServerDataManager() {
        this.servers = new ArrayList<>();
    }

    public void load(BotApplication bot) {
        bot.getDatabase().query("SELECT * FROM `servers`", rs -> {
            try {
                while (rs.next()) {
                    this.servers.add(new ServerData(rs));
                }
            } catch (SQLException ex) {
                bot.getLogger().error("Failed to load servers data!", ex);
            }
            bot.getLogger().info("Loaded data of {} servers.", servers.size());
        });
    }

    public ServerData add(long id) {
        ServerData serverData = new ServerData(id);
        this.servers.add(serverData);
        return serverData;
    }

    public ServerData get(long id) {
        return this.servers.stream().filter(serverData -> serverData.getServerId() == id).findAny().orElseGet(() -> this.add(id));
    }
}
