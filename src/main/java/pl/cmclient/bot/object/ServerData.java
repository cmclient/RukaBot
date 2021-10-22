package pl.cmclient.bot.object;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private final long serverId;
    private Map<User, PermissionType> userPermissions;

    public ServerData(long serverId) {
        this.serverId = serverId;
        this.userPermissions = new ConcurrentHashMap<>();
    }

    public ServerData(ResultSet rs) throws SQLException {
        this.serverId = rs.getLong("serverId");
    }

    public long getServerId() {
        return serverId;
    }
}
