package pl.cmclient.bot.object;

import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.user.User;
import pl.cmclient.bot.BotApplication;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerData {

    private final long serverId;
    private boolean inviteBans;
    private Map<User, PermissionType> userPermissions;

    public ServerData(long serverId) {
        this.serverId = serverId;
        this.userPermissions = new ConcurrentHashMap<>();
        this.insert();
    }

    public ServerData(ResultSet rs) throws SQLException {
        this.serverId = rs.getLong("serverId");
        this.inviteBans = rs.getBoolean("inviteBans");
    }

    public long getServerId() {
        return serverId;
    }

    public boolean isInviteBans() {
        return inviteBans;
    }

    public void setInviteBans(boolean inviteBans) {
        this.inviteBans = inviteBans;
    }

    private void insert() {
        BotApplication.getInstance().getDatabase().update("INSERT INTO `servers`(`id`, `serverId`, `inviteBans`) VALUES (NULL, '" + this.serverId + "', '" + (this.inviteBans ? 1 : 0) + "');");
    }
}
