package pl.kuezeze.bot.object;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ServerData {

    private final long serverId;

    public ServerData(long serverId) {
        this.serverId = serverId;
    }

    public ServerData(ResultSet rs) throws SQLException {
        this.serverId = rs.getLong("serverId");
    }

    public long getServerId() {
        return serverId;
    }
}
