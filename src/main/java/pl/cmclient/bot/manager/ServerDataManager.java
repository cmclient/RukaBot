package pl.cmclient.bot.manager;

import pl.cmclient.bot.BotApplication;
import pl.cmclient.bot.object.ServerData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        bot.getLogger().info("Loaded " + servers.size() + " servers data");
    }

    public List<ServerData> getServers() {
        return servers;
    }

    public ServerData add(long id) {
        ServerData serverData = new ServerData(id);
        this.servers.add(serverData);
        return serverData;
    }

    public Optional<ServerData> get(long id) {
        return this.servers.stream().filter(serverData -> serverData.getServerId() == id).findAny();
    }

    public ServerData getOrCreate(long id) {
        return this.servers.stream().filter(serverData -> serverData.getServerId() == id).findAny().orElseGet(() -> this.add(id));
    }
}
